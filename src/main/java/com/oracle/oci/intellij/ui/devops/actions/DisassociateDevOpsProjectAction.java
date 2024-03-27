package com.oracle.oci.intellij.ui.devops.actions;

import javax.swing.JOptionPane;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.oracle.oci.intellij.settings.OCIProjectSettings;
import com.oracle.oci.intellij.settings.OCIProjectSettings.State;

public class DisassociateDevOpsProjectAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (isEnabled(e)) {
      int confirmed = JOptionPane.showConfirmDialog(
                               null, "Are you sure you want to delete assocation between this project and OCI?", 
                               "Are you sure", JOptionPane.YES_NO_OPTION);
      if (confirmed == JOptionPane.OK_OPTION) {
        OCIProjectSettings settings = OCIProjectSettings.getInstance(e.getProject());
        @Nullable
        State state = settings.getState();
        if (state != null) {
          state.clearDevOpsAssociation();
        }
      }
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    boolean isEnabled = isEnabled(e);
    e.getPresentation().setEnabled(isEnabled);
  }

  private boolean isEnabled(AnActionEvent e) {
    @Nullable
    State state = OCIProjectSettings.getInstance(e.getProject()).getState();
    boolean isEnabled = false;
    if (state != null) {
      isEnabled = state.getDevOpsProjectId() != null;
    }
    return isEnabled;
  }

  
}
