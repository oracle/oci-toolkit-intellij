package com.oracle.oci.intellij.ui.common;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.JBColor;
import com.oracle.oci.intellij.LogHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class UIUtil {

  private static Project currentProject;
  private static final NotificationGroup NOTIFICATION_GROUP =
      new NotificationGroup("Oracle Cloud Infrastructure",
          NotificationDisplayType.BALLOON, true);

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
    final Notification notification = NOTIFICATION_GROUP.createNotification("OCI","",
        msg, NotificationType.INFORMATION);
    notification.notify(currentProject);
  }

  public static void fireErrorNotification(@NotNull final String msg) {
    //fireNotification(msg, Messages.getErrorIcon(), JBColor.PanelBackground);
    final Notification notification = NOTIFICATION_GROUP.createNotification("OCI","",
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

  public static boolean isValidAdminPassword(String pwd) {
    if(pwd == null)
      return false;
    pwd = pwd.trim();
    return !(  pwd.isEmpty()
            || pwd.equalsIgnoreCase("admin")
            || pwd.indexOf('\"') != -1
            || pwd.length() < 12
            || pwd.length() > 30);
  }
}
