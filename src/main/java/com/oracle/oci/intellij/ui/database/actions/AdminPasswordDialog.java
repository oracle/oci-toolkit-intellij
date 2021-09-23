/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.ui.common.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class AdminPasswordDialog extends DialogWrapper {
  private JPanel mainPanel;
  private JPanel passwordMainPanel;
  private JTextField userNameTextField;
  private JPasswordField passwordTextField;
  private JPasswordField confirmPasswordText;
  private JLabel passwordHelpLabel;

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;

  protected AdminPasswordDialog(
      AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    init();
    setTitle("Administrator Password");
    setOKButtonText("Update");
    userNameTextField.setText("ADMIN");

    passwordMainPanel.setPreferredSize(new Dimension(500,150));
    UIUtil.createWebLink(passwordHelpLabel,
            "https://docs.oracle.com/en-us/iaas/Content/Database/Tasks/adbmanaging.htm#setadminpassword");
  }

  @Override protected void doOKAction() {
    if (!Arrays.equals(passwordTextField.getPassword(),
            confirmPasswordText.getPassword())) {
      Messages.showErrorDialog("ADMIN passwords don't match.",
              "Passwords mismatch");
      return;
    }

    Runnable nonblockingUpdate = () -> {
      try {
        OracleCloudAccount.getInstance().getDatabaseClient()
                .changeAdminPassword(autonomousDatabaseSummary, String.valueOf(passwordTextField.getPassword()));
        UIUtil.fireNotification(NotificationType.INFORMATION, "Admin Password Updated Successfully.", "Admin password");
      }
      catch (Exception e) {
        UIUtil.fireNotification(NotificationType.ERROR, "Failed to update Admin Password : " + e.getMessage(), null);
      }
    };

    // Do this in background
    UIUtil.executeAndUpdateUIAsync(nonblockingUpdate, null);
    close(DialogWrapper.OK_EXIT_CODE);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return new JBScrollPane(mainPanel);
  }
}
