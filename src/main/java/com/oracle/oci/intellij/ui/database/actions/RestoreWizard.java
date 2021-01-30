/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.oracle.bmc.database.model.AutonomousDatabaseBackupSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.util.List;

public class RestoreWizard extends DialogWrapper {
  private static final String[] RESTORE_COLUMN_NAMES = new String[] {
      "Backup Name", "State", "Type" };
  private JPanel mainPanel;
  private JPanel datePanel;
  private JPanel tablePanel;
  private JTable backupListTable;
  private JSpinner spinner1;
  private JSpinner spinner2;
  private JButton refreshButton;
  private SpinnerDateModel dateModel1;
  private SpinnerDateModel dateModel2;
  private final AutonomousDatabaseSummary autonomousDatabaseSummary;
  private List<AutonomousDatabaseBackupSummary> backupList;

  protected RestoreWizard(final AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    setTitle("Restore from backup");
    setOKButtonText("Restore");
    dateModel1 = new SpinnerDateModel();
    dateModel2 = new SpinnerDateModel();
    spinner1.setModel(dateModel1);
    spinner2.setModel(dateModel2);
    spinner1.setEditor(new JSpinner.DateEditor(spinner1, "dd/MMM/yyyy"));
    spinner2.setEditor(new JSpinner.DateEditor(spinner2, "dd/MMM/yyyy"));
    final DefaultTableModel model = new DefaultTableModel(RESTORE_COLUMN_NAMES,
        0);
    backupListTable.setModel(model);
    Date today = new Date();
    dateModel1.setValue(DateUtils.addDays(today, -7));
    dateModel2.setValue(today);
    refreshButton.addActionListener((e) -> filterList());
    init();
  }

  @Override
  protected void doOKAction() {
    final int selectedRow = backupListTable.getSelectedRow();
    if (selectedRow == -1) {
      Messages.showErrorDialog("No Restore Item Selected.", "Error");
      return;
    }
    final Date restoreTimestamp = getSelectedTimestamp(selectedRow);
    final Runnable nonblockingUpdate = () -> {
      try {
        ADBInstanceClient.getInstance()
            .restore(autonomousDatabaseSummary.getId(), restoreTimestamp);
        ApplicationManager.getApplication().invokeLater(() -> UIUtil
            .fireSuccessNotification("ADB Instance successfully restored."));
      }
      catch (Exception e) {
        ApplicationManager.getApplication().invokeLater(() -> UIUtil
            .fireErrorNotification("Failed to restore : " + e.getMessage()));
      }
    };

    // Do this in background
    UIUtil.fetchAndUpdateUI(nonblockingUpdate, null);

    close(DialogWrapper.OK_EXIT_CODE);
  }

  private Date getSelectedTimestamp(int selectedRow) {
    final String backupName = (String) backupListTable
        .getValueAt(selectedRow, 0);
    for (AutonomousDatabaseBackupSummary backup : backupList) {
      if (backup.getDisplayName().equalsIgnoreCase(backupName))
        return backup.getTimeEnded();
    }
    return null;
  }

  private void fetchRestoreList() {
    // TODO : Need to convert it to asyc model. Currently leaving it as it is.
    // Coverting to async model using UIUtil.fetchAndUpdateUI is not working for some unknown reason
    // have to debug it later.
    final DefaultTableModel model = (DefaultTableModel) backupListTable.getModel();
    model.setRowCount(0);
    updateActionState(false);
    UIUtil.setStatus("Loading the restore data. Please wait..");
    try {
      backupList = ADBInstanceClient.getInstance().getBackupList(autonomousDatabaseSummary);
      filterList();
    }
    catch(Exception e) {
      ApplicationManager.getApplication().invokeLater( () ->
          UIUtil.fireErrorNotification("Unable to get the restore list : " + e.getMessage()));
    }
  }

  private void filterList() {
    try {
      final DefaultTableModel model = (DefaultTableModel) backupListTable
          .getModel();
      model.setRowCount(0);
      if(backupList != null && backupList.size() > 0) {
        //UIUtil.setStatus("Found " + backupList.size() +" restore " + (backupList.size() == 1 ? "point." : "points."));
        final Date fromDate = (Date) dateModel1.getDate();
        final Date toDate = (Date) dateModel2.getDate();
        int hiddingCount = 0;
        for (AutonomousDatabaseBackupSummary backup : backupList) {
          if (backup.getTimeEnded().compareTo(fromDate) >= 0
              && backup.getTimeEnded().compareTo(toDate) <= 0) {
            model.addRow(new Object[] { backup.getDisplayName(),
                backup.getLifecycleState().getValue(),
                backup.getType().getValue() });
          }
          else {
            hiddingCount++;
          }
        }
        UIUtil.setStatus("Found " + backupList.size() +" restore " + (backupList.size() == 1 ? "point." : "points.")
            + " Shown : " + (backupList.size() - hiddingCount) + " Hidden : " + hiddingCount);
      }
      else {
        UIUtil.setStatus("No restore data found.");
      }
    }
    catch(Exception ex) {
      UIUtil.fireErrorNotification("Filter List: Unable to get the restore list : " + ex.getMessage());
    }
    updateActionState(true);
  }

  private void updateActionState(final boolean enabled) {
    refreshButton.setEnabled(enabled);
    spinner1.setEnabled(enabled);
    spinner2.setEnabled(enabled);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    fetchRestoreList();
    return mainPanel;
  }
}
