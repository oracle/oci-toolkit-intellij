/*
  Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseWallet;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.common.AutonomousDatabaseConstants;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class DownloadCredentialsDialog extends DialogWrapper {
  private JPanel mainPanel;
  private JPasswordField passwordField;
  private JPasswordField confirmPasswordField;
  private JTextField walletLocationTextField;
  private JButton browseButton;
  private JComboBox<String> walletTypeComboBox;
  private JButton rotateWalletButton;
  private JLabel rotateWalletLabel;
  private JPanel walletLocationPanel;
  private JPanel downloadWalletPanel;
  private JCheckBox downloadWalletCheckBox;
  private JLabel walletLocationLabel;
  private JLabel passwordLabel;
  private JLabel confirmPasswordLabel;
  private JLabel rotateWalletHelpLabel;

  private final String rotationMsgInstanceWallet =
        "Rotating the instance wallet invalidates the certificate keys associated with the existing instance wallet"+"\n"
            +"and generates a new wallet. The existing regional wallet will continue to work. Existing connections"+"\n"
            +"to the Autonomous Database that use the old instance wallet will be terminated over a period of"+"\n"
            +"time, and will need to be reestablished using the new wallet. If you need to terminate all existing"+"\n"
            +"connections to a database immediately, stop and restart the database."+"\n\n";

  private final String rotationMsgRegionalWallet =
        "Are you sure you want to rotate the regional wallet?"+"\n\n"
            +"Rotating the regional wallet will invalidate all existing regional and instance wallets in the region."+"\n"
            +"Certificate keys associated with the existing wallets in the region will be invalidated. All connections"+"\n"
            +"to databases in the region that use the existing regional wallet will be terminated over a period of time."+"\n"
            +"If you need to terminate all existing connections to a database immediately, stop and restart the database."+"\n\n";

  private final String rotationMsg1 = "Enter the currently selected database name (%s) to confirm the %s rotation";

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;

  protected DownloadCredentialsDialog(
      AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    init();

    setTitle("Download Client Credentials (Wallet)");
    setOKButtonText("Download");
    mainPanel.setPreferredSize(new Dimension(850, 300));

    // Add download wallet help web link.
    final String downloadWalletHelpWebLink =
            "https://docs.oracle.com/en-us/iaas/Content/Database/Tasks/adbconnecting.htm#access";
    final String downloadWalletHelpText = "<html>Download Wallet  " +
            "<a href=" + "\"" + downloadWalletHelpWebLink + "\"" + ">HELP</a></html";
    final Border borderLine = BorderFactory
            .createTitledBorder(downloadWalletHelpText);
    downloadWalletPanel.setBorder(borderLine);
    UIUtil.createWebLink(downloadWalletPanel, downloadWalletHelpWebLink);

    // Add rotate wallet help web link.
    final String rotateWalletHelpWebLink =
            "https://docs.oracle.com/en-us/iaas/Content/Database/Tasks/adbconnecting.htm#AboutDownloadingClientCredentials";
    final String rotateWalletHelpText = "<html>" +
            "<a href=" + "\"" + rotateWalletHelpWebLink + "\"" + ">HELP</a></html";
    rotateWalletHelpLabel.setText(rotateWalletHelpText);
    UIUtil.createWebLink(rotateWalletHelpLabel, rotateWalletHelpWebLink);

    walletLocationTextField.setEditable(false);

    if (autonomousDatabaseSummary.getIsDedicated()) {
      walletTypeComboBox.setEnabled(false);
      rotateWalletButton.setEnabled(false);
    }
    else {
      initializeWalletTypes();
    }

    browseButton.addActionListener((e) -> {
      final JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.setMultiSelectionEnabled(false);
      fileChooser.setDialogTitle("Choose a directory to download wallet");

      if (fileChooser.showDialog(mainPanel, "Save")
          == JFileChooser.APPROVE_OPTION) {
        final File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile.isDirectory()) {
          walletLocationTextField.setText(selectedFile.getAbsolutePath());
        } else {
          walletLocationTextField.setText(fileChooser.getCurrentDirectory().getPath());
        }
      }
    });

    downloadWalletCheckBox.addItemListener((event) -> {
      boolean isSelected = (event.getStateChange() == ItemEvent.SELECTED);

      walletLocationPanel.setEnabled(isSelected);
      walletLocationLabel.setEnabled(isSelected);
      walletLocationTextField.setEnabled(isSelected);
      browseButton.setEnabled(isSelected);

      downloadWalletPanel.setEnabled(isSelected);
      passwordLabel.setEnabled(isSelected);
      passwordField.setEnabled(isSelected);
      confirmPasswordLabel.setEnabled(isSelected);
      confirmPasswordField.setEnabled(isSelected);

      if (!isSelected) {
        walletLocationTextField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
      }
    });
  }

  private void initializeWalletTypes() {
    final Map<String, AutonomousDatabaseWallet> walletTypes =
            OracleCloudAccount.getInstance().getDatabaseClient().getWalletType(autonomousDatabaseSummary);

    AutonomousDatabaseWallet regionalWallet = walletTypes.get(AutonomousDatabaseConstants.REGIONAL_WALLET);
    AutonomousDatabaseWallet instanceWallet = walletTypes.get(AutonomousDatabaseConstants.INSTANCE_WALLET);

    // Check if wallet rotation is in progress.
    final Function<AutonomousDatabaseWallet, Boolean> isWalletRotating = (wallet) -> wallet != null &&
            wallet.getLifecycleState().equals(AutonomousDatabaseWallet.LifecycleState.Updating);

    if (isWalletRotating.apply(regionalWallet) ||
            isWalletRotating.apply(instanceWallet)) {
      final String rotationInProgressMsg = "The wallet rotation process takes a few minutes. During the wallet rotation, a new wallet is generated."
              + "\n" + "You cannot perform a wallet download during the rotation process. Existing connections to database"
              + "\n" + "will be terminated, and will need to be reestablished using the new wallet. Please try after sometime.";
      Messages.showInfoMessage(rotationInProgressMsg, "Wallet Rotation in Progress");
      close(DialogWrapper.CLOSE_EXIT_CODE);
    }

    walletTypeComboBox.addItem(AutonomousDatabaseConstants.INSTANCE_WALLET);
    walletTypeComboBox.addItem(AutonomousDatabaseConstants.REGIONAL_WALLET);

    walletTypeComboBox.addItemListener((e) -> {
      final String selectedWalletType = (String) walletTypeComboBox.getSelectedItem();

      String rotationTimeStr;
      if (selectedWalletType !=null && selectedWalletType.equals(AutonomousDatabaseConstants.INSTANCE_WALLET)) {
        rotationTimeStr = (instanceWallet != null
                && instanceWallet.getTimeRotated() != null) ?
                instanceWallet.getTimeRotated().toString() :
                "-";
      } else {
        rotationTimeStr = (regionalWallet != null
                && regionalWallet.getTimeRotated() != null) ?
                regionalWallet.getTimeRotated().toString() :
                "-";
      }

      rotateWalletLabel.setText(rotationTimeStr);
      rotateWalletLabel.updateUI();
    });

    walletTypeComboBox.setSelectedIndex(0);
    final String rotationTimeStr =
        (instanceWallet != null && instanceWallet.getTimeRotated() != null) ?
            instanceWallet.getTimeRotated().toString() : "-";
    rotateWalletLabel.setText(rotationTimeStr);

    rotateWalletButton.addActionListener((e) -> {
      final String selectedWalletType = (String) walletTypeComboBox
          .getSelectedItem();

      final String msg;
      if (selectedWalletType != null && selectedWalletType.equals(AutonomousDatabaseConstants.INSTANCE_WALLET)) {
        msg = rotationMsgInstanceWallet + String
                .format(rotationMsg1, autonomousDatabaseSummary.getDbName(),
                        selectedWalletType);
      } else {
        msg = rotationMsgRegionalWallet + String
                .format(rotationMsg1, autonomousDatabaseSummary.getDbName(),
                        selectedWalletType);
      }

      final String input = Messages
          .showInputDialog(msg, "Rotate Wallet", Messages.getQuestionIcon());

      // input will be null when 'Cancel' button is clicked.
      if (input != null) {
        if (autonomousDatabaseSummary.getDbName().equals(input)) {
          OracleCloudAccount.getInstance().getDatabaseClient()
                  .rotateWallet(autonomousDatabaseSummary.getId(), selectedWalletType);

          Messages.showInfoMessage("Wallet rotated successfully", "Success");
          close(DialogWrapper.CLOSE_EXIT_CODE);
        } else {
          Messages.showErrorDialog(
                  "Database name does not match the existing value.",
                  "Database name mismatch");
        }
      }
    });
  }

  public void doOKAction() {
    final String dirPath = walletLocationTextField.getText().trim();
    if (!isValidPassword() || !isValidWalletDir(dirPath)) {
      return;
    }

    final String selectedWalletType = (String) walletTypeComboBox.getSelectedItem();
    final String walletDirectory = dirPath + File.separator + "Wallet_"
            + autonomousDatabaseSummary.getDbName();

    final Runnable nonblockingDownload = () -> {
      try {
        OracleCloudAccount.getInstance().getDatabaseClient()
                .downloadWallet(autonomousDatabaseSummary, selectedWalletType, String.valueOf(passwordField.getPassword()), walletDirectory);
        UIUtil.fireNotification(NotificationType.INFORMATION,
                        "<html>Wallet downloaded successfully.</html>", "Wallet download");
      }
      catch (Exception e) {
        UIUtil.fireNotification(NotificationType.ERROR,"Wallet download failed : " + e.getMessage(), null);
      }
    };

    // Do this in background
    UIUtil.executeAndUpdateUIAsync(nonblockingDownload, null);
    close(DialogWrapper.OK_EXIT_CODE);
  }

  private boolean isValidPassword() {
    final char[] password = passwordField.getPassword();
    final char[] confirmPassword = confirmPasswordField.getPassword();

    if (password == null || password.length == 0) {
      Messages.showErrorDialog("Wallet password cannot be empty", " Error");
      return false;
    }
    else if (!Arrays.equals(password, confirmPassword)) {
      Messages.showErrorDialog("Confirmation must match password", "Error");
      return false;
    }
    Arrays.fill(password,' ');
    Arrays.fill(confirmPassword, ' ');
    return true;
  }

  private boolean isValidWalletDir(final String dirPath) {
    if (dirPath == null || dirPath.trim().equals("")) {
      Messages.showErrorDialog("Wallet location cannot be empty", "Error");
      return false;
    }

    return true;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return new JBScrollPane(mainPanel);
  }
}
