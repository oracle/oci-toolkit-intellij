/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.common;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.wm.WindowManager;
import com.oracle.oci.intellij.util.LogHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class UIUtil {
  private static Project currentProject;

  private static @NlsSafe final String id = "Oracle Cloud Infrastructure";
  //private static NotificationGroup notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(id);
  private static final NotificationGroup notificationGroup =
          new NotificationGroup("Oracle Cloud Infrastructure",
                                NotificationDisplayType.BALLOON, true);

  public static void setCurrentProject(@NotNull Project project) {
    currentProject = project;
  }

  public static void showInfoInStatusBar(@NotNull final String info) {
    if(currentProject != null) {
      WindowManager.getInstance()
              .getStatusBar(currentProject)
              .setInfo(info);
    }
  }

  public static void fireNotification(NotificationType notificationType, @NotNull final String msg) {
    notificationGroup
            .createNotification(id, "", msg, notificationType)
            .notify(currentProject);
  }

  public static void executeAndUpdateUIAsync(@NotNull Runnable action, @Nullable Runnable update) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try{
        action.run();
      } catch (Throwable th) {
        final String errorMsg = "Action execution failed.";
        LogHandler.error(errorMsg, th);
        fireNotification(NotificationType.ERROR, errorMsg + th.getMessage());
      }
      /* Schedule the UI update after fetching the data in background thread. */
      if (update != null) {
        ApplicationManager.getApplication().invokeLater(update);
      }
    });
  }

  public static void createWebLink(JComponent component, String uri) {
    component.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    component.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        try {
          if(!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            return;
          }
          Desktop.getDesktop().browse(new URI(uri));
        } catch (URISyntaxException | IOException ex) {
          throw new RuntimeException(ex);
        }
      }
    });
  }
}
