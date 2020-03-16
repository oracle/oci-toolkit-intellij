package com.oracle.oci.intellij.ui.database;

import com.intellij.openapi.wm.WindowManager;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.LifecycleState;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase;
import com.oracle.oci.intellij.ErrorHandler;
import com.oracle.oci.intellij.account.AuthProvider;
import com.oracle.oci.intellij.account.GlobalEventHandler;
import com.oracle.oci.intellij.account.IdentClient;
import com.oracle.oci.intellij.account.PreferencesWrapper;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.actions.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class DatabaseDetails implements PropertyChangeListener {

  private static final String[] ADB_COLUMN_NAMES = new String[] { "Name",
      "Database Name", "State", "Always Free", "Dedicated Infrastructure",
      "CPU-Core", "Storage", "Workload Type", "Created" };

  private static final String WORKLOAD_DW = "Data Warehouse";
  private static final String WORKLOAD_OLTP = "Transaction Processing";
  private static final String WORKLOAD_ALL = "All";

  private RefreshAction refreshAction;

  private JPanel mainPanel;
  private JComboBox workloadCmb;
  private JButton refreshADBListButton;
  private JTable adbTable;
  private JLabel profileValueLbl;
  private JLabel compartmentValueLbl;
  private JLabel regionValueLbl;
  private JPanel tablePanel;

  List<AutonomousDatabaseSummary> instances;

  public DatabaseDetails() {
    initializeTable();
    initializeWorkLoadTypeFilter();
    initializeActions();
    profileValueLbl.setText(PreferencesWrapper.getProfile());
    // TODO: This will always set the root compartment when initialized and might require change
    compartmentValueLbl.setText(IdentClient.getInstance().getCurrentCompartmentName());
    regionValueLbl.setText(AuthProvider.getInstance().getRegion().toString());
    refreshADBListButton.setAction(refreshAction);
    GlobalEventHandler.getInstance().addPropertyChangeListener(this);
  }

  private void initializeActions() {
    refreshAction = new RefreshAction(this, "Refresh List");
  }

  private void initializeWorkLoadTypeFilter() {
    workloadCmb.addItem(WORKLOAD_ALL);
    workloadCmb.addItem(WORKLOAD_OLTP);
    workloadCmb.addItem(WORKLOAD_DW);
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
    default:
      return AutonomousDatabaseSummary.DbWorkload.UnknownEnumValue;
    }
  }

  private void initializeTable() {

    try {
      adbTable.setModel(new DefaultTableModel(ADB_COLUMN_NAMES, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }

        @Override
        public String getColumnName(int indx) {
          return ADB_COLUMN_NAMES[indx];
        }

        @Override
        public Class getColumnClass(int column) {
          return (column == 2) ? JLabel.class : String.class;
        }
      });

      adbTable.getColumn("State").setCellRenderer(new TableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column) {
          if (column == 2) {
            final AutonomousDatabaseSummary s = (AutonomousDatabaseSummary) value;
            final JLabel statusLbl = new JLabel(
                s.getLifecycleState().getValue());
            statusLbl.setIcon(getStatusImage(s.getLifecycleState()));
            return statusLbl;
          }
          return (Component) value;
        }
      });

      adbTable.addMouseListener(new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
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
            popupMenu = getADBActionMenu(selectedSummary);
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
          }
        }
      });
      populateTableData();
    }
    catch (Exception e) {
      ErrorHandler
          .logError("Unable to list Autonomous Databases: " + e.getMessage());
    }
  }

  private JPopupMenu getADBActionMenu(
      AutonomousDatabaseSummary selectedSummary) {
    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(new JMenuItem(new RefreshAction(this, "Refresh List")));
    popupMenu.addSeparator();
    popupMenu.add(new JMenuItem(
        new WizardActionHandler(WizardActionHandler.Action.CREATE_ADB,
            CreateAutonomousDatabaseBase.DbWorkload.Dw,
            "Create ADW Instance")));
    popupMenu.add(new JMenuItem(new WizardActionHandler(
        WizardActionHandler.Action.CREATE_ADB,
        CreateAutonomousDatabaseBase.DbWorkload.Oltp,
        "Create ATP Instance")));
    if (selectedSummary != null) {
      popupMenu.addSeparator();
      popupMenu.add(new JMenuItem(
          new WizardActionHandler(WizardActionHandler.Action.ADMIN_PWD_CHANGE,
              selectedSummary, "Admin Password")));
      popupMenu
          .add(new JMenuItem(new WizardActionHandler(
              WizardActionHandler.Action.CLONE_DB,
              selectedSummary, "Create Clone")));
      popupMenu.add(new JMenuItem(
          new WizardActionHandler(WizardActionHandler.Action.CREATE_CONNECTION,
              selectedSummary, "Create Connection")));
      popupMenu.add(new JMenuItem(
          new WizardActionHandler(WizardActionHandler.Action.DOWNLOAD_CREDENTIALS,
              selectedSummary,
          "Download Client Credentials (Wallet)")));
      popupMenu
          .add(new JMenuItem(new WizardActionHandler(
              WizardActionHandler.Action.RESTORE_ADB,
              selectedSummary, "Restore")));
      popupMenu.add(
          new JMenuItem(new WizardActionHandler(WizardActionHandler.Action.SCALE_ADB,
              selectedSummary, "Scale Up/Down")));

      if (selectedSummary.getLifecycleState() == LifecycleState.Stopped)
        popupMenu.add(new JMenuItem(new SimpleActionHandler(selectedSummary,
            SimpleActionHandler.ActionType.START)));
      else if (selectedSummary.getLifecycleState() == LifecycleState.Available)
        popupMenu.add(new JMenuItem(new SimpleActionHandler(selectedSummary,
            SimpleActionHandler.ActionType.STOP)));

      popupMenu.add(new JMenuItem(new SimpleActionHandler(selectedSummary,
          SimpleActionHandler.ActionType.TERMINATE)));
      popupMenu.add(new JMenuItem(
          new WizardActionHandler(WizardActionHandler.Action.UPDATE_LICENSE,
              selectedSummary, "Update License Type")));
      popupMenu.addSeparator();
      popupMenu.add(new JMenuItem(new WizardActionHandler(
          WizardActionHandler.Action.ADB_INFO,
          selectedSummary,
          "Autonomous Database Information")));

      /*
      popupMenu.addSeparator();
      popupMenu.add(new JMenuItem(
          new RegisterDriverAction(this, "Register Database Driver")));
       */
    }
    return popupMenu;
  }

  public void populateTableData() {
    // Populate the data in the table
    final DefaultTableModel model = ((DefaultTableModel) adbTable.getModel());
    model.setRowCount(0);
    UIUtil.setStatus("Refreshing database instances..");
    Runnable fetchData = () -> {
      String selectedType = (String) workloadCmb.getSelectedItem();
      AutonomousDatabaseSummary.DbWorkload workLoadType = selectedType == null ?
          AutonomousDatabaseSummary.DbWorkload.UnknownEnumValue :
          getWorkLoadType(selectedType);
      try {
        instances = ADBInstanceClient.getInstance().getInstances(workLoadType);
      }
      catch (Exception e) {
        ErrorHandler
            .logErrorStack("Unable to fetch the db instance details", e);
      }
    };

    Runnable updateUI = () -> {
      UIUtil.setStatus( (instances==null ? 0 : instances.size() ) + " database instances found.");
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
        rowData[7] = s.getDbWorkload().getValue();
        rowData[8] = s.getTimeCreated().toGMTString();
        model.addRow(rowData);
      }
    };

    UIUtil.fetchAndUpdateUI(fetchData, updateUI);
  }

  private ImageIcon getStatusImage(LifecycleState state) {
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
  public void propertyChange(final PropertyChangeEvent evt) {
    System.out.println("@@@handling the proeprty changes in evetn dd");
    profileValueLbl.setText(PreferencesWrapper.getProfile());
    if ("RegionID".equals(evt.getPropertyName())) {
      regionValueLbl.setText(evt.getNewValue().toString());
    }
    else if ("CompartmentID".equals(evt.getPropertyName())) {
      compartmentValueLbl
          .setText(IdentClient.getInstance().getCurrentCompartmentName());
    }
    populateTableData();
  }

  class RefreshAction extends AbstractAction {

    private final DatabaseDetails adbDetails;

    public RefreshAction(DatabaseDetails adbDetails, String actionName) {
      super(actionName);
      this.adbDetails = adbDetails;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // Messages.showInfoMessage("Refreshing the data", "Refresh List");
      adbDetails.populateTableData();

    }
  }
}
