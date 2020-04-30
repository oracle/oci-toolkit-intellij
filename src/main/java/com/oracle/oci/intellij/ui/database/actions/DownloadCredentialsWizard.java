package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseWallet;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.ADBConstants;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Map;

public class DownloadCredentialsWizard extends DialogWrapper {
  private JPanel mainPanel;
  private JPanel descriptionPanel;
  private JPanel centerPanel;
  private JPanel walletPanel;
  private JPanel credentialsPanel;
  private JPasswordField passwordPasswordField;
  private JPasswordField confirmPasswordPasswordField;
  private JTextField walletLocationTextField;
  private JButton browseButton;
  private JPanel walletTypePanel;
  private JLabel walletTypeLbl;
  private JComboBox walletTypeCmb;
  private JButton rotateWalletBtn;
  private JLabel rotateWalletLbl;

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

  private final String rotationInProgressMsg =
        "The wallet rotation process takes a few minutes. During the wallet rotation, a new wallet is generated."
            + "\n" + "You cannot perform a wallet download during the rotation process. Existing connections to database"
            + "\n" + "will be terminated, and will need to be reestablished using the new wallet. Please try after sometime.";

  private final String rotationMsg1 = "ENTER THE DATABASE NAME (%s) TO CONFIRM THE %s ROTATION";

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;
  private AutonomousDatabaseWallet regionalWallet = null;
  private AutonomousDatabaseWallet instanceWallet = null;
  private boolean isDedicatedInstance;

  protected DownloadCredentialsWizard(
      AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    this.isDedicatedInstance = (
        autonomousDatabaseSummary.getIsDedicated() != null
            && autonomousDatabaseSummary.getIsDedicated());
    init();

    setTitle("Download Client Credentials");
    setOKButtonText("Download");
    walletLocationTextField.setEditable(false);
    if (isDedicatedInstance) {
      walletTypeCmb.setEnabled(false);
      rotateWalletBtn.setEnabled(false);
    }
    else {
      initializeWalletTypes();
    }

    browseButton.addActionListener((e) -> {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.setMultiSelectionEnabled(false);
      fileChooser.setDialogTitle("Select a folder to download the wallet");
      if (fileChooser.showOpenDialog(mainPanel)
          == JFileChooser.APPROVE_OPTION) {
        File downloadFolder = fileChooser.getSelectedFile();
        //Messages.showInfoMessage("Selected file : " + downloadFolder.getAbsolutePath(), "Secetechg");
        walletLocationTextField.setText(downloadFolder.getAbsolutePath());
      }
    });
  }

  private void initializeWalletTypes() {
    final Map<String, AutonomousDatabaseWallet> walletTypes = ADBInstanceClient.getInstance().getWalletType(autonomousDatabaseSummary);
    regionalWallet = walletTypes.get(ADBConstants.REGIONAL_WALLET);
    instanceWallet = walletTypes.get(ADBConstants.INSTANCE_WALLET);

    // Check if wallet rotation is in progress.
    if ((instanceWallet != null && instanceWallet.getLifecycleState()
        .equals(AutonomousDatabaseWallet.LifecycleState.Updating)) ||
        (regionalWallet != null && regionalWallet.getLifecycleState()
            .equals(AutonomousDatabaseWallet.LifecycleState.Updating))) {
      Messages.showInfoMessage(rotationInProgressMsg, "Wallet Rotation in Progress");
      close(DialogWrapper.CLOSE_EXIT_CODE);
    }

    walletTypeCmb.addItem(ADBConstants.INSTANCE_WALLET);
    walletTypeCmb.addItem(ADBConstants.REGIONAL_WALLET);
    walletTypeCmb.addItemListener((e) -> {
      final String selectedWalletType = (String) walletTypeCmb
          .getSelectedItem();
      String rotationTimeStr = "";
      if (selectedWalletType.equals(ADBConstants.INSTANCE_WALLET))
        rotationTimeStr = (instanceWallet != null
            && instanceWallet.getTimeRotated() != null) ?
            instanceWallet.getTimeRotated().toGMTString() :
            "Not Available";
      else
        rotationTimeStr = (regionalWallet != null
            && regionalWallet.getTimeRotated() != null) ?
            regionalWallet.getTimeRotated().toGMTString() :
            "Not Available";

      rotateWalletLbl.setText("Wallet Last Rotated Time : " + rotationTimeStr);
      rotateWalletLbl.updateUI();

    });
    walletTypeCmb.setSelectedIndex(0);
    final String rotationTimeStr =
        (instanceWallet != null && instanceWallet.getTimeRotated() != null) ?
            instanceWallet.getTimeRotated().toGMTString() : "Not Available";
    rotateWalletLbl.setText("Wallet Last Rotated Time : " + rotationTimeStr);

    rotateWalletBtn.addActionListener((e) -> {
      final String selectedWalletType = (String) walletTypeCmb
          .getSelectedItem();
      final String msg;
      if (selectedWalletType.equals(ADBConstants.INSTANCE_WALLET))
        msg = rotationMsgInstanceWallet + String
            .format(rotationMsg1, autonomousDatabaseSummary.getDbName(),
                "INSTANCE WALLET");
      else
        msg = rotationMsgRegionalWallet + String
            .format(rotationMsg1, autonomousDatabaseSummary.getDbName(),
                "REGIONAL WALLET");

      final String input = Messages
          .showInputDialog(msg, "Rotate Wallet", Messages.getQuestionIcon());
      if (autonomousDatabaseSummary.getDbName().equals(input)) {
        ADBInstanceClient.getInstance()
            .rotateWallet(autonomousDatabaseSummary.getId(),
                selectedWalletType);
        Messages.showInfoMessage("Wallet rotated successfully", "Success");
        close(DialogWrapper.CLOSE_EXIT_CODE);
      }
      else {
        Messages.showErrorDialog(
            "Entered database name does not match with existing name.",
            "Database name mismatch error");
      }
    });
  }

  public void doOKAction() {
    if (!isValidPassword())
      return;

    final String dirPath = walletLocationTextField.getText().trim();
    if (!isValidWalletDir(dirPath))
      return;

    final String adminPassword = new String(
        passwordPasswordField.getPassword());
    final String selectedWalletType = (String) walletTypeCmb.getSelectedItem();
    final String walletDirectory =
        dirPath + File.separator + "Wallet_" + autonomousDatabaseSummary
            .getDbName();

    final Runnable nonblockingDownload = () -> {
      try {
        ADBInstanceClient.getInstance()
            .downloadWallet(autonomousDatabaseSummary, selectedWalletType,
                adminPassword, walletDirectory);
        ApplicationManager.getApplication().invokeLater(() -> UIUtil
            .fireSuccessNotification(
                "<html>Wallet downloaded successfully." + "<br>Path : "
                    + walletDirectory + "</html>"));
      }
      catch (Exception e) {
        ApplicationManager.getApplication().invokeLater(() -> UIUtil
            .fireErrorNotification("Wallet download failed : " + e.getMessage()));
      }
    };

    // Do this in background
    UIUtil.fetchAndUpdateUI(nonblockingDownload, null);

    close(DialogWrapper.OK_EXIT_CODE);
  }

  private boolean isValidPassword() {
    final String password = new String(passwordPasswordField.getPassword());
    final String confirmPassword = new String(
        confirmPasswordPasswordField.getPassword());

    if (password == null || password.trim().equals("")) {
      Messages.showErrorDialog("Wallet password cannot be empty", " Error");
      return false;
    }
    else if (!password.equals(confirmPassword)) {
      Messages.showErrorDialog("Confirm wallet password must match wallet password", "Error");
      return false;
    }
    return true;
  }

  private boolean isValidWalletDir(final String dirPath) {
    if (dirPath == null || dirPath.trim().equals("")) {
      Messages.showErrorDialog("Wallet directory path cannot be empty", "Error");
      return false;
    }
    return true;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return mainPanel;
  }
}
