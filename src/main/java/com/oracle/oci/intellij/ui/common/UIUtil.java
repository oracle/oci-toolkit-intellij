package com.oracle.oci.intellij.ui.common;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.WindowManager;
import com.oracle.oci.intellij.ErrorHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class UIUtil {

  private static Project currentProject;

  public static void setCurrentProject(@NotNull Project project) {
    currentProject = project;
  }

  public static void setStatus(@NotNull final String msg) {
    try {
      WindowManager.getInstance()
          .getStatusBar(currentProject)
          .setInfo(msg);
    }
    catch (Exception e) {
      ErrorHandler.logErrorStack("Unable to update the status", e);
    }
  }

  public static void fireSuccessNotification(@NotNull final String msg) {
    fireNotification(msg, Messages.getInformationIcon(), Color.LIGHT_GRAY);
  }

  public static void fireErrorNotification(@NotNull final String msg) {
    fireNotification(msg, Messages.getErrorIcon(), Color.RED);
  }

  public static void fireNotification(@NotNull final String msg,
      Icon msgIcon, Color color) {
    try {
      JLabel notificationLabel = new JLabel(msg);
      notificationLabel.setIcon(msgIcon);
      WindowManager.getInstance()
          .getStatusBar(currentProject)
          .fireNotificationPopup(notificationLabel, color);
    }
    catch (Exception e) {
      ErrorHandler.logErrorStack("Unable to update the status", e);
    }
  }

  public static void fetchAndUpdateUI(@NotNull Runnable fetch, @Nullable Runnable update) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        fetch.run();
        // Scchedule the UI update after fetching the data in background thread.
        if(update != null)
          ApplicationManager.getApplication().invokeLater(update);
      }
    });
  }

  public static Project getCurrentProject() {
    return currentProject;
  }
}
