/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.common.AutonomousDatabaseConstants;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ScaleUpDownDialog extends DialogWrapper {
  private JPanel mainPanel;
  private JSpinner cpuCountSpinner;
  private JSpinner storageSpinner;
  private JCheckBox autoScalingChkBox;
  private JPanel scalePanel;

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;

  protected ScaleUpDownDialog(AutonomousDatabaseSummary autonomousDatabaseSummary) {
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
    UIUtil.createWebLink(scalePanel, scaleHelpWebLink);
    scalePanel.setPreferredSize(new Dimension(400, 125));

    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    cpuCountSpinner.setModel(
        new SpinnerNumberModel(AutonomousDatabaseConstants.CPU_CORE_COUNT_DEFAULT,
            AutonomousDatabaseConstants.CPU_CORE_COUNT_MIN, AutonomousDatabaseConstants.CPU_CORE_COUNT_MAX,
            AutonomousDatabaseConstants.CPU_CORE_COUNT_INCREMENT));
    storageSpinner.setModel(
        new SpinnerNumberModel(AutonomousDatabaseConstants.STORAGE_IN_TB_DEFAULT,
            AutonomousDatabaseConstants.STORAGE_IN_TB_MIN, AutonomousDatabaseConstants.STORAGE_IN_TB_MAX,
            AutonomousDatabaseConstants.STORAGE_IN_TB_INCREMENT));
    cpuCountSpinner.setValue(autonomousDatabaseSummary.getCpuCoreCount());
    storageSpinner.setValue(autonomousDatabaseSummary.getDataStorageSizeInTBs());
    autoScalingChkBox
        .setSelected(autonomousDatabaseSummary.getIsAutoScalingEnabled());
  }

  @Override
  protected void doOKAction() {

    final Runnable nonblockingUpdate = () -> {
      try {
        OracleCloudAccount.getInstance().getDatabaseClient().scaleUpDownInstance(autonomousDatabaseSummary,
                (int) cpuCountSpinner.getValue(), (int) storageSpinner.getValue(),
                autoScalingChkBox.isSelected());
        UIUtil.fireNotification(NotificationType.INFORMATION, "Scale up / down completed successfully.", "Scale");
      }
      catch (Exception e) {
        UIUtil.fireNotification(NotificationType.ERROR, "Scale up / down failed : " + e.getMessage(), null);
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
