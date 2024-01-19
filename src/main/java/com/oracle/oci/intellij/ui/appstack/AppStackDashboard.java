/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.appstack;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.oracle.bmc.identity.model.AuthToken;
import com.oracle.bmc.resourcemanager.model.JobSummary;
import com.oracle.bmc.resourcemanager.model.LogEntry;
import com.oracle.bmc.resourcemanager.responses.GetJobLogsResponse;
import com.oracle.oci.intellij.ui.appstack.command.*;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.Nullable;


import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.bmc.resourcemanager.model.StackSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.CommandFailedException;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Severity;
import com.oracle.oci.intellij.common.command.CommandStack;
import com.oracle.oci.intellij.common.command.CompositeCommand;
import com.oracle.oci.intellij.ui.appstack.command.DeleteStackCommand;
import com.oracle.oci.intellij.ui.appstack.command.DestroyStackCommand;
import com.oracle.oci.intellij.ui.appstack.command.GetStackJobsCommand;
import com.oracle.oci.intellij.ui.appstack.command.GetStackJobsCommand.GetStackJobsResult;
import com.oracle.oci.intellij.ui.appstack.command.ListStackCommand.ListStackResult;
import com.oracle.oci.intellij.ui.appstack.uimodel.AppStackTableModel;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.explorer.ITabbedExplorerContent;
import com.oracle.oci.intellij.util.LogHandler;

public final class AppStackDashboard implements PropertyChangeListener, ITabbedExplorerContent {

  private JPanel mainPanel;
  //private JComboBox<String> workloadCombo;
  // buttons bound in form
  private JButton refreshAppStackButton;
  private JButton deleteAppStackButton;
  private JButton createAppStackButton;
  private JTable appStacksTable;
  private JLabel profileValueLabel;
  private JLabel compartmentValueLabel;
  private JLabel regionValueLabel;
  private List<StackSummary> appStackList;
  private CommandStack commandStack = new CommandStack();


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
      createAppStackButton.setAction(new CreateAction(this, "Create New AppStack"));
    }
    
    if (deleteAppStackButton != null) {
      deleteAppStackButton.setAction(new DeleteAction(this, "Delete AppStack"));
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
    appStacksTable.setModel(new AppStackTableModel(0));

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
      @Override
      public void mouseClicked(MouseEvent e){
        if (e.getButton() == 3) {
          JPopupMenu popupMenu;
          StackSummary selectedSummary = null;
          if (appStacksTable.getSelectedRowCount() == 1) {
            Object selectedObject = appStackList.get(appStacksTable.getSelectedRow());
            if (selectedObject instanceof StackSummary) {
              selectedSummary = (StackSummary) selectedObject;
            }
          }
          // TODO:
          System.out.println("Pop!");
          popupMenu = getStackSummaryActionMenu(selectedSummary);
          popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
      }
    });
  }

//  private static class StackJobDialog extends DialogWrapper {
//
//    private final List<JobSummary> jobs;
//
//    protected StackJobDialog(List<JobSummary> jobs) {
//      super(true);
//      this.jobs = new ArrayList<>(jobs);
//      init();
//      setTitle("Stack Job");
//      setOKButtonText("Ok");
//    }
//
//    @Override
//    protected @Nullable JComponent createCenterPanel() {
//      JPanel centerPanel = new JPanel();
//      centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
//
//      DefaultTableModel jobsModel = new DefaultTableModel();
//      jobsModel.addColumn("Name");
//      jobsModel.addColumn("Operation");
//      jobsModel.addColumn("Status");
//      jobsModel.addColumn("Time Created");
//      List<Object> row = new ArrayList<>();
//      this.jobs.forEach(j -> {
//        row.add(j.getDisplayName());
//        row.add(j.getOperation());
//        row.add(j.getLifecycleState());
//        row.add(j.getTimeCreated());
//        jobsModel.addRow(row.toArray());
//        row.clear();
//      });
//
//      JTable jobsTable = new JTable();
//      jobsTable.setModel(jobsModel);
//      centerPanel.add(jobsTable);
//
//      JTextArea textArea = new JTextArea();
////      textArea.setText("Hello!");
//      textArea.setLineWrap(true);
//      textArea.setEditable(false);
//      textArea.setVisible(true);
//
//      JScrollPane scroll = new JScrollPane (textArea);
//      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//
//      centerPanel.add(scroll);
//  //    centerPanel.add(textArea);
//
//      jobsTable.addMouseListener(new  MouseAdapter() {
//
//        @Override
//        public void mouseClicked(MouseEvent e) {
//          if (e.getButton() == MouseEvent.BUTTON1) {
//            if (jobsTable.getSelectedRowCount() == 1) {
//              int selectedRow = jobsTable.getSelectedRow();
//              JobSummary jobSummary = jobs.get(selectedRow);
//              String id = jobSummary.getId();
//              GetJobLogsResponse jobLogs =
//                OracleCloudAccount.getInstance().getResourceManagerClientProxy().getJobLogs(id);
//              List<LogEntry> items = jobLogs.getItems();
//              textArea.setText(null);
//              StringBuilder builder = new StringBuilder();
//              for (LogEntry logEntry : items) {
//                builder.append(logEntry.getMessage());
//                builder.append("\n");
//              }
//              textArea.setText(builder.toString());
//            }
//          }
//        }
//      });
//
//      return centerPanel;
//    }
//  }

  private static class LoadStackJobsAction extends AbstractAction {

    private static final long serialVersionUID = 1463690182909882687L;
    private StackSummary stack;
    
    public LoadStackJobsAction(StackSummary stack) {
      super("View Jobs..");
      this.stack = stack;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      ResourceManagerClientProxy resourceManagerClientProxy = OracleCloudAccount.getInstance().getResourceManagerClientProxy();
      GetStackJobsCommand command = new GetStackJobsCommand(resourceManagerClientProxy, 
                                        stack.getCompartmentId(), stack.getId());
      // TODO: bother with command stack?
      try {
        GetStackJobsResult execute = command.execute();
        if (execute.isOk()) {
          
          execute.getJobs().forEach(job -> System.out.println(job));
          StackJobDialog dialog = new StackJobDialog(execute.getJobs());
          dialog.show();

        }
        else if (execute.getException() != null) {
          throw execute.getException();
        }
      } catch (Throwable e1) {
        throw new RuntimeException(e1);
      }
    }
  }
  
  private JPopupMenu getStackSummaryActionMenu(StackSummary selectedSummary) {
    final JPopupMenu popupMenu = new JPopupMenu();
//
//    popupMenu.add(new JMenuItem(new RefreshAction(this, "Refresh")));
//    popupMenu.addSeparator();
//
    if (selectedSummary != null) {
      popupMenu.add(new JMenuItem(new LoadStackJobsAction(selectedSummary)));
//      if (selectedSummary.getLifecycleState() == LifecycleState.Available) {

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
    }
    return popupMenu;
  }

  public void populateTableData() {
    ((DefaultTableModel) appStacksTable.getModel()).setRowCount(0);
    UIUtil.showInfoInStatusBar("Refreshing Autonomous Databases.");

    refreshAppStackButton.setEnabled(false);

    final Runnable fetchData = () -> {
      try {
        String compartmentId = SystemPreferences.getCompartmentId();
        ListStackCommand command = 
          new ListStackCommand(OracleCloudAccount.getInstance().getResourceManagerClientProxy(), compartmentId);
        ListStackResult result = (ListStackResult) commandStack.execute(command);
        if (!result.isError()) {
          appStackList =  result.getStacks();
        }
        else 
        {
          throw new CommandFailedException("Failed refreshing list of stacks");
        }
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
          final Object[] rowData = new Object[AppStackTableModel.APPSTACK_COLUMN_NAMES.length];
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

//  private ImageIcon getStatusImage(LifecycleState state) {
//    if (state.equals(LifecycleState.Available) || state
//            .equals(LifecycleState.ScaleInProgress)) {
//      return new ImageIcon(getClass().getResource("/icons/db-available-state.png"));
//    } else if (state.equals(LifecycleState.Terminated) || state
//            .equals(LifecycleState.Unavailable)) {
//      return new ImageIcon(getClass().getResource("/icons/db-unavailable-state.png"));
//    } else {
//      return new ImageIcon(getClass().getResource("/icons/db-inprogress-state.png"));
//    }
//  }

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
    private AppStackDashboard dashboard;

    public CreateAction(AppStackDashboard dashboard, String actionName) {
      super(actionName);
      this.dashboard = dashboard;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

      AtomicReference<Map<String, String>> variables = new AtomicReference<>(new LinkedHashMap<>());

      dashboard.createAppStackButton.setEnabled(false);
      Runnable runnable = () -> {
        YamlLoader loader = new YamlLoader();
        dashboard.createAppStackButton.setEnabled(true);

        try {
          variables.set(loader.load());
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException ex) {
          throw new RuntimeException(ex);
        }
        if (variables.get() == null)
          return;
        try {
          ResourceManagerClientProxy proxy = OracleCloudAccount.getInstance().getResourceManagerClientProxy();
          String compartmentId = SystemPreferences.getCompartmentId();
          ClassLoader cl = AppStackDashboard.class.getClassLoader();
          CreateStackCommand command =
                  new CreateStackCommand(proxy, compartmentId, cl, "appstackforjava.zip");
//          Map<String,String> variables = new ModelLoader().loadTestVariables();
//          variables.put("shape","CI.Standard.E3.Flex");
          command.setVariables(variables.get());
//          command.setVariables(variables.get());
          this.dashboard.commandStack.execute(command);
        } catch (Exception e1) {
          throw new RuntimeException(e1);
        }
      };
      ApplicationManager.getApplication().invokeLater(runnable);

    }
  }
  
  public static class DeleteAction extends AbstractAction {

    private static final long serialVersionUID = 7216149349340773007L;
    private AppStackDashboard dashboard;
    public DeleteAction(AppStackDashboard dashboard, String title) {
      super("Delete");
      this.dashboard = dashboard;
    }
    
    private static class DeleteYesNoDialog extends DialogWrapper {

      protected DeleteYesNoDialog() {
        super(true);
        init();
        setTitle("Confirm Delete");
        setOKButtonText("Ok");
      }

      @Override
      protected @Nullable JComponent createNorthPanel() {
        JPanel northPanel = new JPanel();
        JLabel label = new JLabel();
        label.setText("Delete Stack.  Are you sure?");
        northPanel.add(label);
        return northPanel;
      }

      @Override
      protected @NotNull JPanel createButtonsPanel(@NotNull List<? extends JButton> buttons) {
        return new JPanel();
      }

      @Override
      protected @Nullable JComponent createCenterPanel() {
        JPanel messagePanel = new JPanel(new BorderLayout());

        JPanel yesNoButtonPanel = new JPanel();
        yesNoButtonPanel.setLayout(new BoxLayout(yesNoButtonPanel, BoxLayout.X_AXIS));
        JButton yesButton = new JButton();
        yesButton.setText("Yes");
        yesButton.addActionListener(new ActionListener() { 
          @Override
          public void actionPerformed(ActionEvent e) {
            close(OK_EXIT_CODE);
          }
        });
        yesNoButtonPanel.add(yesButton);
        JButton noButton = new JButton();
        noButton.setText("No");
        noButton.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            close(CANCEL_EXIT_CODE);
          }
          
        });
        yesNoButtonPanel.add(noButton);
        
        messagePanel.add(yesNoButtonPanel, BorderLayout.CENTER);
        
        JCheckBox myCheckBox = new JCheckBox();
        myCheckBox.setText("Destroy stack before deleting");
        myCheckBox.setSelected(true);
        messagePanel.add(myCheckBox, BorderLayout.SOUTH);

       // pack();

        return messagePanel;
      }
      
    }
    @Override
    public void actionPerformed(ActionEvent e) {
     
      int selectedRow = this.dashboard.appStacksTable.getSelectedRow();
      // TODO: should be better way to get select row object
      if (selectedRow >=0 && selectedRow < this.dashboard.appStackList.size()) {
        StackSummary stackSummary = this.dashboard.appStackList.get(selectedRow);
        ResourceManagerClientProxy proxy = OracleCloudAccount.getInstance().getResourceManagerClientProxy();
        DeleteYesNoDialog dialog = new DeleteYesNoDialog();
        boolean yesToDelete = dialog.showAndGet();
        if (yesToDelete) {
          DestroyStackCommand destroyCommmand = new DestroyStackCommand(proxy, stackSummary.getId());
          DeleteStackCommand deleteCommand = new DeleteStackCommand(proxy, stackSummary.getId());
          CompositeCommand compositeCommand = new CompositeCommand(destroyCommmand, deleteCommand);
          
          Thread t = new Thread(() -> {
            try {
              Result r = this.dashboard.commandStack.execute(compositeCommand);
              if (r.getSeverity() != Severity.ERROR) {
                SwingUtilities.invokeAndWait(() -> {
                  this.dashboard.populateTableData();
                });
              }
            } catch (CommandFailedException | InvocationTargetException | InterruptedException e1) {
              // TODO:
              e1.printStackTrace();
            }
          });
          t.start();
        }
      }
    }
  }

  @Override
  public String getTitle() {
    return "Application Stack";
  }


}