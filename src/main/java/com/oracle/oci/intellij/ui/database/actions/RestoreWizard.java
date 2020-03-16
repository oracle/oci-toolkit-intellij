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

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;
  private List<AutonomousDatabaseBackupSummary> backupList;

  protected RestoreWizard(
      final AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    init();
    setTitle("Restore from backup");
    setOKButtonText("Restore");
    SpinnerDateModel dateModel1 = new SpinnerDateModel();
    SpinnerDateModel dateModel2 = new SpinnerDateModel();
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
    fetchRestoreList();
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
            .fireSuccessNotification("ADB Instance Successfully Restored.."));
      }
      catch (Exception e) {
        ApplicationManager.getApplication().invokeLater(() -> UIUtil
            .fireErrorNotification("Restore Failed : " + e.getMessage()));
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
    System.out.println("Fetching the restore list : " + Thread.currentThread().getName());
    final DefaultTableModel model = (DefaultTableModel) backupListTable
        .getModel();
    model.setRowCount(0);
    model.addRow(new Object[] { "Loading data. Please wait..", "", "" });
    final Runnable fetch = () -> {
      try {
        backupList = ADBInstanceClient.getInstance()
            .getBackupList(autonomousDatabaseSummary);
        filterList();
        System.out.println("Fetch completed. Backup Size : " + backupList.size());
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    };

    UIUtil.fetchAndUpdateUI(fetch, null);
  }

  private void filterList() {
    System.out.println("Updating the UI " + backupList);
    final DefaultTableModel model = (DefaultTableModel) backupListTable
        .getModel();
    model.setRowCount(0);
    if(backupList != null && backupList.size() > 0) {
      final Date fromDate = (Date) spinner1.getValue();
      final Date toDate = (Date) spinner2.getValue();

      for (AutonomousDatabaseBackupSummary backup : backupList) {
        if (backup.getTimeEnded().compareTo(fromDate) >= 0
            && backup.getTimeEnded().compareTo(toDate) <= 0) {
          model.addRow(new Object[] { backup.getDisplayName(),
              backup.getLifecycleState().getValue(),
              backup.getType().getValue() });
        }
      }
    }
    else {
      model.addRow(new Object[] { "No Data Found.", "", "" });
    }
    System.out.println("Updating the UI");
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return mainPanel;
  }
}
