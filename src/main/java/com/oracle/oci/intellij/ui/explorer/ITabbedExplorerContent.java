package com.oracle.oci.intellij.ui.explorer;

import javax.swing.JComponent;

public interface ITabbedExplorerContent {
  public String getTitle();
  public JComponent createCenterPanel();
}