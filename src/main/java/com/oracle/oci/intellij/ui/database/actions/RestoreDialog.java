/*
  Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.oracle.bmc.database.model.AutonomousDatabaseBackupSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.ui.common.UIUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.util.List;

public class RestoreDialog extends DialogWrapper {
  private static final String[] RESTORE_COLUMN_NAMES = new String[] {
      "Backup Name", "State", "Type" };
  private JPanel mainPanel;
  private JTable backupListTable;
  private JSpinner spinner1;
  private JSpinner spinner2;
  private JButton refreshButton;
  private final SpinnerDateModel dateModel1;
  private final SpinnerDateModel dateModel2;
  private final AutonomousDatabaseSummary autonomousDatabaseSummary;
  private List<AutonomousDatabaseBackupSummary> backupList;

  protected RestoreDialog(final AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    setTitle("Restore from backup");
    setOKButtonText("Restore");

    dateModel1 = new SpinnerDateModel();
    spinner1.setModel(dateModel1);
    spinner1.setEditor(new JSpinner.DateEditor(spinner1, "dd/MMM/yyyy"));

    dateModel2 = new SpinnerDateModel();
    spinner2.setModel(dateModel2);
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
      Messages.showErrorDialog("No restore item selected.", "Error");
      return;
    }
    final Date restoreTimestamp = getSelectedTimestamp(selectedRow);
    final Runnable nonblockingUpdate = () -> {
      try {
        OracleCloudAccount.getInstance().getDatabaseClient()
                .restore(autonomousDatabaseSummary.getId(), restoreTimestamp);
        UIUtil.fireNotification(NotificationType.INFORMATION,"Autonomous Database Instance restored successfully.", "Restore");
      }
      catch (Exception e) {
        UIUtil.fireNotification(NotificationType.ERROR, "Failed to restore : " + e.getMessage(), null);
      }
    };

    UIUtil.executeAndUpdateUIAsync(nonblockingUpdate, null);
    close(DialogWrapper.OK_EXIT_CODE);
  }

  private Date getSelectedTimestamp(int selectedRow) {
    final String backupName = (String) backupListTable
        .getValueAt(selectedRow, 0);
    for (AutonomousDatabaseBackupSummary backup : backupList) {
      if (backup.getDisplayName().equalsIgnoreCase(backupName))
        return backup.getTimeEnded();
    }
    throw new IllegalStateException("Fatal error: Selected row is invalid");
  }

  private void fetchRestoreList() {
    final DefaultTableModel model = (DefaultTableModel) backupListTable.getModel();
    model.setRowCount(0);
    updateActionState(false);
    UIUtil.showInfoInStatusBar("Loading the restore data. Please wait.");
    backupList = OracleCloudAccount.getInstance().getDatabaseClient().getBackupList(autonomousDatabaseSummary);
    filterList();
  }

  private void filterList() {
    final DefaultTableModel model = (DefaultTableModel) backupListTable
            .getModel();
    model.setRowCount(0);
    if(backupList != null && backupList.size() > 0) {
      final Date fromDate = dateModel1.getDate();
      final Date toDate = dateModel2.getDate();
      int hidingCount = 0;
      for (AutonomousDatabaseBackupSummary backup : backupList) {
        if (backup.getTimeEnded().compareTo(fromDate) >= 0
                && backup.getTimeEnded().compareTo(toDate) <= 0) {
          model.addRow(new Object[] { backup.getDisplayName(),
                  backup.getLifecycleState().getValue(),
                  backup.getType().getValue() });
        }
        else {
          hidingCount++;
        }
      }
      UIUtil.showInfoInStatusBar("Found " + backupList.size() +" restore " + (backupList.size() == 1 ? "point." : "points.")
              + " Shown : " + (backupList.size() - hidingCount) + " Hidden : " + hidingCount);
    }
    else {
      UIUtil.showInfoInStatusBar("No restore data found.");
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
    return new JBScrollPane(mainPanel);
  }
}
