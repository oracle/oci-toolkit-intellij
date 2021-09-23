/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.account;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Action handler for selection event of UI component 'Oracle Cloud Infrastructure'.
 */
public class ConfigureAction extends AnAction {

  public ConfigureAction() {
    super("Oracle Cloud Infrastructure configuration", "Oracle Cloud Infrastructure configuration",
        new ImageIcon(ConfigureAction
            .class.getResource("/icons/toolbar-login.png")));
  }

  /**
   * Event handler.
   *
   * @param event event.
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    ConfigureOracleCloudDialog.newInstance().showAndGet();
  }
}
