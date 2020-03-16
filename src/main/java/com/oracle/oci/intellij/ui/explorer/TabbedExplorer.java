package com.oracle.oci.intellij.ui.explorer;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBTabbedPane;
import com.oracle.oci.intellij.ui.database.DatabaseDetails;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TabbedExplorer extends DialogWrapper {
  private JBTabbedPane tabbedPane1;
  private JPanel mainPanel;

  DatabaseDetails databaseDetails;

  public TabbedExplorer(final ToolWindow toolWindow) {
    super(true);
    init();
  }

  public JComponent getContent() {
    // Each module will have a Tab in the toolbar, as of now only database tab.
    databaseDetails = new DatabaseDetails();
    tabbedPane1.add("ADB Instances", databaseDetails.createCenterPanel());
    return mainPanel;

  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return mainPanel;
  }
}
