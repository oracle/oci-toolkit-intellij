/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.common;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.wm.WindowManager;
import com.oracle.oci.intellij.account.SystemPreferences;
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

  private static @NlsSafe final String NOTIFICATION_GROUP_ID = "Oracle Cloud Infrastructure";

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

  public static void fireNotification(NotificationType notificationType, @NotNull final String msg, String eventName) {
    invokeLater(() -> {
      NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)
              .createNotification(NOTIFICATION_GROUP_ID, "", msg, notificationType)
              .notify(currentProject);

      if (eventName != null) {
        SystemPreferences.fireADBInstanceUpdateEvent(eventName);
      }});
  }

  public static void executeAndUpdateUIAsync(@NotNull Runnable action, @Nullable Runnable update) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      action.run();
      if (update != null) {
        invokeLater(update);
      }
    });
  }

  public static void invokeLater(Runnable runnable) {
    ApplicationManager.getApplication().invokeLater(runnable);
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
