/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

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
    super("Oracle Cloud Infrastructure", "OCI Explorer Settings",
        new ImageIcon(OCISettingsAction
            .class.getResource("/icons/toolbar-login.png")));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final OCISettingsPanel settings = new OCISettingsPanel();
    settings.showAndGet();
  }
}
