package com.oracle.oci.intellij.ui.git.config;

public class GitConfigRemote extends ConsumingGitConfigObject {

  String remoteName;

  public GitConfigRemote(String remoteName) {
    this.remoteName = remoteName;
  }

  public String getRemoteName() {
    return remoteName;
  }

  public String getUrl() {
    return getValue("url");
  }
  
  public String getFetch() {
    return getValue("fetch");
  }

  @Override
  public int hashCode() {
    return getRemoteName().hashCode();
  }
  
  protected String getSectionLine() {
    return String.format("[remote \"%s\"]", getRemoteName());
  }

  @Override
  protected boolean keysAreEqual(ConsumingGitConfigObject obj) {
    if (!(obj instanceof GitConfigRemote)) { return false; }
    return this.remoteName.equals(((GitConfigRemote)obj).getRemoteName());
  }
  
}