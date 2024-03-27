package com.oracle.oci.intellij.ui.git.config;

public class GitConfigCore extends ConsumingGitConfigObject {
  public GitConfigCore() {
  }

  @Override
  protected String getSectionLine() {
    return "[core]";
  }

  @Override
  protected boolean keysAreEqual(ConsumingGitConfigObject obj) {
    return obj instanceof GitConfigCore;
  }
}