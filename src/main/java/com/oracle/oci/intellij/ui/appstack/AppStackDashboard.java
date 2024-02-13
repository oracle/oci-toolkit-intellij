/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.appstack;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.jetbrains.annotations.Nullable;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.oracle.bmc.resourcemanager.model.StackSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.CommandFailedException;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Severity;
import com.oracle.oci.intellij.common.command.CommandStack;
import com.oracle.oci.intellij.common.command.CompositeCommand;
import com.oracle.oci.intellij.ui.appstack.command.CreateStackCommand;
import com.oracle.oci.intellij.ui.appstack.command.DestroyStackCommand;
import com.oracle.oci.intellij.ui.appstack.command.GetStackJobsCommand;
import com.oracle.oci.intellij.ui.appstack.command.GetStackJobsCommand.GetStackJobsResult;
import com.oracle.oci.intellij.ui.appstack.command.ListStackCommand;
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
  private JButton destroyAppStackButton;
  private List<StackSummary> appStackList;
  private CommandStack commandStack = new CommandStack();
  private static ResourceBundle resBundle;

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

    if (destroyAppStackButton != null){
//      destroyAppStackButton.setOpaque(true);
//      destroyAppStackButton.setBackground(Color.RED);
//      destroyAppStackButton.setBorderPainted(false);
      destroyAppStackButton.setToolTipText(resBundle.getString("DELETE_BUTTON_TOOLTIP"));
      destroyAppStackButton.setAction(new DestroyAction(this,"Destroy AppStack"));
    }

    resBundle = ResourceBundle.getBundle("appStackDashboard", Locale.ROOT);
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
  private static class LoadAppUrlAction extends AbstractAction {

    public LoadAppUrlAction(StackSummary stack) {
      super("Launch App Url..");
     // this.stack = stack;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
      Desktop desktop = java.awt.Desktop.getDesktop();
      try {
        //specify the protocol along with the URL
        URI oURL = new URI(
            "http://129.153.104.43");
        desktop.browse(oURL);
      } catch (URISyntaxException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
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
      popupMenu.add(new JMenuItem(new LoadAppUrlAction(selectedSummary)));

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
    populateTableData();
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
          //todo get the compartment from the form , that i will add in the stack infos
          String compartmentId = variables.get().get("appstack_compartment");
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

  public static class DestroyAction extends AbstractAction {

    private static final long serialVersionUID = 7216149349340773007L;
    private final AppStackDashboard dashboard;
    public DestroyAction(AppStackDashboard dashboard, String title) {
      super("Destroy");
      this.dashboard = dashboard;
    }

    private static class DestroyYesNoDialog extends DialogWrapper {

      protected DestroyYesNoDialog() {
        super(true);
        init();
        setTitle("Confirm Destroy");
        setOKButtonText("Ok");
      }

      @Override
      protected @Nullable JComponent createNorthPanel() {
        JPanel northPanel = new JPanel();
        JLabel label = new JLabel();
        label.setText("Destroy Stack.  Are you sure?");
        northPanel.add(label);
        return northPanel;
      }

//      @Override
//      protected @NotNull JPanel createButtonsPanel(@NotNull List<? extends JButton> buttons) {
//        return new JPanel();
//      }

      @Override
      protected @Nullable JComponent createCenterPanel() {
        JPanel messagePanel = new JPanel(new BorderLayout());

//        JPanel yesNoButtonPanel = new JPanel();
//        yesNoButtonPanel.setLayout(new BoxLayout(yesNoButtonPanel, BoxLayout.X_AXIS));
//        JButton yesButton = new JButton();
//        yesButton.setText("Yes");
//        yesButton.addActionListener(new ActionListener() {
//          @Override
//          public void actionPerformed(ActionEvent e) {
//            close(OK_EXIT_CODE);
//          }
//        });
//        yesNoButtonPanel.add(yesButton);
//        JButton noButton = new JButton();
//        noButton.setText("No");
//        noButton.addActionListener(new ActionListener() {
//
//          @Override
//          public void actionPerformed(ActionEvent e) {
//            close(CANCEL_EXIT_CODE);
//          }
//
//        });
//        yesNoButtonPanel.add(noButton);
//
//        messagePanel.add(yesNoButtonPanel, BorderLayout.CENTER);
//
//        JCheckBox myCheckBox = new JCheckBox();
//        myCheckBox.setText("Destroy stack before deleting");
//        myCheckBox.setSelected(true);
//        messagePanel.add(myCheckBox, BorderLayout.SOUTH);
//
//        // pack();
//
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
        DestroyYesNoDialog dialog = new DestroyYesNoDialog();
        boolean yesToDelete = dialog.showAndGet();
        if (yesToDelete) {
          DestroyStackCommand destroyCommmand = new DestroyStackCommand(proxy, stackSummary.getId());
          CompositeCommand compositeCommand = new CompositeCommand(destroyCommmand);
          invokeLater(this.dashboard,compositeCommand);
        }
      }
    }
  }
  private static void invokeLater(AppStackDashboard appStackDashboard,CompositeCommand command){
    Thread t = new Thread(() -> {
      try {
        Result r = appStackDashboard.commandStack.execute(command);
        if (r.getSeverity() != Severity.ERROR) {
          SwingUtilities.invokeAndWait(() -> {
            appStackDashboard.populateTableData();
          });
        }
      } catch (CommandFailedException | InvocationTargetException | InterruptedException e1) {
        // TODO:
        e1.printStackTrace();
      }
    });
    t.start();
  }

  public static class DeleteAction extends AbstractAction {

    private static final long serialVersionUID = 7216149349340773007L;
    private AppStackDashboard dashboard;

    public DeleteAction(AppStackDashboard dashboard, String title) {
      super("Delete...");
      this.dashboard = dashboard;
    }
    
    private static class DeleteYesNoDialog extends DialogWrapper {
      // TODO: externalize
      private static final String DESTROY_ONLY_DESCRIPTION_TEXT = resBundle.getString("DESTROY_ONLY_DESCRIPTION_TEXT");
      private static final String DELETE_ONLY_DESCRIPTION_TEXT = resBundle.getString("DELETE_ONLY_DESCRIPTION_TEXT");
      private static final String DELETE_ALL_DESCRIPTION_TEXT = resBundle.getString("DELETE_ALL_DESCRIPTION_TEXT");
      JRadioButton deleteInfraStackRdoBtn;
      JRadioButton deleteStackRdoBtn;
      JRadioButton deleteAllRdoBtn;

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
        label.setText("Warning: This operation cannot be undone. Are you sure?");
        northPanel.add(label);
        return northPanel;
      }


      @SuppressWarnings("serial")
      @Override
      protected void doOKAction() {
        Object inputVal = JOptionPane.showInputDialog(this.getContentPane(), 
                                    "This action cannot be undone.  Please type \"confirm\" to confirm your choice",
                                    "Confirm Deletion",
                                    JOptionPane.QUESTION_MESSAGE,
                                    null, null, null);
        System.out.println(inputVal);
        super.doOKAction();
      }

      @Override
      protected @Nullable JComponent createCenterPanel() {
        JPanel messagePanel = new JPanel();
        messagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        BoxLayout mgr = new BoxLayout(messagePanel, BoxLayout.PAGE_AXIS);
        messagePanel.setLayout(mgr);

        JTextArea descriptionText = new JTextArea();
        descriptionText.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionText.setEditable(false);
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setColumns(20);
        descriptionText.setText(DESTROY_ONLY_DESCRIPTION_TEXT);

        JLabel selectOptionLbl = new JLabel("Select the deletion mode:");

        ActionListener selectionListener = new ActionListener() {
          
          @Override
          public void actionPerformed(ActionEvent e) {
            switch(e.getActionCommand()) {
            case "DESTROY_ONLY":
              descriptionText.setText(DESTROY_ONLY_DESCRIPTION_TEXT);
              break;
            case "DELETE_ONLY":
              descriptionText.setText(DELETE_ONLY_DESCRIPTION_TEXT);
              break;
            case "DELETE_ALL":
              descriptionText.setText(DELETE_ALL_DESCRIPTION_TEXT);
            }
          }
        };   
        deleteInfraStackRdoBtn = new JRadioButton();
        initRadioBtn(deleteInfraStackRdoBtn, "DESTROY_ONLY", "Destroy the OCI infrastructure for the selected stack.", selectionListener
                     );

        deleteStackRdoBtn = new JRadioButton();
        initRadioBtn(deleteStackRdoBtn, "DELETE_ONLY", "Delete the definition for the stack", selectionListener);
        
        deleteAllRdoBtn = new JRadioButton();
        initRadioBtn(deleteAllRdoBtn, "DELETE_ALL", "Delete everything to do with the selected stack", selectionListener);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(deleteInfraStackRdoBtn);
        buttonGroup.add(deleteStackRdoBtn);
        buttonGroup.add(deleteAllRdoBtn);

        addHorizontalPadding(messagePanel, 15);
        messagePanel.add(descriptionText);
        addHorizontalPadding(messagePanel, 15);
        messagePanel.add(selectOptionLbl);
        //addHorizontalPadding(messagePanel, 10);
        messagePanel.add(deleteInfraStackRdoBtn);
        //addHorizontalPadding(messagePanel, 5);
        messagePanel.add(deleteStackRdoBtn);
        //addHorizontalPadding(messagePanel, 5);
        messagePanel.add(deleteAllRdoBtn);
       // pack();

       deleteInfraStackRdoBtn.setSelected(true);

        return messagePanel;
      }

      private void initRadioBtn(JRadioButton rdoButton, String actionCommand, String text,
                                ActionListener actionListener) {
        rdoButton.setText(text);
        rdoButton.setActionCommand(actionCommand);
        rdoButton.addActionListener(actionListener);
        rdoButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      }

      private void addHorizontalPadding(JPanel messagePanel, int padding) {
        Dimension minSize = new Dimension(0, padding);
        Dimension prefSize = new Dimension(0, padding);
        Dimension maxSize = new Dimension(Short.MAX_VALUE, padding);
        messagePanel.add(new Box.Filler(minSize, prefSize, maxSize));     
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
        CompositeCommand compositeCommand;
        if (yesToDelete) {
//          if (dialog.shouldDestroy()){
//            DestroyStackCommand destroyCommmand = new DestroyStackCommand(proxy, stackSummary.getId());
//            DeleteStackCommand deleteCommand = new DeleteStackCommand(proxy, stackSummary.getId());
//            compositeCommand = new CompositeCommand(destroyCommmand, deleteCommand);
//          }else {
//            DeleteStackCommand deleteCommand = new DeleteStackCommand(proxy, stackSummary.getId());
//            compositeCommand = new CompositeCommand(deleteCommand);
//          }
//          invokeLater(this.dashboard,compositeCommand);
        }
      }
    }
  }

  @Override
  public String getTitle() {
    return "Application Stack";
  }


}