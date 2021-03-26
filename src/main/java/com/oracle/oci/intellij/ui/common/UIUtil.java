/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.common;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.oracle.oci.intellij.util.LogHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Arrays;

public class UIUtil {

  private static Project currentProject;
  private static final NotificationGroup NOTIFICATION_GROUP =
      new NotificationGroup("Oracle Cloud Infrastructure",
          NotificationDisplayType.BALLOON, true);

  private static final char[] ADMIN_CHARS = "admin".toCharArray();

  public static void setCurrentProject(@NotNull Project project) {
    currentProject = project;
  }

  public static void setStatus(@NotNull final String msg) {
    if(msg == null || currentProject == null)
      return;
    try {
      WindowManager.getInstance()
          .getStatusBar(currentProject)
          .setInfo(msg);
    }
    catch (Exception e) {
      LogHandler.error("Unable to update the status", e);
    }
  }

  public static void fireSuccessNotification(@NotNull final String msg) {
    //fireNotification(msg, Messages.getInformationIcon(), JBColor.PanelBackground);
    final Notification notification = NOTIFICATION_GROUP.createNotification("Oracle Cloud Infrastructure","",
        msg, NotificationType.INFORMATION);
    notification.notify(currentProject);
  }

  public static void fireErrorNotification(@NotNull final String msg) {
    //fireNotification(msg, Messages.getErrorIcon(), JBColor.PanelBackground);
    final Notification notification = NOTIFICATION_GROUP.createNotification("Oracle Cloud Infrastructure","",
        msg, NotificationType.ERROR);
    notification.notify(currentProject);
  }

  public static void fetchAndUpdateUI(@NotNull Runnable fetch, @Nullable Runnable update) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try{
        fetch.run();
      }
      catch (Throwable th) {
        LogHandler.error("Unable to execute the fetch job", th);
        fireErrorNotification("Unable to execute the fetch job" + th.getMessage());
      }
      // Scchedule the UI update after fetching the data in background thread.
      if(update != null)  {
        ApplicationManager.getApplication().invokeLater(update);
      }


    });
  }

  public static void makeWebLink(JLabel lbl, String uri) {
    lbl.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    lbl.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        try {
          if(!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            return;
          Desktop.getDesktop().browse(new URI(uri));
        }
        catch (Exception ex) {
        }
      }
    });
  }

  public static Project getCurrentProject() {
    return currentProject;
  }

  public static boolean isValidAdminPassword(char[] pwd) {
    if(pwd == null || pwd.length == 0)
      return false;

    if(pwd.length < 12 || pwd.length > 30)
      return false;

    final char[] pwdLowerCase = new char[pwd.length];
    for(int i = 0; i < pwd.length; i++)
      pwdLowerCase[i]  = Character.toLowerCase(pwd[i]);

    final boolean result = (!Arrays.equals(pwdLowerCase, ADMIN_CHARS))
        && (Arrays.binarySearch(pwd, '\"') == -1);
    Arrays.fill(pwdLowerCase,' ');

    return result;
  }
}
