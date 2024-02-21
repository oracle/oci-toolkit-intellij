package com.oracle.oci.intellij.ui.git.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.hash.LinkedHashMap;

public class GitConfig {
  private Optional<Project> projectOptional;
  private AtomicInteger age = new AtomicInteger(-1);
  
  private GitConfigCore core;
  private LinkedHashMap<String, GitConfigRemote> remotes = new LinkedHashMap<>();
  private LinkedHashMap<String, GitConfigBranch> branches = new LinkedHashMap<>();
  private Map<GitConfigBranch, GitConfigRemote>  branchToRemote = new HashMap<>();
  private Map<String, GitConfigRemote> remoteUrls = new HashMap<>();
  
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
    // TODO Auto-generated method stub
    return false;
  }

  public void dispose() {
    branches.clear();
    remotes.clear();
    core = null;
    projectOptional = null;
    remoteUrls.clear();
    branchToRemote.clear();
    age = null;
  }
}