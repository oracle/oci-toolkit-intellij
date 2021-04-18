/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.LifecycleState;
import com.oracle.oci.intellij.util.LogHandler;
import com.oracle.oci.intellij.account.AuthenticationDetails;
import com.oracle.oci.intellij.account.Identity;
import com.oracle.oci.intellij.account.ServicePreferences;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.actions.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public final class DatabaseDetails implements PropertyChangeListener {

  private static final String[] ADB_COLUMN_NAMES = new String[] {"Display Name",
          "Database Name", "State", "Free Tier", "Dedicated Infrastructure",
          "CPU Cores", "Storage (TB)", "Workload Type", "Created"};

  private static final String WORKLOAD_DW = "Data Warehouse";
  private static final String WORKLOAD_OLTP = "Transaction Processing";
  private static final String WORKLOAD_AJD = "JSON Database";
  private static final String WORKLOAD_ALL = "All";

  private static final String CREATE_ADB = "Create Autonomous Database";

  private JPanel mainPanel;
  private JComboBox<String> workloadCmb;
  private JButton refreshADBListButton;
  private JTable adbTable;
  private JLabel profileValueLbl;
  private JLabel compartmentValueLbl;
  private JLabel regionValueLbl;
  private JPanel tablePanel;
  private JButton createADBButton;
  private List<AutonomousDatabaseSummary> instances;

  public DatabaseDetails(){
    initializeWorkLoadTypeFilter();
    initializeTableStructure();
    initializeLabels();
    populateTableData();

    refreshADBListButton.setAction(new RefreshAction(this, "Refresh"));
    createADBButton.setAction(new CreateAction("Create Autonomous Database"));
  }

  private void initializeLabels() {
    profileValueLbl.setText(ServicePreferences.getProfileName());
    // TODO: This will always set the root compartment when initialized and might require change
    compartmentValueLbl.setText(Identity.getInstance().getCurrentCompartmentName());
    regionValueLbl.setText(AuthenticationDetails.getInstance().getRegion().toString());
  }

  private void initializeWorkLoadTypeFilter() {
    workloadCmb.addItem(WORKLOAD_ALL);
    workloadCmb.addItem(WORKLOAD_DW);
    workloadCmb.addItem(WORKLOAD_OLTP);
    workloadCmb.addItem(WORKLOAD_AJD);

    workloadCmb.setSelectedIndex(0);
    workloadCmb.addItemListener((ItemEvent e) -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        populateTableData();
      }
    });
  }

  private AutonomousDatabaseSummary.DbWorkload getWorkLoadType(String type) {
    switch (type) {
      case WORKLOAD_OLTP:
        return AutonomousDatabaseSummary.DbWorkload.Oltp;
      case WORKLOAD_DW:
        return AutonomousDatabaseSummary.DbWorkload.Dw;
      case WORKLOAD_AJD:
        return AutonomousDatabaseSummary.DbWorkload.Ajd;
      default:
        return AutonomousDatabaseSummary.DbWorkload.UnknownEnumValue;
    }
  }

  private void initializeTableStructure() {
    adbTable.setModel(new DefaultTableModel(ADB_COLUMN_NAMES, 0) {
      @Override
      public boolean isCellEditable(int row, int column){
        return false;
      }

      @Override
      public String getColumnName(int indx){
        return ADB_COLUMN_NAMES[indx];
      }

      @Override
      public Class<?> getColumnClass(int column){
        return (column == 2) ? JLabel.class : String.class;
      }
    });

    adbTable.getColumn("State").setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
      if (column == 2) {
        final AutonomousDatabaseSummary s = (AutonomousDatabaseSummary) value;
        final JLabel statusLbl = new JLabel(
                s.getLifecycleState().getValue());
        statusLbl.setIcon(getStatusImage(s.getLifecycleState()));
        return statusLbl;
      }
      return (Component) value;
    });

    adbTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e){
        if (e.getButton() == 3) {
          JPopupMenu popupMenu;
          AutonomousDatabaseSummary selectedSummary = null;
          if (adbTable.getSelectedRowCount() == 1) {
            Object selectedObject = adbTable.getModel()
                    .getValueAt(adbTable.getSelectedRow(), 2);
            if (selectedObject instanceof AutonomousDatabaseSummary) {
              selectedSummary = (AutonomousDatabaseSummary) adbTable
                      .getModel().getValueAt(adbTable.getSelectedRow(), 2);
            }
          }
          popupMenu = getADBActionMenu(selectedSummary, true);
          popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
      }
    });
  }

  private JPopupMenu getADBActionMenu(AutonomousDatabaseSummary selectedSummary,
                                      boolean showRefreshMenu) {
    final JPopupMenu popupMenu = new JPopupMenu();

    if (showRefreshMenu) {
      popupMenu.add(new JMenuItem(new RefreshAction(this, "Refresh")));
      popupMenu.addSeparator();
    }

    if (selectedSummary != null) {
      if (selectedSummary.getLifecycleState() == LifecycleState.Stopped) {
        popupMenu.add(new JMenuItem(new SimpleActionHandler(selectedSummary,
                SimpleActionHandler.ActionType.START)));
      }
      else if (selectedSummary.getLifecycleState() == LifecycleState.Available) {
        popupMenu.add(new JMenuItem(new SimpleActionHandler(selectedSummary,
                SimpleActionHandler.ActionType.STOP)));
      }

      popupMenu.add(new JMenuItem(new WizardActionHandler(
              WizardActionHandler.Action.RESTORE_ADB,
              selectedSummary, "Restore")));

      popupMenu.add(new JMenuItem(new WizardActionHandler(
              WizardActionHandler.Action.CLONE_DB,
              selectedSummary, "Create Clone")));

      popupMenu.add(new JMenuItem(new WizardActionHandler(WizardActionHandler.Action.ADMIN_PWD_CHANGE,
                      selectedSummary, "Administrator Password")));

      popupMenu.add(new JMenuItem(
              new WizardActionHandler(WizardActionHandler.Action.UPDATE_LICENSE,
                      selectedSummary, "Update License Type")));

      popupMenu.add(new JMenuItem(new SimpleActionHandler(selectedSummary,
              SimpleActionHandler.ActionType.TERMINATE)));

      if (selectedSummary.getDbWorkload().equals(
              AutonomousDatabaseSummary.DbWorkload.Ajd)) {
        popupMenu.add(new JMenuItem(new SimpleActionHandler(
                        selectedSummary, SimpleActionHandler.ActionType.CHANGE_WORKLOAD_TYPE)));
      }

      popupMenu.add(new JMenuItem(new WizardActionHandler(WizardActionHandler.Action.CREATE_ADB,
                      selectedSummary, CREATE_ADB)));

      popupMenu.add(new JMenuItem(
              new WizardActionHandler(WizardActionHandler.Action.DOWNLOAD_CREDENTIALS,
                      selectedSummary,
                      "Download Client Credentials (Wallet)")));

      popupMenu.add(new JMenuItem(new SimpleActionHandler(
              selectedSummary, SimpleActionHandler.ActionType.SERVICE_CONSOLE)));

      popupMenu.add(new JMenuItem(new WizardActionHandler(WizardActionHandler.Action.SCALE_ADB,
              selectedSummary, "Scale Up/Down")));

      popupMenu.add(new JMenuItem(
              new WizardActionHandler(WizardActionHandler.Action.CREATE_CONNECTION,
                      selectedSummary, "Create Connection")));

      popupMenu.addSeparator();
      popupMenu.add(new JMenuItem(new WizardActionHandler(
              WizardActionHandler.Action.ADB_INFO,
              selectedSummary,
              "Autonomous Database Information")));
    }

    return popupMenu;
  }

  public void populateTableData() {
    // Populate the data in the table
    final DefaultTableModel model = ((DefaultTableModel) adbTable.getModel());
    model.setRowCount(0);
    UIUtil.setStatus("Refreshing database instances..");

    refreshADBListButton.setEnabled(false);
    workloadCmb.setEnabled(false);

    final Runnable fetchData = () -> {
      String selectedType = (String) workloadCmb.getSelectedItem();
      AutonomousDatabaseSummary.DbWorkload workLoadType = selectedType == null ?
              AutonomousDatabaseSummary.DbWorkload.UnknownEnumValue :
              getWorkLoadType(selectedType);
      try {
        instances = ADBInstanceClient.getInstance().getInstances(workLoadType);
      } catch (Exception e) {
        UIUtil.fireNotification(NotificationType.ERROR,"Unable to fetch the db instance details : " + e.getMessage());
        LogHandler.error("Unable to fetch the db instance details", e);
      }
    };

    final Runnable updateUI = () -> {
      if (instances == null) {
        UIUtil.fireNotification(NotificationType.ERROR,"Unable to get the database instance details. Please check the OCI settings");
      } else {
        UIUtil.setStatus((instances.size()) + " database instances found.");

        final Function<AutonomousDatabaseSummary.DbWorkload, String> valueFunction = (workload) -> {
          switch (workload.getValue()) {
            case "OLTP":
              return WORKLOAD_OLTP;
            case "DW":
              return WORKLOAD_DW;
            case "AJD":
              return WORKLOAD_AJD;
            default:
              return WORKLOAD_ALL;
          }
        };

        for (AutonomousDatabaseSummary s : instances) {
          final Object[] rowData = new Object[ADB_COLUMN_NAMES.length];
          final boolean isFreeTier =
                  s.getIsFreeTier() != null && s.getIsFreeTier();
          rowData[0] = s.getDisplayName();
          rowData[1] = s.getDbName();
          rowData[2] = s;
          rowData[3] = isFreeTier ? "Yes" : "No";
          rowData[4] = (s.getIsDedicated() != null && s.getIsDedicated()) ?
                  "Yes" :
                  "No";
          rowData[5] = String.valueOf(s.getCpuCoreCount());
          rowData[6] = isFreeTier ?
                  ADBConstants.ALWAYS_FREE_STORAGE_TB :
                  String.valueOf(s.getDataStorageSizeInTBs());
          rowData[7] = valueFunction.apply(s.getDbWorkload());
          rowData[8] = s.getTimeCreated();
          model.addRow(rowData);
        }
      }

      workloadCmb.setEnabled(true);
      refreshADBListButton.setEnabled(true);
    };

    UIUtil.fetchAndUpdateUI(fetchData, updateUI);
  }

  private ImageIcon getStatusImage(LifecycleState state){
    if (state.equals(LifecycleState.Available) || state
            .equals(LifecycleState.ScaleInProgress))
      return new ImageIcon(
              getClass().getResource("/icons/db-available-state.png"));
    else if (state.equals(LifecycleState.Terminated) || state
            .equals(LifecycleState.Unavailable))
      return new ImageIcon(
              getClass().getResource("/icons/db-unavailable-state.png"));
    else
      return new ImageIcon(
              getClass().getResource("/icons/db-inprogress-state.png"));
  }

  public JComponent createCenterPanel() {
    return mainPanel;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt){
    LogHandler.info("DatabaseDetails: Handling the Event Update : " + evt.toString());
    switch (evt.getPropertyName()) {
      case ServicePreferences.EVENT_COMPARTMENT_UPDATE:
        compartmentValueLbl.setText(Identity.getInstance().getCurrentCompartmentName());
        break;

      case ServicePreferences.EVENT_REGION_UPDATE:
        regionValueLbl.setText(ServicePreferences.getRegion());
        break;

      case ServicePreferences.EVENT_SETTINGS_UPDATE:
      case ServicePreferences.EVENT_ADB_INSTANCE_UPDATE:
        profileValueLbl.setText(ServicePreferences.getProfileName());
        compartmentValueLbl.setText(Identity.getInstance().getCurrentCompartmentName());
        regionValueLbl.setText(ServicePreferences.getRegion());
        break;
    }
    populateTableData();
  }

  private static class RefreshAction extends AbstractAction {
    private final DatabaseDetails adbDetails;

    public RefreshAction(DatabaseDetails adbDetails, String actionName) {
      super(actionName);
      this.adbDetails = adbDetails;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      adbDetails.populateTableData();

    }
  }

  private static class CreateAction extends AbstractAction {
    public CreateAction(String actionName) {
      super(actionName);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final DialogWrapper wizard = new CreateAutonomousDatabaseWizard();
      if(wizard != null) {
        wizard.showAndGet();
      } else {
        LogHandler.error("Failed to show wizard \"Create Autonomous Database wizard\"");
      }
    }
  }

}
