package com.oracle.oci.intellij.ui.git.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.hash.LinkedHashMap;

public class GitConfig {
  
  private GitConfigCore core;
  private LinkedHashMap<String, GitConfigRemote> remotes = new LinkedHashMap<>();
  private LinkedHashMap<String, GitConfigBranch> branches = new LinkedHashMap<>();
  private Map<GitConfigBranch, GitConfigRemote>  branchToRemote = new HashMap<>();
  private Map<String, GitConfigRemote> remoteUrls = new HashMap<>();
  private @Nullable Optional<@Nullable VirtualFile> gitConfigFile;
  private volatile long configFileTimeStamp;
  
  public void setCore(GitConfigCore core) {
    this.core = core;
  }
  
  public void addRemote(GitConfigRemote remote) {
    this.remotes.put(remote.remoteName, remote);
  }
  
  public void addBranch(GitConfigBranch branch) {
    this.branches.put(branch.getBranch(), branch);
  }
  
  public void updateMappings() {
    branchToRemote.clear();
    this.branches.forEach((k,v) -> {
      String branchName = k;
      assert branchName == v.getBranch();
      String remoteName = v.getRemote();
      GitConfigRemote gitConfigRemote = this.remotes.get(remoteName);
      v.setRemoteObj(gitConfigRemote);
      branchToRemote.put(v, gitConfigRemote);
    });
    
    remoteUrls.clear();
    this.remotes.forEach((k,v) -> {
      remoteUrls.put(v.getUrl(), v);
    });
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder();
    Optional.ofNullable(core).ifPresent(c -> builder.append(c.toString()));
    this.remotes.forEach((k,v) -> builder.append(v.toString()));
    this.branches.forEach((k,v) -> builder.append(v.toString()));
    return builder.toString();
  }

  public Set<String> getUrls() {
    return Collections.unmodifiableSet(this.remoteUrls.keySet());
  }

  public boolean checkStale() {
    long curTS = this.configFileTimeStamp;
    // always returns non-stale if the source file isn't set
    if (gitConfigFile.isPresent()) {
      curTS = gitConfigFile.get().getTimeStamp();
    }
    return curTS != this.configFileTimeStamp;
  }

  public void dispose() {
    branches.clear();
    remotes.clear();
    core = null;
    remoteUrls.clear();
    branchToRemote.clear();
  }

  public void setConfigFile(@Nullable VirtualFile gitConfigFile) {
    this.gitConfigFile = Optional.ofNullable(gitConfigFile);
    this.gitConfigFile.ifPresent(gcf -> configFileTimeStamp = gcf.getTimeStamp());
  }
}