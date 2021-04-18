/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class AdminPasswordWizard extends DialogWrapper {
  private JPanel mainPanel;
  private JPanel passwordMainPanel;
  private JTextField userNameTextField;
  private JPasswordField pwdTxt;
  private JPasswordField confirmPwdTxt;
  private JLabel usernameLabel;
  private JLabel passwordHelpLabel;
  private JPanel passwordFieldsPanel;

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;

  protected AdminPasswordWizard(
      AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    init();
    setTitle("Administrator Password");
    setOKButtonText("Update");
    userNameTextField.setText("ADMIN");

    passwordMainPanel.setPreferredSize(new Dimension(500,150));
    UIUtil.makeWebLink(passwordHelpLabel,
            "https://docs.oracle.com/en-us/iaas/Content/Database/Tasks/adbmanaging.htm#setadminpassword");
  }

  @Override protected void doOKAction() {
    if (!Arrays.equals(pwdTxt.getPassword(),confirmPwdTxt.getPassword())) {
      Messages.showErrorDialog("Error. Password mismatch",
              "Confirmation must match password.");
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
            .fireNotification(NotificationType.INFORMATION, "Admin Password Updated Successfully."));
      }
      catch (Exception e) {
        ApplicationManager.getApplication().invokeLater(() -> UIUtil
            .fireNotification(NotificationType.ERROR, "Admin Password Update failed : " + e.getMessage()));
      }
    };

    // Do this in background
    UIUtil.fetchAndUpdateUI(nonblockingUpdate, null);
    close(DialogWrapper.OK_EXIT_CODE);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return mainPanel;
  }
}
