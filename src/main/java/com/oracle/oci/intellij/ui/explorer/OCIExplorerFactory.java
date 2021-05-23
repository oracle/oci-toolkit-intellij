/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.explorer;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.account.CompartmentAction;
import com.oracle.oci.intellij.ui.account.ConfigureAction;
import com.oracle.oci.intellij.ui.account.RegionAction;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.AutonomousDatabasesDashboard;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class OCIExplorerFactory implements ToolWindowFactory {

  private final AutonomousDatabasesDashboard autonomousDatabasesDashboard;

  public OCIExplorerFactory() {
    OracleCloudAccount.getInstance().configure(SystemPreferences.getConfigFilePath(),
            SystemPreferences.getProfileName());
    autonomousDatabasesDashboard = new AutonomousDatabasesDashboard();
    SystemPreferences.addPropertyChangeListener(autonomousDatabasesDashboard);
  }

  @Override
  public void createToolWindowContent(@NotNull Project project,
                                      @NotNull ToolWindow toolWindow) {

    UIUtil.setCurrentProject(project);

    // This actions are available on the top right of the toolbar
    final DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(new ConfigureAction());
    actionGroup.add(new RegionAction());
    actionGroup.add(new CompartmentAction());
    toolWindow.setTitleActions(Arrays.asList(actionGroup));

    final TabbedExplorer ociTabbedToolBar = new TabbedExplorer(toolWindow, autonomousDatabasesDashboard);
    final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    final Content ociTabbedToolBarContent = contentFactory
            .createContent(ociTabbedToolBar.getContent(), "", false);
    toolWindow.getContentManager().addContent(ociTabbedToolBarContent);
  }

}
