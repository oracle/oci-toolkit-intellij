/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.account.ServicePreferences;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.ADBConstants;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ScaleDBWizard extends DialogWrapper {
  private JPanel mainPanel;
  private JSpinner cpuCountSpnr;
  private JSpinner storageSpnr;
  private JCheckBox autoScalingChkBox;
  private JPanel scalePanel;

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;

  protected ScaleDBWizard(AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    init();
    setTitle("Scale Up/Down");
    setOKButtonText("Update");

    // Set the border line text for scale panel.
    final String scaleHelpWebLink =
            "https://docs.oracle.com/en-us/iaas/Content/Database/Tasks/adbmanaging.htm#scale";
    final String scalePanelBorderText = "<html>Scale Up/Down. " +
            "<a href=" + "\"" + scaleHelpWebLink + "\"" + ">HELP</a></html";
    final Border borderLine = BorderFactory
            .createTitledBorder(scalePanelBorderText);
    scalePanel.setBorder(borderLine);
    UIUtil.makeWebLink(scalePanel, scaleHelpWebLink);
    scalePanel.setPreferredSize(new Dimension(400, 125));

    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    cpuCountSpnr.setModel(
        new SpinnerNumberModel(ADBConstants.CPU_CORE_COUNT_DEFAULT,
            ADBConstants.CPU_CORE_COUNT_MIN, ADBConstants.CPU_CORE_COUNT_MAX,
            ADBConstants.CPU_CORE_COUNT_INCREMENT));
    storageSpnr.setModel(
        new SpinnerNumberModel(ADBConstants.STORAGE_IN_TB_DEFAULT,
            ADBConstants.STORAGE_IN_TB_MIN, ADBConstants.STORAGE_IN_TB_MAX,
            ADBConstants.STORAGE_IN_TB_INCREMENT));
    cpuCountSpnr.setValue(autonomousDatabaseSummary.getCpuCoreCount());
    storageSpnr.setValue(autonomousDatabaseSummary.getDataStorageSizeInTBs());
    autoScalingChkBox
        .setSelected(autonomousDatabaseSummary.getIsAutoScalingEnabled());
  }

  @Override
  protected void doOKAction() {

    final Runnable nonblockingUpdate = () -> {
      try {
        ADBInstanceClient.getInstance()
            .scaleUpDownInstance(autonomousDatabaseSummary,
                (int) cpuCountSpnr.getValue(), (int) storageSpnr.getValue(),
                autoScalingChkBox.isSelected());
        ApplicationManager.getApplication().invokeLater(() -> {
          UIUtil.fireNotification(NotificationType.INFORMATION, "Scale up or scale down completed successfully.");
          ServicePreferences.fireADBInstanceUpdateEvent("Scale");
        });
      }
      catch (Exception e) {
        ApplicationManager.getApplication()
            .invokeLater(() -> UIUtil.fireNotification(NotificationType.ERROR, "Scale up or scale down failed : " + e.getMessage()));
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
