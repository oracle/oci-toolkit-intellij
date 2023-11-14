/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.database;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.LifecycleState;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.common.AutonomousDatabaseConstants;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.actions.AutonomousDatabaseBasicActions;
import com.oracle.oci.intellij.ui.database.actions.AutonomousDatabaseMoreActions;
import com.oracle.oci.intellij.ui.database.actions.CreateAutonomousDatabaseDialog;
import com.oracle.oci.intellij.ui.explorer.ITabbedExplorerContent;
import com.oracle.oci.intellij.util.LogHandler;

public final class AutonomousDatabasesDashboard implements PropertyChangeListener, ITabbedExplorerContent {

  private static final String[] ADB_COLUMN_NAMES = new String[] {"Display Name",
          "Database Name", "State", "Free Tier", "Dedicated Infrastructure",
          "CPU Cores", "Storage (TB)", "Workload Type", "Created"};

  private static final String WORKLOAD_DW = "Data Warehouse";
  private static final String WORKLOAD_OLTP = "Transaction Processing";
  private static final String WORKLOAD_AJD = "JSON";
  private static final String WORKLOAD_APEX = "APEX";
  private static final String WORKLOAD_ALL = "All";

  private static final String CREATE_ADB = "Create Autonomous Database";

  private JPanel mainPanel;
  private JComboBox<String> workloadCombo;
  private JButton refreshADBInstancesButton;
  private JTable adbInstancesTable;
  private JLabel profileValueLabel;
  private JLabel compartmentValueLabel;
  private JLabel regionValueLabel;
  private JButton createADBInstanceButton;
  private List<AutonomousDatabaseSummary> autonomousDatabaseInstancesList;

  private static final AutonomousDatabasesDashboard INSTANCE =
          new AutonomousDatabasesDashboard();

  public static AutonomousDatabasesDashboard getInstance() {
    return INSTANCE;
  }

  private AutonomousDatabasesDashboard() {
    initializeWorkLoadTypeFilter();
    initializeTableStructure();
    initializeLabels();

    if (refreshADBInstancesButton != null) {
      refreshADBInstancesButton.setAction(new RefreshAction(this, "Refresh"));
    }
    
    if (createADBInstanceButton != null) {
      createADBInstanceButton.setAction(new CreateAction("Create Autonomous Database"));
    }

  }

  private void initializeLabels() {
    if (profileValueLabel == null || compartmentValueLabel == null || regionValueLabel == null) {
      LogHandler.info("Skipping Labels; form not populated");
      return;
    }
    profileValueLabel.setText(SystemPreferences.getProfileName());
    compartmentValueLabel.setText(SystemPreferences.getCompartmentName());
    regionValueLabel.setText(SystemPreferences.getRegionName());
  }

  private void initializeWorkLoadTypeFilter() {
    if (workloadCombo == null) {
      LogHandler.info("Skipping WorkLoadTypeFilter; form not populated");
      return;
    }
    workloadCombo.addItem(WORKLOAD_ALL);
    workloadCombo.addItem(WORKLOAD_OLTP);
    workloadCombo.addItem(WORKLOAD_DW);
    workloadCombo.addItem(WORKLOAD_AJD);
    workloadCombo.addItem(WORKLOAD_APEX);

    workloadCombo.setSelectedIndex(0);
    workloadCombo.addItemListener((ItemEvent e) -> {
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
      case WORKLOAD_APEX:
        return AutonomousDatabaseSummary.DbWorkload.Apex;
      default:
        return AutonomousDatabaseSummary.DbWorkload.UnknownEnumValue;
    }
  }

  private void initializeTableStructure() {
    if (workloadCombo == null) {
      LogHandler.info("Skipping Table; form not populated.");
      return;
    }
    adbInstancesTable.setModel(new DefaultTableModel(ADB_COLUMN_NAMES, 0) {
      /**
         * 
         */
        private static final long serialVersionUID = 1L;

    @Override
      public boolean isCellEditable(int row, int column){
        return false;
      }

      @Override
      public String getColumnName(int index){
        return ADB_COLUMN_NAMES[index];
      }

      @Override
      public Class<?> getColumnClass(int column){
        return (column == 2) ? JLabel.class : String.class;
      }
    });

    adbInstancesTable.getColumn("State").setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
      if (column == 2) {
        final AutonomousDatabaseSummary s = (AutonomousDatabaseSummary) value;
        final JLabel statusLbl = new JLabel(
                s.getLifecycleState().getValue());
        statusLbl.setIcon(getStatusImage(s.getLifecycleState()));
        return statusLbl;
      }
      return (Component) value;
    });

    adbInstancesTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e){
        if (e.getButton() == 3) {
          JPopupMenu popupMenu;
          AutonomousDatabaseSummary selectedSummary = null;
          if (adbInstancesTable.getSelectedRowCount() == 1) {
            Object selectedObject = adbInstancesTable.getModel()
                    .getValueAt(adbInstancesTable.getSelectedRow(), 2);
            if (selectedObject instanceof AutonomousDatabaseSummary) {
              selectedSummary = (AutonomousDatabaseSummary) adbInstancesTable
                      .getModel().getValueAt(adbInstancesTable.getSelectedRow(), 2);
            }
          }
          popupMenu = getADBActionMenu(selectedSummary);
          popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
      }
    });
  }

  private JPopupMenu getADBActionMenu(AutonomousDatabaseSummary selectedSummary) {
    final JPopupMenu popupMenu = new JPopupMenu();

    popupMenu.add(new JMenuItem(new RefreshAction(this, "Refresh")));
    popupMenu.addSeparator();

    if (selectedSummary != null) {
      if (selectedSummary.getLifecycleState() == LifecycleState.Available) {
        popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(selectedSummary,
                AutonomousDatabaseBasicActions.ActionType.STOP)));

        popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(
                AutonomousDatabaseMoreActions.Action.RESTORE_ADB,
                selectedSummary, "Restore")));

        popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(
                AutonomousDatabaseMoreActions.Action.CLONE_DB,
                selectedSummary, "Create Clone")));

        popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(
                AutonomousDatabaseMoreActions.Action.ADMIN_PWD_CHANGE,
                selectedSummary, "Administrator Password")));

        popupMenu.add(new JMenuItem(
                new AutonomousDatabaseMoreActions(AutonomousDatabaseMoreActions.Action.UPDATE_LICENSE,
                        selectedSummary, "Update License Type")));

        popupMenu.add(new JMenuItem(
                new AutonomousDatabaseMoreActions(AutonomousDatabaseMoreActions.Action.DOWNLOAD_CREDENTIALS,
                        selectedSummary, "Download Client Credentials (Wallet)")));

        popupMenu.add(new JMenuItem(
                new AutonomousDatabaseMoreActions(AutonomousDatabaseMoreActions.Action.UPDATE_NETWORK_ACCESS,
                        selectedSummary, "Update Network Access")));

        popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(AutonomousDatabaseMoreActions.Action.SCALE_ADB,
                selectedSummary, "Scale Up/Down")));

        if (selectedSummary.getDbWorkload().equals(
                AutonomousDatabaseSummary.DbWorkload.Ajd)) {
          popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(
                  selectedSummary, AutonomousDatabaseBasicActions.ActionType.CHANGE_WORKLOAD_TYPE)));
        }

        popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(selectedSummary,
                AutonomousDatabaseBasicActions.ActionType.TERMINATE)));

        popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(
                AutonomousDatabaseMoreActions.Action.ADB_INFO,
                selectedSummary,
                "Autonomous Database Information")));

        popupMenu.addSeparator();
      } else if (selectedSummary.getLifecycleState() == LifecycleState.Stopped) {
        popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(selectedSummary,
                AutonomousDatabaseBasicActions.ActionType.START)));

        popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(
                AutonomousDatabaseMoreActions.Action.CLONE_DB,
                selectedSummary, "Create Clone")));

        popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(selectedSummary,
                AutonomousDatabaseBasicActions.ActionType.TERMINATE)));
      } else if (selectedSummary.getLifecycleState() == LifecycleState.Provisioning) {
        popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(selectedSummary,
                AutonomousDatabaseBasicActions.ActionType.TERMINATE)));
      }

      popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(AutonomousDatabaseMoreActions.Action.CREATE_ADB,
              selectedSummary, CREATE_ADB)));

      popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(
              selectedSummary, AutonomousDatabaseBasicActions.ActionType.SERVICE_CONSOLE)));
    }

    return popupMenu;
  }

  public void populateTableData() {
    ((DefaultTableModel) adbInstancesTable.getModel()).setRowCount(0);
    UIUtil.showInfoInStatusBar("Refreshing Autonomous Databases.");

    refreshADBInstancesButton.setEnabled(false);
    workloadCombo.setEnabled(false);

    final Runnable fetchData = () -> {
      try {
        final AutonomousDatabaseSummary.DbWorkload workLoadType =
                getWorkLoadType((String) workloadCombo.getSelectedItem());

        autonomousDatabaseInstancesList = OracleCloudAccount.getInstance()
                .getDatabaseClient().getAutonomousDatabaseInstances(workLoadType);
      } catch (Exception exception) {
        autonomousDatabaseInstancesList = null;
        UIUtil.fireNotification(NotificationType.ERROR, exception.getMessage(), null);
        LogHandler.error(exception.getMessage(), exception);
      }
    };

    final Runnable updateUI = () -> {
      if (autonomousDatabaseInstancesList != null) {
        UIUtil.showInfoInStatusBar((autonomousDatabaseInstancesList.size()) + " Autonomous Databases found.");

        final Function<AutonomousDatabaseSummary.DbWorkload, String> valueFunction = (workload) -> {
          switch (workload.getValue()) {
            case "DW":
              return WORKLOAD_DW;
            case "OLTP":
              return WORKLOAD_OLTP;
            case "AJD":
              return WORKLOAD_AJD;
            case "APEX":
              return WORKLOAD_APEX;
            default:
              return WORKLOAD_ALL;
          }
        };

        final DefaultTableModel model = ((DefaultTableModel) adbInstancesTable.getModel());
        model.setRowCount(0);

        for (AutonomousDatabaseSummary s : autonomousDatabaseInstancesList) {
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
                  AutonomousDatabaseConstants.ALWAYS_FREE_STORAGE_TB :
                  String.valueOf(s.getDataStorageSizeInTBs());
          rowData[7] = valueFunction.apply(s.getDbWorkload());
          rowData[8] = s.getTimeCreated();
          model.addRow(rowData);
        }
      }

      workloadCombo.setEnabled(true);
      refreshADBInstancesButton.setEnabled(true);
    };

    UIUtil.executeAndUpdateUIAsync(fetchData, updateUI);
  }

  private ImageIcon getStatusImage(LifecycleState state) {
    if (state.equals(LifecycleState.Available) || state
            .equals(LifecycleState.ScaleInProgress)) {
      return new ImageIcon(getClass().getResource("/icons/db-available-state.png"));
    } else if (state.equals(LifecycleState.Terminated) || state
            .equals(LifecycleState.Unavailable)) {
      return new ImageIcon(getClass().getResource("/icons/db-unavailable-state.png"));
    } else {
      return new ImageIcon(getClass().getResource("/icons/db-inprogress-state.png"));
    }
  }

  public JComponent createCenterPanel() {
    return mainPanel;
  }

  @Override
  public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
    LogHandler.info("AutonomousDatabasesDashboard: Handling the Event Update : " + propertyChangeEvent.toString());
    ((DefaultTableModel) adbInstancesTable.getModel()).setRowCount(0);

    switch (propertyChangeEvent.getPropertyName()) {
      case SystemPreferences.EVENT_COMPARTMENT_UPDATE:
        compartmentValueLabel.setText(SystemPreferences.getCompartmentName());
        break;

      case SystemPreferences.EVENT_REGION_UPDATE:
        regionValueLabel.setText(propertyChangeEvent.getNewValue().toString());
        break;

      case SystemPreferences.EVENT_SETTINGS_UPDATE:
      case SystemPreferences.EVENT_ADB_INSTANCE_UPDATE:
        profileValueLabel.setText(SystemPreferences.getProfileName());
        compartmentValueLabel.setText(SystemPreferences.getCompartmentName());
        regionValueLabel.setText(SystemPreferences.getRegionName());
        break;
    }
    populateTableData();
  }

  private static class RefreshAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final AutonomousDatabasesDashboard adbDetails;

    public RefreshAction(AutonomousDatabasesDashboard adbDetails, String actionName) {
      super(actionName);
      this.adbDetails = adbDetails;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      adbDetails.populateTableData();
    }
  }

  private static class CreateAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CreateAction(String actionName) {
      super(actionName);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final DialogWrapper wizard = new CreateAutonomousDatabaseDialog();
      wizard.showAndGet();
    }

  }

//  private static class DeployAction extends AbstractAction {
//    /**
//     *
//     */
//    private static final long serialVersionUID = 1L;
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//      // TODO Auto-generated method stub
//      
//    }
//
//   }


  @Override
  public String getTitle() {
    return "Autonomouse Database";
  }

}
