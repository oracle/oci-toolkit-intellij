package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.LicenseModel;
import com.oracle.bmc.database.model.UpdateAutonomousDatabaseDetails;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class UpdateLicenseWizard extends DialogWrapper {

  private JPanel mainPanel;
  private JRadioButton alreadyRBtn;
  private JRadioButton newLicenseRBtn;
  private ButtonGroup bGroup;

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;

  protected UpdateLicenseWizard(
      AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    init();
    setTitle("ADB Update License");
    setOKButtonText("Update");
    bGroup = new ButtonGroup();
    bGroup.add(alreadyRBtn);
    bGroup.add(newLicenseRBtn);
    LicenseModel licenseModel = autonomousDatabaseSummary.getLicenseModel();
    if (licenseModel.equals(LicenseModel.LicenseIncluded))
      newLicenseRBtn.setSelected(true);
    else
      alreadyRBtn.setSelected(true);

  }

  @Override public void doOKAction() {
    final UpdateAutonomousDatabaseDetails.LicenseModel licenseModel =
        newLicenseRBtn.isSelected() ?
            UpdateAutonomousDatabaseDetails.LicenseModel.LicenseIncluded :
            UpdateAutonomousDatabaseDetails.LicenseModel.BringYourOwnLicense;

    final Runnable nonblockingUpdate = () -> {
      try {
        ADBInstanceClient.getInstance()
            .updateLicenseType(autonomousDatabaseSummary, licenseModel);
        ApplicationManager.getApplication().invokeLater(() -> UIUtil
            .fireSuccessNotification("License model successfully updated."));
      }
      catch (Exception e) {
        ApplicationManager.getApplication().invokeLater(
            () -> UIUtil.fireErrorNotification("Failed to update the license model : " + e.getMessage()));
      }
    };

    // Do this in background
    UIUtil.fetchAndUpdateUI(nonblockingUpdate, null);

    close(DialogWrapper.OK_EXIT_CODE);
  }

  @Nullable @Override protected JComponent createCenterPanel() {
    return mainPanel;
  }
}
