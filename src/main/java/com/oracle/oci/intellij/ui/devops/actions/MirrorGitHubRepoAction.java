package com.oracle.oci.intellij.ui.devops.actions;

import java.io.IOException;
import java.util.Optional;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.SystemIndependent;

import com.google.common.io.Files;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;

public class MirrorGitHubRepoAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    System.out.println("actionPerformed");
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    @Nullable
    Project project = e.getProject();
    
    super.update(e);
  }

  
}
