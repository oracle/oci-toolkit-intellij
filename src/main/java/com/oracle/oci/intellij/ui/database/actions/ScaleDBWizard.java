package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.account.PreferencesWrapper;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.ADBConstants;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ScaleDBWizard extends DialogWrapper {
  private JPanel mainPanel;
  private JSpinner cpuCountSpnr;
  private JSpinner storageSpnr;
  private JCheckBox autoScalingChkBox;

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;

  protected ScaleDBWizard(AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    init();
    setTitle("ADB Scale Up / Down");
    setOKButtonText("Update");
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
          UIUtil.fireSuccessNotification("Scaleup or Scaledown completed successfully.");
          PreferencesWrapper.fireADBInstanceUpdateEvent("Scale");
        });
      }
      catch (Exception e) {
        ApplicationManager.getApplication()
            .invokeLater(() -> UIUtil.fireErrorNotification("Scaleup or Scaledown failed : " + e.getMessage()));
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
