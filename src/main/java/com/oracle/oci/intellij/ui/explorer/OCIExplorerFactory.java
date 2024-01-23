/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.explorer;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
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
import com.oracle.oci.intellij.util.BundleUtil;
import com.oracle.oci.intellij.util.LogHandler;

public class OCIExplorerFactory implements ToolWindowFactory {

  public OCIExplorerFactory() {
    BundleUtil.withContextCL(
      this.getClass().getClassLoader(), 
      new Runnable() {
        @Override
        public void run() {
          try {
            OracleCloudAccount.getInstance().configure(SystemPreferences.getConfigFilePath(),
                                                       SystemPreferences.getProfileName());
            AutonomousDatabasesDashboard.getInstance().populateTableData();
          } catch (Exception ex) {
           final String message = "Oracle Cloud account configuration failed: " + ex.getMessage();
           LogHandler.warn(message);
           UIUtil.fireNotification(NotificationType.ERROR, message, null);
          }}}
        );
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

    final TabbedExplorer ociTabbedToolBar =
      new TabbedExplorer(toolWindow,
                         AutonomousDatabasesDashboard.getInstance());
    final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    final Content ociTabbedToolBarContent =
      contentFactory.createContent(ociTabbedToolBar.getContent(), "", false);
    toolWindow.getContentManager().addContent(ociTabbedToolBarContent);
  }

}
