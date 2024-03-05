/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.common;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.ApplicationImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.wm.WindowManager;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.devops.wizard.mirrorgh.CreateSecretWizardModel.SecretWizardContext;

import io.github.resilience4j.core.lang.NonNull;

public class UIUtil {
  private static Project currentProject;

  private static @NlsSafe final String NOTIFICATION_GROUP_ID =
    "Oracle Cloud Infrastructure";

  public static void setCurrentProject(@NotNull Project project) {
    currentProject = project;
  }

  public static void showInfoInStatusBar(@NotNull final String info) {
    if (currentProject != null) {
      WindowManager.getInstance().getStatusBar(currentProject).setInfo(info);
    }
  }

  public static void fireNotification(NotificationType notificationType,
                                      @NotNull final String msg,
                                      String eventName) {
    invokeLater(() -> {
      NotificationGroupManager.getInstance()
                              .getNotificationGroup(NOTIFICATION_GROUP_ID)
                              .createNotification(NOTIFICATION_GROUP_ID, "",
                                                  msg, notificationType)
                              .notify(currentProject);

      if (eventName != null) {
        SystemPreferences.fireADBInstanceUpdateEvent(eventName);
      }
    });
  }

  public static void warn(final String msg) {
    fireNotification(NotificationType.WARNING, msg, "");
  }

  public static void executeAndUpdateUIAsync(@NotNull Runnable action,
                                             @Nullable Runnable update) {
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
          if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            return;
          }
          Desktop.getDesktop().browse(new URI(uri));
        } catch (URISyntaxException | IOException ex) {
          throw new RuntimeException(ex);
        }
      }
    });
  }

  public static SimpleDialogWrapper createDialog(String title,
                                                 boolean canBeParent) {
    SimpleDialogWrapper dialog = new SimpleDialogWrapper(canBeParent) {
      @Override
      protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        GridLayout layout = new GridLayout(1, 1);
        panel.setLayout(layout);

        panel.add(new JTextArea("Default SimpleDialogWrapper. Override createCenterPanel or provide fn"));
        return panel;
      }
    };
    dialog.setTitle(title);
    return dialog;
  }

  public static SimpleDialogWrapper createDialog(String title,
                                                 boolean canBeParent,
                                                 Function<SimpleDialogWrapper, JComponent> createCenterPane) {
    SimpleDialogWrapper dialog = new SimpleDialogWrapper(canBeParent);
    dialog.setTitle(title);
    return dialog;
  }

  public static class SimpleDialogWrapper extends DialogWrapper {

    private Function<SimpleDialogWrapper, JComponent> createCenterPanelFn;

    public SimpleDialogWrapper(boolean canBeParent) {
      this((Project) null, canBeParent);
    }

    protected SimpleDialogWrapper(@NotNull Component parent,
                                  boolean canBeParent) {
      super(parent, canBeParent);
      basicInit();
    }

    public SimpleDialogWrapper(@Nullable Project project, boolean canBeParent,
                               @NotNull IdeModalityType ideModalityType) {
      this(project, null, canBeParent, ideModalityType);
    }

    public SimpleDialogWrapper(@Nullable Project project, boolean canBeParent) {
      this(project, canBeParent, IdeModalityType.IDE);
    }

    public SimpleDialogWrapper(@Nullable Project project) {
      this(project, true);
    }

    public SimpleDialogWrapper(@Nullable Project project,
                               @Nullable Component parentComponent,
                               boolean canBeParent,
                               @NotNull IdeModalityType ideModalityType) {
      this(project, parentComponent, canBeParent, ideModalityType, true);
    }

    protected SimpleDialogWrapper(Project project, boolean canBeParent,
                                  boolean applicationModalIfPossible) {
      super(project, canBeParent, applicationModalIfPossible);
      basicInit();
    }

    protected SimpleDialogWrapper(@Nullable Project project,
                                  @Nullable Component parentComponent,
                                  boolean canBeParent,
                                  @NotNull IdeModalityType ideModalityType,
                                  boolean createSouth) {
      super(project, parentComponent, canBeParent, ideModalityType,
            createSouth);
    }

    protected void basicInit() {
      setOKButtonText("OK");
      init();
    }

    public void setCreatePanelFn(Function<SimpleDialogWrapper, JComponent> fn) {
      this.createCenterPanelFn = fn;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
      if (this.createCenterPanelFn != null) {
        return this.createCenterPanelFn.apply(this);
      }
      return null;
    }
  }

  public static <MODEL> ModelHolder<MODEL> holdModel(MODEL m) {
    return new ModelHolder<MODEL>(m);
  }

  public static class ModelHolder<MODEL> implements Supplier<MODEL> {
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
      return textProvider.orElse((m) -> {
        return m.toString();
      }).apply(this.model);
    }

    public ModelHolder<MODEL> setTextProvider(Function<MODEL, String> provider) {
      this.textProvider = Optional.of(provider);
      return this;
    }

    public static <T> Optional<T> getComboItem(@NonNull JComboBox<ModelHolder<T>> comboBox) {
      Object selectedItem = comboBox.getSelectedItem();
      ModelHolder<T> selected = (ModelHolder<T>) selectedItem;
      return Optional.ofNullable(selected.get());
    }
  }

  public static class GridBagLayoutConstraintBuilder {
    private GridBagLayoutConstraintBuilder() {
      
    }
    private int gridx;
    private int gridy;
    private int gridwidth;
    private int gridheight;
    private int weightx;
    private int weighty;
    private int anchor;
    private int fill;
    private Insets insets;
    private int ipadx;
    private int ipady;

    public GridBagLayoutConstraintBuilder gridx(int gridx) {
      this.gridx = gridx;
      return this;
    }

    public GridBagLayoutConstraintBuilder gridy(int gridy) {
      this.gridy = gridy;
      return this;
    }

    public GridBagLayoutConstraintBuilder gridwidth(int width) {
      this.gridwidth = width;
      return this;
    }

    public GridBagLayoutConstraintBuilder gridHeight(int gridheight) {
      this.gridheight = gridheight;
      return this;
    }

    public GridBagLayoutConstraintBuilder fillHorizontal() {
      this.fill = GridBagConstraints.HORIZONTAL;
      return this;
    }

    public GridBagLayoutConstraintBuilder fillVertical() {
      this.fill = GridBagConstraints.VERTICAL;
      return this;
    }

    public GridBagLayoutConstraintBuilder fillBoth() {
      this.fill = GridBagConstraints.BOTH;
      return this;
    }

    public GridBagLayoutConstraintBuilder fillNone() {
      this.fill = GridBagConstraints.NONE;
      return this;
    }

    public GridBagLayoutConstraintBuilder ipadx(int ipadx) {
      this.ipadx = ipadx;
      return this;
    }

    public GridBagLayoutConstraintBuilder ipady(int ipady) {
      this.ipady = ipady;
      return this;
    }

    public GridBagLayoutConstraintBuilder insets(Insets insets) {
      this.insets = insets;
      return this;
    }
    
    public GridBagLayoutConstraintBuilder anchor(int anchor) {
      this.anchor = anchor;
      return this;
    }
    
    public GridBagLayoutConstraintBuilder weightx(int weightx) {
      this.weightx = weightx;
      return this;
    }
    
    public GridBagLayoutConstraintBuilder weighty(int weighty) {
      this.weighty = weighty;
      return this;
    }
    
    public static GridBagLayoutConstraintBuilder defaults() {
      GridBagLayoutConstraintBuilder builder = new GridBagLayoutConstraintBuilder();
      builder.gridx = GridBagConstraints.RELATIVE;
      builder.gridy = GridBagConstraints.RELATIVE;
      builder.gridwidth = 1;
      builder.gridheight = 1;
      
      builder.weightx = 0;
      builder.weighty = 0;
      builder.anchor = GridBagConstraints.CENTER;
      builder.fill = GridBagConstraints.NONE;

      builder.insets = new Insets(0, 0, 0, 0);
      builder.ipadx = 0;
      builder.ipady = 0;
      
      return builder;
    }
    
    public GridBagConstraints build() {
      return new GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady);
    }
  }
}
