package com.oracle.oci.intellij.ui.explorer;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import com.oracle.oci.intellij.ui.account.CompartmentSelectionAction;
import com.oracle.oci.intellij.ui.account.OCISettingsAction;
import com.oracle.oci.intellij.ui.account.RegionSelectionAction;
import com.oracle.oci.intellij.ui.common.UIUtil;
import org.jetbrains.annotations.NotNull;



public class OCIExplorerFactory
    implements ToolWindowFactory {

  private ToolWindowEx toolWindowEx;

  public OCIExplorerFactory() {

  }

  @Override
  public void createToolWindowContent(@NotNull Project project,
      @NotNull ToolWindow toolWindow) {
    UIUtil.setCurrentProject(project);
    toolWindowEx = (ToolWindowEx) toolWindow;

    // This actions are available on the top right of the toolbar
    final DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(new OCISettingsAction());
    actionGroup.add(new RegionSelectionAction());
    actionGroup.add(new CompartmentSelectionAction());
    toolWindowEx.setTitleActions(actionGroup);


    final TabbedExplorer ociTabbedToolBar = new TabbedExplorer(toolWindow);
    final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    final Content ociTabbedToolBarContent = contentFactory
        .createContent(ociTabbedToolBar.getContent(), "", false);
    toolWindow.getContentManager().addContent(ociTabbedToolBarContent);

  }

}
