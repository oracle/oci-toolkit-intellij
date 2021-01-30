/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;
import com.oracle.oci.intellij.ui.database.ADBInstanceWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;

public class AdminPasswordWizard extends DialogWrapper {
  private JPanel mainPanel;
  private JPanel contentPanel;
  private JTextField userNameTxt;
  private JPasswordField pwdTxt;
  private JPasswordField confirmPwdTxt;

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;

  protected AdminPasswordWizard(
      AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    init();
    setTitle("ADB Admin Password Change");
    setOKButtonText("Update");
    userNameTxt.setText("ADMIN");
    userNameTxt.setEditable(false);
  }

  @Override protected void doOKAction() {
    if (!isValidPassword()) {
      return;
    }
    Runnable nonblockingUpdate = () -> {
      try {
        final char[] pwd = pwdTxt.getPassword();
        ADBInstanceClient.getInstance()
            .changeAdminPassword(autonomousDatabaseSummary,
                new String(pwd));
        Arrays.fill(pwd, ' ');
        ApplicationManager.getApplication().invokeLater(() -> UIUtil
            .fireSuccessNotification("Admin Password Updated Successfully."));
      }
      catch (Exception e) {
        ApplicationManager.getApplication().invokeLater(() -> UIUtil
            .fireErrorNotification("Admin Password Update failed : " + e.getMessage()));
      }
    };

    // Do this in background
    UIUtil.fetchAndUpdateUI(nonblockingUpdate, null);
    close(DialogWrapper.OK_EXIT_CODE);
  }

  private boolean isValidPassword() {
    final char[] adminPassword = pwdTxt.getPassword();
    final char[] confirmAdminPassword = confirmPwdTxt.getPassword();

    if (!UIUtil.isValidAdminPassword(adminPassword)) {
      Messages.showErrorDialog("Admin password entered is not valid.",
          "Invalid Password");
      return false;
    }

    if (!Arrays.equals(adminPassword,confirmAdminPassword)) {
      Messages.showErrorDialog("Admin password mismatch error",
          "Confirm Admin password must match Admin password");
      return false;
    }

    Arrays.fill(adminPassword, ' ');
    Arrays.fill(confirmAdminPassword, ' ');
    return true;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return mainPanel;
  }
}
