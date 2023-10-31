/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.explorer;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBTabbedPane;

public class TabbedExplorer<TabContent extends ITabbedExplorerContent> extends DialogWrapper {
  private JBTabbedPane tabbedPane;
  private JPanel mainPanel;
  private final TabContent tabbedExplorerContent;

  public TabbedExplorer(final ToolWindow toolWindow, final TabContent dashboard) {
    super(true);
    this.tabbedExplorerContent = dashboard;
    init();
  }

  public JComponent getContent() {
    tabbedPane.add(tabbedExplorerContent.getTitle(), tabbedExplorerContent.createCenterPanel());
    return mainPanel;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return mainPanel;
  }
}
