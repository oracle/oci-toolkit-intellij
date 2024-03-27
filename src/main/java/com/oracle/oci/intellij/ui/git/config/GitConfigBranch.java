package com.oracle.oci.intellij.ui.git.config;

import java.util.Optional;

public class GitConfigBranch extends ConsumingGitConfigObject {
  private GitConfigRemote remote;
  private String branch;

  public GitConfigBranch(String branch) {
    this.branch = branch;
  }

  public String getBranch() {
    return branch;
  }

  public String getRemote() {
    return getValue("remote");
  }
  public GitConfigBranch setRemoteObj(GitConfigRemote remote) {
    this.remote = remote;
    return this;
  }

  public Optional<GitConfigRemote> getRemoteObj() {
    return Optional.ofNullable(this.remote);
  }

  @Override
  protected String getSectionLine() {
    return String.format("[branch %s]", this.branch);
  }

  @Override
  public int hashCode() {
    return this.branch.hashCode();
  }

  @Override
  protected boolean keysAreEqual(ConsumingGitConfigObject obj) {
    if (!(obj instanceof GitConfigCore)) { return false; }
    return this.branch.equals(((GitConfigBranch)obj).getBranch());
  }
  
}