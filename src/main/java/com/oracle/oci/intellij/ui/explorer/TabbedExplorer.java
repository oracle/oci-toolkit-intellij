/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.explorer;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBTabbedPane;
import com.oracle.oci.intellij.ui.database.AutonomousDatabasesDashboard;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TabbedExplorer extends DialogWrapper {
  private JBTabbedPane tabbedPane;
  private JPanel mainPanel;
  private final AutonomousDatabasesDashboard autonomousDatabasesTable;

  public TabbedExplorer(final ToolWindow toolWindow, final AutonomousDatabasesDashboard dashboard) {
    super(true);
    this.autonomousDatabasesTable = dashboard;
    init();
  }

  public JComponent getContent() {
    tabbedPane.add("Autonomous Database Instances", autonomousDatabasesTable.createCenterPanel());
    return mainPanel;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return mainPanel;
  }
}
