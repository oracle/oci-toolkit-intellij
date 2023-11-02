/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.appstack;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.intellij.notification.NotificationType;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.LifecycleState;
import com.oracle.bmc.resourcemanager.model.StackSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.explorer.ITabbedExplorerContent;
import com.oracle.oci.intellij.util.LogHandler;

public final class AppStackDashboard implements PropertyChangeListener, ITabbedExplorerContent {

  private static final String[] APPSTACK_COLUMN_NAMES =
    new String[] { "Display Name", "Description", "Terraform Version", "State", "Created" };

  private static final String CREATE_APPSTACK = "Create Application Stack";

  private JPanel mainPanel;
  //private JComboBox<String> workloadCombo;
  private JButton refreshAppStackButton;
  private JTable appStacksTable;
  private JLabel profileValueLabel;
  private JLabel compartmentValueLabel;
  private JLabel regionValueLabel;
  private JButton createAppStackButton;
  private List<StackSummary> appStackList;

  private static final AppStackDashboard INSTANCE =
          new AppStackDashboard();

  public synchronized static AppStackDashboard getInstance() {
    return INSTANCE;
  }

  private AppStackDashboard() {
    //initializeWorkLoadTypeFilter();
    initializeTableStructure();
    initializeLabels();

    if (refreshAppStackButton != null) {
      refreshAppStackButton.setAction(new RefreshAction(this, "Refresh"));
    }
    
    if (createAppStackButton != null) {
      createAppStackButton.setAction(new CreateAction("Create New AppStack"));
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

  private void initializeTableStructure() {
    appStacksTable.setModel(new DefaultTableModel(APPSTACK_COLUMN_NAMES, 0) {
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
        return APPSTACK_COLUMN_NAMES[index];
      }

      @Override
      public Class<?> getColumnClass(int column){
        return String.class; //(column == 2) ? JLabel.class : String.class;
      }
    });

//    appStacksTable.getColumn("State").setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
//      if (column == 2) {
//        final AutonomousDatabaseSummary s = (AutonomousDatabaseSummary) value;
//        final JLabel statusLbl = new JLabel(
//                s.getLifecycleState().getValue());
//        statusLbl.setIcon(getStatusImage(s.getLifecycleState()));
//        return statusLbl;
//      }
//      return (Component) value;
//    });

    appStacksTable.addMouseListener(new MouseAdapter() {
//      @Override
//      public void mouseClicked(MouseEvent e){
//        if (e.getButton() == 3) {
//          JPopupMenu popupMenu;
//          AutonomousDatabaseSummary selectedSummary = null;
//          if (appStacksTable.getSelectedRowCount() == 1) {
//            Object selectedObject = appStacksTable.getModel()
//                    .getValueAt(appStacksTable.getSelectedRow(), 2);
//            if (selectedObject instanceof AutonomousDatabaseSummary) {
//              selectedSummary = (AutonomousDatabaseSummary) appStacksTable
//                      .getModel().getValueAt(appStacksTable.getSelectedRow(), 2);
//            }
//          }
//          // TODO:
//          System.out.println("Pop!");
////          popupMenu = getADBActionMenu(selectedSummary);
////          popupMenu.show(e.getComponent(), e.getX(), e.getY());
//        }
//      }
    });
  }

//  private JPopupMenu getADBActionMenu(AutonomousDatabaseSummary selectedSummary) {
//    final JPopupMenu popupMenu = new JPopupMenu();
//
//    popupMenu.add(new JMenuItem(new RefreshAction(this, "Refresh")));
//    popupMenu.addSeparator();
//
//    if (selectedSummary != null) {
//      if (selectedSummary.getLifecycleState() == LifecycleState.Available) {
//        popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(selectedSummary,
//                AutonomousDatabaseBasicActions.ActionType.STOP)));
//
//        popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(
//                AutonomousDatabaseMoreActions.Action.RESTORE_ADB,
//                selectedSummary, "Restore")));
//
//        popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(
//                AutonomousDatabaseMoreActions.Action.CLONE_DB,
//                selectedSummary, "Create Clone")));
//
//        popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(
//                AutonomousDatabaseMoreActions.Action.ADMIN_PWD_CHANGE,
//                selectedSummary, "Administrator Password")));
//
//        popupMenu.add(new JMenuItem(
//                new AutonomousDatabaseMoreActions(AutonomousDatabaseMoreActions.Action.UPDATE_LICENSE,
//                        selectedSummary, "Update License Type")));
//
//        popupMenu.add(new JMenuItem(
//                new AutonomousDatabaseMoreActions(AutonomousDatabaseMoreActions.Action.DOWNLOAD_CREDENTIALS,
//                        selectedSummary, "Download Client Credentials (Wallet)")));
//
//        popupMenu.add(new JMenuItem(
//                new AutonomousDatabaseMoreActions(AutonomousDatabaseMoreActions.Action.UPDATE_NETWORK_ACCESS,
//                        selectedSummary, "Update Network Access")));
//
//        popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(AutonomousDatabaseMoreActions.Action.SCALE_ADB,
//                selectedSummary, "Scale Up/Down")));
//
//        if (selectedSummary.getDbWorkload().equals(
//                AutonomousDatabaseSummary.DbWorkload.Ajd)) {
//          popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(
//                  selectedSummary, AutonomousDatabaseBasicActions.ActionType.CHANGE_WORKLOAD_TYPE)));
//        }
//
//        popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(selectedSummary,
//                AutonomousDatabaseBasicActions.ActionType.TERMINATE)));
//
//        popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(
//                AutonomousDatabaseMoreActions.Action.ADB_INFO,
//                selectedSummary,
//                "Autonomous Database Information")));
//
//        popupMenu.addSeparator();
//      } else if (selectedSummary.getLifecycleState() == LifecycleState.Stopped) {
//        popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(selectedSummary,
//                AutonomousDatabaseBasicActions.ActionType.START)));
//
//        popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(
//                AutonomousDatabaseMoreActions.Action.CLONE_DB,
//                selectedSummary, "Create Clone")));
//
//        popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(selectedSummary,
//                AutonomousDatabaseBasicActions.ActionType.TERMINATE)));
//      } else if (selectedSummary.getLifecycleState() == LifecycleState.Provisioning) {
//        popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(selectedSummary,
//                AutonomousDatabaseBasicActions.ActionType.TERMINATE)));
//      }
//
//      popupMenu.add(new JMenuItem(new AutonomousDatabaseMoreActions(AutonomousDatabaseMoreActions.Action.CREATE_ADB,
//              selectedSummary, CREATE_ADB)));
//
//      popupMenu.add(new JMenuItem(new AutonomousDatabaseBasicActions(
//              selectedSummary, AutonomousDatabaseBasicActions.ActionType.SERVICE_CONSOLE)));
//    }
//
//    return popupMenu;
//  }

  public void populateTableData() {
    ((DefaultTableModel) appStacksTable.getModel()).setRowCount(0);
    UIUtil.showInfoInStatusBar("Refreshing Autonomous Databases.");

    refreshAppStackButton.setEnabled(false);

    final Runnable fetchData = () -> {
      try {
        String compartmentId = SystemPreferences.getCompartmentId();
        appStackList = OracleCloudAccount.getInstance()
                .getResourceManagerClientProxy().listStacks(compartmentId);
      } catch (Exception exception) {
        appStackList = null;
        UIUtil.fireNotification(NotificationType.ERROR, exception.getMessage(), null);
        LogHandler.error(exception.getMessage(), exception);
      }
    };

    final Runnable updateUI = () -> {
      if (appStackList != null) {
        UIUtil.showInfoInStatusBar((appStackList.size()) + " Autonomous Databases found.");
        final DefaultTableModel model = ((DefaultTableModel) appStacksTable.getModel());
        model.setRowCount(0);

        for (StackSummary s : appStackList) {
          final Object[] rowData = new Object[APPSTACK_COLUMN_NAMES.length];
//          final boolean isFreeTier =
//                  s.getIsFreeTier() != null && s.getIsFreeTier();
          rowData[0] = s.getDisplayName();
          rowData[1] = s.getDescription();
          rowData[2] = s.getTerraformVersion();
          rowData[3] = s.getLifecycleState();
          rowData[4] = s.getTimeCreated();
          model.addRow(rowData);
        }
      }
      refreshAppStackButton.setEnabled(true);
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
    ((DefaultTableModel) appStacksTable.getModel()).setRowCount(0);

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
    // TODO: populateTableData();
  }

  private static class RefreshAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final AppStackDashboard appStackDashBoard;

    public RefreshAction(AppStackDashboard adbDetails, String actionName) {
      super(actionName);
      this.appStackDashBoard = adbDetails;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      appStackDashBoard.populateTableData();
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
//      final DialogWrapper wizard = new CreateAutonomousDatabaseDialog();
//      wizard.showAndGet();
//      try {
////        OracleCloudAccount.getInstance().getResourceManagerClientProxy().createStack();
//      } catch (IOException e1) {
//        // TODO Auto-generated catch block
//        e1.printStackTrace();
//      }

      try {
        YamlLoader.Load();
      } catch (IOException | IntrospectionException | InvocationTargetException | IllegalAccessException ex) {
        throw new RuntimeException(ex);
      }
//      try {
//        OracleCloudAccount.getInstance().getResourceManagerClientProxy().createStack();
//      } catch (IOException e1) {
//        // TODO Auto-generated catch block
//        e1.printStackTrace();
//      }
    }
  }

  @Override
  public String getTitle() {
    return "Application Stack";
  }

}
