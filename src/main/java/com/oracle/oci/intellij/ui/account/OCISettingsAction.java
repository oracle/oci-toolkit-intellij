package com.oracle.oci.intellij.ui.account;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.oci.intellij.account.PreferencesWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class OCISettingsAction extends AnAction {

  public OCISettingsAction() {
    super("OCI Settings", "OCI Explorer Settings",
        new ImageIcon(OCISettingsAction
            .class.getResource("/icons/toolbar-login.png")));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final OCISettingsPanel settings = new OCISettingsPanel();
    settings.showAndGet();
  }
}
