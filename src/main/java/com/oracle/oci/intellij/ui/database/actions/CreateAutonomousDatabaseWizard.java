package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.ADBConstants;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateAutonomousDatabaseWizard extends DialogWrapper {
  private JPanel basicInfoOfAutonomousDatabasePanel;
  private JTextField displayNameTextField;
  private JLabel displayNameLabel;
  private JLabel databaseNameLabel;
  private JTextField databaseNameTextField;
  private JLabel compartmentLabel;
  private JTextField compartmentTextField;
  private JButton selectCompartmentButton;
  private JRadioButton dataWarehouseRadioButton;
  private JPanel workloadTypePanel;
  private JRadioButton transactionProcessingRadioButton;
  private JRadioButton jsonRadioButton;
  private JRadioButton apexRadioButton;
  private JPanel deploymentTypePanel;
  private JRadioButton sharedInfrastructureRadioButton;
  private JRadioButton dedicatedInfrastructureRadioButton;
  private JPanel configureDatabasePanel;
  private JComboBox databaseVersionComboBox;
  private JLabel databaseVersionLabel;
  private JLabel ocpuCountLabel;
  private JLabel storageLabel;
  private JTextField usernameTextField;
  private JPanel adminCredentialsPanel;
  private JLabel usernameLabel;
  private JLabel passwordLabel;
  private JLabel autoScalingLabel;
  private JCheckBox autoScalingCheckBox;
  private JLabel confirmPasswordLabel;
  private JPanel networkAccessPanel;
  private JPasswordField passwordField;
  private JPasswordField confirmPasswordField;
  private JPanel accessTypePanel;
  private JRadioButton allowSecureAccessFromRadioButton;
  private JRadioButton virtualCloudNetworkRadioButton;
  private JPanel autonomousContainerDatabasePanel;
  private JPanel autonomousDataGuardPanel;
  private JCheckBox configureAccessControlRulesCheckBox;
  private JCheckBox autonomousDataGuardEnabledCheckBox;
  private JPanel configureAccessControlRulesPanel;
  private JPanel ipNotationPanel;
  private JPanel virtualCloudNetworkPanel;
  private JPanel networkSecurityGroupsPanel;
  private JPanel virtCloudNetworkSubnetPanel;
  private JPanel mainPanel;
  private JPanel licenseTypePanel;
  private JRadioButton bringYourOwnLicenseRadioButton;
  private JRadioButton licenseIncludedRadioButton;
  private JButton changeCompartmentButton;
  private JComboBox autonomousContainerDatabaseComboBox;
  private JPanel autonomousDataGuardSubPanel;
  private JComboBox virtualCloudNetworkSubnetCombo;
  private JTextField virtualCloudNetworkHostTextField;
  private JPanel networkSecurityGroupsSubPanel;
  private JPanel hostNamePrefixPanel;
  private JPanel anotherNetworkSecurityGroupPanel;
  private JButton addAnotherNetworkSecurityButton;
  private JComboBox virtualCloudNetworkComboBox;
  private JButton virtualCloudNetworkChangeCompartmentButton;
  private JButton virtualCloudNetworkSubnetChangeCompartmentButton;
  private JComboBox networkSecurityGroupChangeComboBox;
  private JButton networkSecurityGroupChangeCompartmentButton;
  private JComboBox ipNotationTypeComboBox;
  private JTextField ipNotationTypeTextField;
  private JPanel ipNotationTypePanel;
  private JPanel ipNotationValuesPanel;
  private JPanel ipNotationAddEntryPanel;
  private JButton ipNotationAddAnotherEntryButton;
  private JSpinner ocpuCountSpinner;
  private JSpinner storageSpinner;

  private final ButtonGroup workloadTypeButtonGroup;
  private final ButtonGroup deploymentTypeButtonGroup;
  private final ButtonGroup accessTypeButtonGroup;
  private final ButtonGroup licenseTypeButtonGroup;

  private static final String WINDOW_TITLE = "Create Autonomous Database";
  private static final String OK_TEXT = "Create";
  private static final String DATABASE_DEFAULT_USERNAME = "ADMIN";

  private static final String AUTONOMOUS_DATAGUARD_SUBPANEL_TEXT = "Autonomous Container Database in ";
  private static final String VIRTUAL_CLOUD_NETWORK_PANEL_TEXT = "Virtual cloud network in ";
  private static final String VIRTUAL_CLOUD_SUBNET_PANEL_TEXT = "Subnet in ";
  private static final String NETWORK_SECURITY_GROUP_SUBPANEL_TEXT = "Network security group in ";

    public CreateAutonomousDatabaseWizard() {
    super(true);
    init();
    setTitle(WINDOW_TITLE);
    setOKButtonText(OK_TEXT);

    // Set the initial border title for panels that show compartment name, on selection, in its title.
    Border initialTitleBorder = BorderFactory.createTitledBorder(AUTONOMOUS_DATAGUARD_SUBPANEL_TEXT);
    autonomousDataGuardSubPanel.setBorder(initialTitleBorder);

    initialTitleBorder = BorderFactory.createTitledBorder(VIRTUAL_CLOUD_NETWORK_PANEL_TEXT);
    virtualCloudNetworkPanel.setBorder(initialTitleBorder);

    initialTitleBorder = BorderFactory.createTitledBorder(VIRTUAL_CLOUD_SUBNET_PANEL_TEXT);
    virtCloudNetworkSubnetPanel.setBorder(initialTitleBorder);

    initialTitleBorder = BorderFactory.createTitledBorder(NETWORK_SECURITY_GROUP_SUBPANEL_TEXT);
    networkSecurityGroupsSubPanel.setBorder(initialTitleBorder);

    // Set the border line text for Admin credentials panel.
    final String passwordHelpWebLink =
            "https://docs.oracle.com/en-us/iaas/Content/Database/Tasks/adbmanaging.htm#setadminpassword";
    final String credentialsPanelBorderText = "<html>Create administrator credentials. " +
            "<a href=" + "\"" + passwordHelpWebLink + "\"" + ">HELP</a></html";
    final Border borderLine = BorderFactory
            .createTitledBorder(credentialsPanelBorderText);
    adminCredentialsPanel.setBorder(borderLine);
    UIUtil.makeWebLink(adminCredentialsPanel, passwordHelpWebLink);

    // Add workload type radio buttons to radio button group.
    workloadTypeButtonGroup = new ButtonGroup();
    workloadTypeButtonGroup.add(dataWarehouseRadioButton);
    workloadTypeButtonGroup.add(transactionProcessingRadioButton);
    workloadTypeButtonGroup.add(jsonRadioButton);
    workloadTypeButtonGroup.add(apexRadioButton);

    // Add deployment type radio buttons to radio button group.
    deploymentTypeButtonGroup = new ButtonGroup();
    deploymentTypeButtonGroup.add(sharedInfrastructureRadioButton);
    deploymentTypeButtonGroup.add(dedicatedInfrastructureRadioButton);

    // Add access type radio buttons to radio button group.
    accessTypeButtonGroup = new ButtonGroup();
    accessTypeButtonGroup.add(allowSecureAccessFromRadioButton);
    accessTypeButtonGroup.add(virtualCloudNetworkRadioButton);

    // Add license type radio buttons to radio button group.
    licenseTypeButtonGroup = new ButtonGroup();
    licenseTypeButtonGroup.add(bringYourOwnLicenseRadioButton);
    licenseTypeButtonGroup.add(licenseIncludedRadioButton);

    // Do default selections.
    transactionProcessingRadioButton.setSelected(true);
    sharedInfrastructureRadioButton.setSelected(true);
    autonomousContainerDatabasePanel.setVisible(false);
    autoScalingCheckBox.setSelected(true);
    allowSecureAccessFromRadioButton.setSelected(true);
    ipNotationPanel.setVisible(false);

    virtualCloudNetworkPanel.setVisible(false);
    virtCloudNetworkSubnetPanel.setVisible(false);
    hostNamePrefixPanel.setVisible(false);
    networkSecurityGroupsPanel.setVisible(false);
    anotherNetworkSecurityGroupPanel.setVisible(false);
    ipNotationAddEntryPanel.setVisible(false);

    // Add action listener for select compartment button.
    selectCompartmentButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

        if(compartmentSelection.showAndGet()) {
          final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
          if(selectedCompartment != null) {
            compartmentTextField.setText(selectedCompartment.getName());
          }
        }
      }
    });

    // Set database name and display name with default values.
    final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
    final String presentDateTime =  DATE_TIME_FORMAT.format(new Date());

    final String databaseDisplayNameDefault = "DB " + presentDateTime;
    displayNameTextField.setText(databaseDisplayNameDefault);

    final String databaseNameDefault = "DB" + presentDateTime;
    databaseNameTextField.setText(databaseNameDefault);

    // Initialize the spinners with defaults.
    ocpuCountSpinner.setModel(new SpinnerNumberModel(ADBConstants.CPU_CORE_COUNT_DEFAULT,
                    ADBConstants.CPU_CORE_COUNT_MIN, ADBConstants.CPU_CORE_COUNT_MAX,
                    ADBConstants.CPU_CORE_COUNT_INCREMENT));
    storageSpinner.setModel(new SpinnerNumberModel(ADBConstants.STORAGE_IN_TB_DEFAULT,
                    ADBConstants.STORAGE_IN_TB_MIN, ADBConstants.STORAGE_IN_TB_MAX,
                    ADBConstants.STORAGE_IN_TB_INCREMENT));

    usernameTextField.setText(DATABASE_DEFAULT_USERNAME);
    usernameTextField.setEditable(false);

    // Add action listener for Data Warehouse workload type radio button.
    dataWarehouseRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        sharedInfrastructureRadioButton.setEnabled(true);
        dedicatedInfrastructureRadioButton.setEnabled(true);

        bringYourOwnLicenseRadioButton.setEnabled(true);
        licenseIncludedRadioButton.setEnabled(true);
      }
    });

    // Add action listener for Transaction Processing workload type radio button.
    transactionProcessingRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        sharedInfrastructureRadioButton.setEnabled(true);
        dedicatedInfrastructureRadioButton.setEnabled(true);

        bringYourOwnLicenseRadioButton.setEnabled(true);
        licenseIncludedRadioButton.setEnabled(true);
      }
    });

    // Add action listener for JSON workload type radio button.
    jsonRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        sharedInfrastructureRadioButton.setEnabled(true);
        // Dedicated instructure deployment type is not supported.
        dedicatedInfrastructureRadioButton.setEnabled(false);

        bringYourOwnLicenseRadioButton.setEnabled(false);
        licenseIncludedRadioButton.setEnabled(true);
        licenseIncludedRadioButton.setSelected(true);
      }
    });

    // Add action listener for APEX workload type radio button.
    apexRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        sharedInfrastructureRadioButton.setEnabled(true);
        // Dedicated infrastructure deployment type is not supported.
        dedicatedInfrastructureRadioButton.setEnabled(false);

        bringYourOwnLicenseRadioButton.setEnabled(false);
        licenseIncludedRadioButton.setEnabled(true);
        licenseIncludedRadioButton.setSelected(true);
      }
    });

    // Add action listener for Shared Infrastructure deployment type radio button.
    sharedInfrastructureRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        databaseVersionLabel.setVisible(true);
        databaseVersionComboBox.setVisible(true);

        jsonRadioButton.setEnabled(true);
        apexRadioButton.setEnabled(true);
        autonomousContainerDatabasePanel.setVisible(false);
        networkAccessPanel.setVisible(true);
        licenseTypePanel.setVisible(true);
      }
    });

    // Add action listener for Dedicated Infrastructure deployment type radio button.
    dedicatedInfrastructureRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        autonomousDataGuardEnabledCheckBox.setSelected(false);
        databaseVersionLabel.setVisible(false);
        databaseVersionComboBox.setVisible(false);

        jsonRadioButton.setEnabled(false);
        apexRadioButton.setEnabled(false);
        autonomousContainerDatabasePanel.setVisible(true);
        networkAccessPanel.setVisible(false);
        licenseTypePanel.setVisible(false);
      }
    });

    allowSecureAccessFromRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        configureAccessControlRulesPanel.setVisible(true);
        ipNotationPanel.setVisible(false);

        virtualCloudNetworkPanel.setVisible(false);
        virtCloudNetworkSubnetPanel.setVisible(false);
        hostNamePrefixPanel.setVisible(false);
        networkSecurityGroupsPanel.setVisible(false);
        anotherNetworkSecurityGroupPanel.setVisible(false);
      }
    });

    virtualCloudNetworkRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        configureAccessControlRulesPanel.setVisible(false);

        virtualCloudNetworkPanel.setVisible(true);
        virtCloudNetworkSubnetPanel.setVisible(true);
        hostNamePrefixPanel.setVisible(true);
        networkSecurityGroupsPanel.setVisible(true);
        anotherNetworkSecurityGroupPanel.setVisible(true);
      }
    });

    configureAccessControlRulesCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (configureAccessControlRulesCheckBox.isSelected()) {
          ipNotationPanel.setVisible(true);
          ipNotationAddEntryPanel.setVisible(true);
        } else {
          ipNotationPanel.setVisible(false);
          ipNotationAddEntryPanel.setVisible(false);
        }
      }
    });

    changeCompartmentButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

        if(compartmentSelection.showAndGet()) {
          final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
          if(selectedCompartment != null) {
            final Border newTitleBorder = BorderFactory
                    .createTitledBorder(AUTONOMOUS_DATAGUARD_SUBPANEL_TEXT + selectedCompartment.getName());
            autonomousDataGuardSubPanel.setBorder(newTitleBorder);
          }
        }
      }
    });

    virtualCloudNetworkChangeCompartmentButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

        if(compartmentSelection.showAndGet()) {
          final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
          if(selectedCompartment != null) {
            final Border newTitleBorder = BorderFactory
                    .createTitledBorder(VIRTUAL_CLOUD_NETWORK_PANEL_TEXT + selectedCompartment.getName());
            virtualCloudNetworkPanel.setBorder(newTitleBorder);
          }
        }
      }
    });

    virtualCloudNetworkSubnetChangeCompartmentButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

        if(compartmentSelection.showAndGet()) {
          final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
          if(selectedCompartment != null) {
            final Border newTitleBorder = BorderFactory
                    .createTitledBorder(VIRTUAL_CLOUD_SUBNET_PANEL_TEXT + selectedCompartment.getName());
            virtCloudNetworkSubnetPanel.setBorder(newTitleBorder);
          }
        }
      }
    });

    networkSecurityGroupChangeCompartmentButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e){
        final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

        if(compartmentSelection.showAndGet()) {
          final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
          if(selectedCompartment != null) {
            final Border newTitleBorder = BorderFactory
                    .createTitledBorder(NETWORK_SECURITY_GROUP_SUBPANEL_TEXT + selectedCompartment.getName());
            networkSecurityGroupsSubPanel.setBorder(newTitleBorder);
          }
        }
      }
    });
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return mainPanel;
  }
}
