/*
  Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.LicenseModel;
import com.oracle.bmc.database.model.UpdateAutonomousDatabaseDetails;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.ui.common.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class UpdateLicenseDialog extends DialogWrapper {
  private JPanel chooseLicenseTypePanel;
  private JRadioButton bringYourOwnLicenseRadioButton;
  private JRadioButton licenseIncludedRadioButton;

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;

  protected UpdateLicenseDialog(
          AutonomousDatabaseSummary autonomousDatabaseSummary){
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;

    init();
    setTitle("Update License Type");
    setOKButtonText("Save Changes");
    chooseLicenseTypePanel.setPreferredSize(new Dimension(300, 65));

    // Set the border line text for scale panel.
    final String licenseHelpWebLink =
            "https://docs.cloud.oracle.com/iaas/Content/Database/Tasks/adbmanaging.htm#updatelicense";
    final String scalePanelBorderText = "<html>Choose a license type. " +
            "<a href=" + "\"" + licenseHelpWebLink + "\"" + ">HELP</a></html";
    final Border borderLine = BorderFactory
            .createTitledBorder(scalePanelBorderText);
    chooseLicenseTypePanel.setBorder(borderLine);
    UIUtil.createWebLink(chooseLicenseTypePanel, licenseHelpWebLink);

    final ButtonGroup licenseTypeButtonGroup = new ButtonGroup();
    licenseTypeButtonGroup.add(bringYourOwnLicenseRadioButton);
    licenseTypeButtonGroup.add(licenseIncludedRadioButton);

    final LicenseModel licenseModel = autonomousDatabaseSummary.getLicenseModel();
    if (licenseModel.equals(LicenseModel.LicenseIncluded)) {
      licenseIncludedRadioButton.setSelected(true);
    } else {
      bringYourOwnLicenseRadioButton.setSelected(true);
    }
  }

  @Override
  public void doOKAction(){
    final UpdateAutonomousDatabaseDetails.LicenseModel licenseModel =
            licenseIncludedRadioButton.isSelected() ?
                    UpdateAutonomousDatabaseDetails.LicenseModel.LicenseIncluded :
                    UpdateAutonomousDatabaseDetails.LicenseModel.BringYourOwnLicense;

    final Runnable nonblockingUpdate = () -> {
      try {
        OracleCloudAccount.getInstance().getDatabaseClient()
                .updateLicenseType(autonomousDatabaseSummary, licenseModel);
        UIUtil.fireNotification(NotificationType.INFORMATION, "License model updated successfully.", "License");
      } catch (Exception ex) {
        UIUtil.fireNotification(NotificationType.ERROR, "Failed to update license model : " + ex.getMessage(), null);
      }
    };

    // Do this in background
    UIUtil.executeAndUpdateUIAsync(nonblockingUpdate, null);
    close(DialogWrapper.OK_EXIT_CODE);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel(){
    return new JBScrollPane(chooseLicenseTypePanel);
  }
}
