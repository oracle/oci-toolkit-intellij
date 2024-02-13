package com.oracle.oci.intellij.ui.git.config;

import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.hash.LinkedHashMap;

public class GitConfig {
  private Optional<Project> projectOptional;
  private AtomicInteger age = new AtomicInteger(-1);
  
  private GitConfigCore core;
  private LinkedHashMap<String, GitConfigRemote> remotes = new LinkedHashMap<>();
  private LinkedHashMap<String, GitConfigBranch> branches = new LinkedHashMap<>();
  private WeakHashMap<GitConfigBranch, GitConfigRemote>  branchToRemote = new WeakHashMap<>();
  
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
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder();
    Optional.ofNullable(core).ifPresent(c -> builder.append(c.toString()));
    this.remotes.forEach((k,v) -> builder.append(v.toString()));
    this.branches.forEach((k,v) -> builder.append(v.toString()));
    return builder.toString();
  }

  public boolean checkStale() {
    // TODO Auto-generated method stub
    return false;
  }
}