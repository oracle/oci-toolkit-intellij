/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.common;

import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.ApplicationImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.wm.WindowManager;
import com.oracle.oci.intellij.account.SystemPreferences;

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
  public static void schedule(Runnable runnable){
    Thread t = new Thread(() -> {
          runnable.run();
    });
    t.start();
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
  
  public static <MODEL> ModelHolder<MODEL> holdModel(MODEL m) {
    return new ModelHolder<MODEL>(m);
  }

  public static class ModelHolder<MODEL> implements Supplier<MODEL>{
    private final MODEL model;
    private Optional<Function<MODEL, String>> textProvider;
    
    public ModelHolder(@NotNull MODEL model) {
      this.model = model;
    }

    @Override
    public MODEL get() {
      return this.model;
    }

    @Override
    public int hashCode() {
      return this.model.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return this.model.equals(obj);
    }

    @Override
    public String toString() {
      return textProvider.orElse((m) -> {return m.toString();}).apply(this.model);
    }
    
    public ModelHolder<MODEL> setTextProvider(Function<MODEL, String> provider) {
      this.textProvider = Optional.of(provider);
      return this;
    }
  }
}
