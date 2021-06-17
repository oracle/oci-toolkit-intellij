package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseDetails;
import com.oracle.bmc.database.model.CustomerContact;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.common.AutonomousDatabaseConstants;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class CreateAutonomousDatabaseDialog extends DialogWrapper {
  private JTextField displayNameTextField;
  private JTextField databaseNameTextField;
  private JTextField compartmentTextField;
  private JButton selectCompartmentButton;
  private JRadioButton dataWarehouseRadioButton;
  private JRadioButton transactionProcessingRadioButton;
  private JRadioButton jsonRadioButton;
  private JRadioButton apexRadioButton;
  private JRadioButton sharedInfrastructureRadioButton;
  private JRadioButton dedicatedInfrastructureRadioButton;
  private JComboBox<String> databaseVersionComboBox;
  private JLabel databaseVersionLabel;
  private JTextField usernameTextField;
  private JPanel adminCredentialsPanel;
  private JCheckBox autoScalingCheckBox;
  private JPanel networkAccessOptionsPanel;
  private JPasswordField passwordField;
  private JPasswordField confirmPasswordField;
  private JPanel autonomousContainerDatabasePanel;
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
  private JTextField autonomousContainerDatabaseTextField;
  private JPanel autonomousDataGuardSubPanel;
  private JPanel networkSecurityGroupsSubPanel;
  private JPanel hostNamePrefixPanel;
  private JPanel anotherNetworkSecurityGroupPanel;
  private JButton virtualCloudNetworkChangeCompartmentButton;
  private JButton virtualCloudNetworkSubnetChangeCompartmentButton;
  private JButton networkSecurityGroupChangeCompartmentButton;
  private JPanel ipNotationAddEntryPanel;
  private JSpinner ocpuCountSpinner;
  private JSpinner storageSpinner;
  private JPanel provideTenMaintenanceContactsPanel;
  private JTextField contactEmailTextField;
  private JCheckBox showOnlyAlwaysFreeCheckBox;
  private JLabel alwaysFreeLabel;
  private JTextPane alwaysFreeConfigGuidelinesTextPane;
  private JLabel alwaysFreeHelpLabel;
  private JRadioButton secureAccessFromEverywhereRadioButton;
  private JRadioButton privateEndPointAccessOnlyRadioButton;

  private Compartment selectedCompartment;

  private static final String WINDOW_TITLE = "Create Autonomous Database";
  private static final String OK_TEXT = "Create";

  private static final String AUTONOMOUS_DATA_GUARD_SUB_PANEL_TEXT = "Autonomous Container Database in ";
  private static final String VIRTUAL_CLOUD_NETWORK_PANEL_TEXT = "Virtual cloud network in ";
  private static final String VIRTUAL_CLOUD_SUBNET_PANEL_TEXT = "Subnet in ";
  private static final String NETWORK_SECURITY_GROUP_SUB_PANEL_TEXT = "Network security group in ";

    public CreateAutonomousDatabaseDialog() {
    super(true);
    init();
    setTitle(WINDOW_TITLE);
    setOKButtonText(OK_TEXT);

    // Set the initial border title for panels that show compartment name, on selection, in its title.
    Border initialTitleBorder = BorderFactory.createTitledBorder(AUTONOMOUS_DATA_GUARD_SUB_PANEL_TEXT);
    autonomousDataGuardSubPanel.setBorder(initialTitleBorder);

    initialTitleBorder = BorderFactory.createTitledBorder(VIRTUAL_CLOUD_NETWORK_PANEL_TEXT);
    virtualCloudNetworkPanel.setBorder(initialTitleBorder);

    initialTitleBorder = BorderFactory.createTitledBorder(VIRTUAL_CLOUD_SUBNET_PANEL_TEXT);
    virtCloudNetworkSubnetPanel.setBorder(initialTitleBorder);

    initialTitleBorder = BorderFactory.createTitledBorder(NETWORK_SECURITY_GROUP_SUB_PANEL_TEXT);
    networkSecurityGroupsSubPanel.setBorder(initialTitleBorder);

    // Set the border line text for Admin credentials panel.
    final String pswdHelpWebLink =
            "https://docs.oracle.com/en-us/iaas/Content/Database/Tasks/adbmanaging.htm#setadminpassword";
    final String credentialsPanelBorderText = "<html>Create administrator credentials. " +
            "<a href=" + "\"" + pswdHelpWebLink + "\"" + ">HELP</a></html";
    final Border borderLine = BorderFactory
            .createTitledBorder(credentialsPanelBorderText);
    adminCredentialsPanel.setBorder(borderLine);
    UIUtil.createWebLink(adminCredentialsPanel, pswdHelpWebLink);

    // Add workload type radio buttons to radio button group.
    final ButtonGroup workloadTypeButtonGroup = new ButtonGroup();
    workloadTypeButtonGroup.add(dataWarehouseRadioButton);
    workloadTypeButtonGroup.add(transactionProcessingRadioButton);
    workloadTypeButtonGroup.add(jsonRadioButton);
    workloadTypeButtonGroup.add(apexRadioButton);

    // Add deployment type radio buttons to radio button group.
    final ButtonGroup deploymentTypeButtonGroup = new ButtonGroup();
    deploymentTypeButtonGroup.add(sharedInfrastructureRadioButton);
    deploymentTypeButtonGroup.add(dedicatedInfrastructureRadioButton);
    // Add this option in the next release.
    dedicatedInfrastructureRadioButton.setEnabled(false);
    databaseVersionLabel.setVisible(false);
    databaseVersionComboBox.setVisible(false);

    // Add access type radio buttons to radio button group.
    final ButtonGroup accessTypeButtonGroup = new ButtonGroup();
    accessTypeButtonGroup.add(secureAccessFromEverywhereRadioButton);
    accessTypeButtonGroup.add(privateEndPointAccessOnlyRadioButton);

    // Add license type radio buttons to radio button group.
    final ButtonGroup licenseTypeButtonGroup = new ButtonGroup();
    licenseTypeButtonGroup.add(bringYourOwnLicenseRadioButton);
    licenseTypeButtonGroup.add(licenseIncludedRadioButton);

    // Do default selections.
    dataWarehouseRadioButton.setSelected(true);
    sharedInfrastructureRadioButton.setSelected(true);
    autonomousContainerDatabasePanel.setVisible(false);
    autoScalingCheckBox.setSelected(true);
    secureAccessFromEverywhereRadioButton.setSelected(true);
    ipNotationPanel.setVisible(false);

    virtualCloudNetworkPanel.setVisible(false);
    virtCloudNetworkSubnetPanel.setVisible(false);
    hostNamePrefixPanel.setVisible(false);
    networkSecurityGroupsPanel.setVisible(false);
    anotherNetworkSecurityGroupPanel.setVisible(false);
    ipNotationAddEntryPanel.setVisible(false);

    // Add action listener for select compartment button.
    selectCompartmentButton.addActionListener(actionEvent -> {
      final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

      if(compartmentSelection.showAndGet()) {
        selectedCompartment = compartmentSelection.getSelectedCompartment();
        if(selectedCompartment != null) {
          compartmentTextField.setText(selectedCompartment.getName());
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

    // Always free help web link.
    final String alwaysFreeHelpWebLink =
            "https://docs.cloud.oracle.com/iaas/Content/Database/Concepts/adbfreeoverview.htm#LifecycleforAlwaysFreeAutonomousDatabases";
    final String alwaysFreeHelpText = "<html>" +
            "<a href=" + "\"" + alwaysFreeHelpWebLink + "\"" + ">Free Tier HELP</a></html";
    alwaysFreeHelpLabel.setText(alwaysFreeHelpText);
    UIUtil.createWebLink(alwaysFreeHelpLabel, alwaysFreeHelpWebLink);

    final SpinnerNumberModel storageSpinnerNumberModel = new SpinnerNumberModel(
            AutonomousDatabaseConstants.STORAGE_IN_TB_DEFAULT,
            AutonomousDatabaseConstants.STORAGE_IN_TB_MIN,
            AutonomousDatabaseConstants.STORAGE_IN_TB_MAX,
            AutonomousDatabaseConstants.STORAGE_IN_TB_INCREMENT);

    showOnlyAlwaysFreeCheckBox.addActionListener((event) -> {
      if (showOnlyAlwaysFreeCheckBox.isSelected()) {
        //dedicatedInfrastructureRadioButton.setEnabled(false);
        ocpuCountSpinner.setEnabled(false);
        storageSpinnerNumberModel.setValue(AutonomousDatabaseConstants.STORAGE_IN_TB_FREE_TIER_DEFAULT);
        storageSpinner.setEnabled(false);
        autoScalingCheckBox.setSelected(false);
        autoScalingCheckBox.setEnabled(false);
        //privateEndPointAccessOnlyRadioButton.setEnabled(false);
        bringYourOwnLicenseRadioButton.setEnabled(false);
        licenseIncludedRadioButton.setSelected(true);
      } else {
        //dedicatedInfrastructureRadioButton.setEnabled(true);
        ocpuCountSpinner.setEnabled(true);
        storageSpinnerNumberModel.setValue(AutonomousDatabaseConstants.STORAGE_IN_TB_DEFAULT);
        storageSpinner.setEnabled(true);
        autoScalingCheckBox.setSelected(true);
        autoScalingCheckBox.setEnabled(true);
        //privateEndPointAccessOnlyRadioButton.setEnabled(true);
        bringYourOwnLicenseRadioButton.setEnabled(true);
        bringYourOwnLicenseRadioButton.setSelected(false);
        licenseIncludedRadioButton.setSelected(false);
      }
    });

    // Initialize the spinners with defaults.
    ocpuCountSpinner.setModel(new SpinnerNumberModel(AutonomousDatabaseConstants.CPU_CORE_COUNT_DEFAULT,
                    AutonomousDatabaseConstants.CPU_CORE_COUNT_MIN, AutonomousDatabaseConstants.CPU_CORE_COUNT_MAX,
                    AutonomousDatabaseConstants.CPU_CORE_COUNT_INCREMENT));
    storageSpinner.setModel(storageSpinnerNumberModel);

    usernameTextField.setText(AutonomousDatabaseConstants.DATABASE_DEFAULT_USERNAME);

    // Add action listener for Data Warehouse workload type radio button.
    dataWarehouseRadioButton.addActionListener(actionEvent -> {
      sharedInfrastructureRadioButton.setEnabled(true);
      //dedicatedInfrastructureRadioButton.setEnabled(true);

      bringYourOwnLicenseRadioButton.setEnabled(true);
      bringYourOwnLicenseRadioButton.setSelected(false);
      licenseIncludedRadioButton.setEnabled(true);
      licenseIncludedRadioButton.setSelected(false);
    });

    // Add action listener for Transaction Processing workload type radio button.
    transactionProcessingRadioButton.addActionListener(actionEvent -> {
      sharedInfrastructureRadioButton.setEnabled(true);
      //dedicatedInfrastructureRadioButton.setEnabled(true);

      bringYourOwnLicenseRadioButton.setEnabled(true);
      bringYourOwnLicenseRadioButton.setSelected(false);
      licenseIncludedRadioButton.setEnabled(true);
      licenseIncludedRadioButton.setSelected(false);
    });

    // Add action listener for JSON workload type radio button.
    jsonRadioButton.addActionListener(actionEvent -> {
      sharedInfrastructureRadioButton.setEnabled(true);
      // Dedicated infrastructure deployment type is not supported.
      //dedicatedInfrastructureRadioButton.setEnabled(false);

      bringYourOwnLicenseRadioButton.setEnabled(false);
      licenseIncludedRadioButton.setEnabled(true);
      licenseIncludedRadioButton.setSelected(true);
    });

    // Add action listener for APEX workload type radio button.
    apexRadioButton.addActionListener(actionEvent -> {
      sharedInfrastructureRadioButton.setEnabled(true);
      // Dedicated infrastructure deployment type is not supported.
      //dedicatedInfrastructureRadioButton.setEnabled(false);

      bringYourOwnLicenseRadioButton.setEnabled(false);
      licenseIncludedRadioButton.setEnabled(true);
      licenseIncludedRadioButton.setSelected(true);
    });

    // Removing secure access and private end-point options in this release.
    secureAccessFromEverywhereRadioButton.setEnabled(false);
    privateEndPointAccessOnlyRadioButton.setEnabled(false);
    configureAccessControlRulesCheckBox.setEnabled(false);
    networkAccessOptionsPanel.setVisible(false);
    virtualCloudNetworkPanel.setVisible(false);
    virtCloudNetworkSubnetPanel.setVisible(false);
    hostNamePrefixPanel.setVisible(false);
    networkSecurityGroupsPanel.setVisible(false);

    // Add action listener for Shared Infrastructure deployment type radio button.
    sharedInfrastructureRadioButton.addActionListener(actionEvent -> {
      //databaseVersionLabel.setVisible(true);
      //databaseVersionComboBox.setVisible(true);

      jsonRadioButton.setEnabled(true);
      apexRadioButton.setEnabled(true);
      autonomousContainerDatabasePanel.setVisible(false);
      //networkAccessOptionsPanel.setVisible(true);
      licenseTypePanel.setVisible(true);

      alwaysFreeLabel.setVisible(true);
      showOnlyAlwaysFreeCheckBox.setVisible(true);

      provideTenMaintenanceContactsPanel.setVisible(true);
      bringYourOwnLicenseRadioButton.setEnabled(!jsonRadioButton.isSelected() && !apexRadioButton.isSelected());
    });

    // Add action listener for Dedicated Infrastructure deployment type radio button.
    dedicatedInfrastructureRadioButton.addActionListener(actionEvent -> {
      autonomousDataGuardEnabledCheckBox.setSelected(false);
      //databaseVersionLabel.setVisible(false);
      //databaseVersionComboBox.setVisible(false);

      jsonRadioButton.setEnabled(false);
      apexRadioButton.setEnabled(false);
      autonomousContainerDatabasePanel.setVisible(true);
      //networkAccessOptionsPanel.setVisible(false);
      licenseTypePanel.setVisible(false);

      alwaysFreeLabel.setVisible(false);
      showOnlyAlwaysFreeCheckBox.setVisible(false);

      provideTenMaintenanceContactsPanel.setVisible(false);

      bringYourOwnLicenseRadioButton.setEnabled(!jsonRadioButton.isSelected() && !apexRadioButton.isSelected());
    });

    secureAccessFromEverywhereRadioButton.addActionListener(actionEvent -> {
      configureAccessControlRulesPanel.setVisible(true);
      ipNotationPanel.setVisible(false);

      virtualCloudNetworkPanel.setVisible(false);
      virtCloudNetworkSubnetPanel.setVisible(false);
      hostNamePrefixPanel.setVisible(false);
      networkSecurityGroupsPanel.setVisible(false);
      anotherNetworkSecurityGroupPanel.setVisible(false);
    });

    privateEndPointAccessOnlyRadioButton.addActionListener(actionEvent -> {
      configureAccessControlRulesPanel.setVisible(false);

      virtualCloudNetworkPanel.setVisible(true);
      virtCloudNetworkSubnetPanel.setVisible(true);
      hostNamePrefixPanel.setVisible(true);
      networkSecurityGroupsPanel.setVisible(true);
      anotherNetworkSecurityGroupPanel.setVisible(true);
    });

    configureAccessControlRulesCheckBox.addActionListener(actionEvent -> {
      if (configureAccessControlRulesCheckBox.isSelected()) {
        ipNotationPanel.setVisible(true);
        ipNotationAddEntryPanel.setVisible(true);
      } else {
        ipNotationPanel.setVisible(false);
        ipNotationAddEntryPanel.setVisible(false);
      }
    });

    changeCompartmentButton.addActionListener(actionEvent -> {
      final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

      if(compartmentSelection.showAndGet()) {
        final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
        if(selectedCompartment != null) {
          final Border newTitleBorder = BorderFactory
                  .createTitledBorder(AUTONOMOUS_DATA_GUARD_SUB_PANEL_TEXT + selectedCompartment.getName());
          autonomousDataGuardSubPanel.setBorder(newTitleBorder);
        }
      }
    });

    virtualCloudNetworkChangeCompartmentButton.addActionListener(actionEvent -> {
      final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

      if(compartmentSelection.showAndGet()) {
        final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
        if(selectedCompartment != null) {
          final Border newTitleBorder = BorderFactory
                  .createTitledBorder(VIRTUAL_CLOUD_NETWORK_PANEL_TEXT + selectedCompartment.getName());
          virtualCloudNetworkPanel.setBorder(newTitleBorder);
        }
      }
    });

    virtualCloudNetworkSubnetChangeCompartmentButton.addActionListener(actionEvent -> {
      final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

      if(compartmentSelection.showAndGet()) {
        final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
        if(selectedCompartment != null) {
          final Border newTitleBorder = BorderFactory
                  .createTitledBorder(VIRTUAL_CLOUD_SUBNET_PANEL_TEXT + selectedCompartment.getName());
          virtCloudNetworkSubnetPanel.setBorder(newTitleBorder);
        }
      }
    });

    networkSecurityGroupChangeCompartmentButton.addActionListener(actionEvent -> {
      final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

      if(compartmentSelection.showAndGet()) {
        final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
        if(selectedCompartment != null) {
          final Border newTitleBorder = BorderFactory
                  .createTitledBorder(NETWORK_SECURITY_GROUP_SUB_PANEL_TEXT + selectedCompartment.getName());
          networkSecurityGroupsSubPanel.setBorder(newTitleBorder);
        }
      }
    });
    //addContactPanel.setVisible(false);
  }

  @Override
  protected void doOKAction() {
    final Supplier<Boolean> inputParametersValidator = () -> {
      if (compartmentTextField.getText().isEmpty()) {
        Messages.showErrorDialog("Compartment cannot be empty.",
                "Select a compartment");
        return false;
      } else if (displayNameTextField.getText().isEmpty()) {
        Messages.showErrorDialog("Database display name cannot be empty.",
                "Select a database display name");
        return false;
      } else if (databaseNameTextField.getText().isEmpty()) {
        Messages.showErrorDialog("Database name cannot be empty.",
                "Select a database name");
        return false;
      } else if ((passwordField.getPassword() == null || passwordField.getPassword().length == 0) ||
              (confirmPasswordField.getPassword() == null || confirmPasswordField.getPassword().length == 0)) {
        Messages.showErrorDialog("Password cannot be empty.",
                "Password required");
        return false;
      } else if (!Arrays.equals(passwordField.getPassword(), confirmPasswordField.getPassword())) {
        Messages.showErrorDialog("Password and confirmation must match.",
                "Passwords mismatch");
        return false;
      } else if(!bringYourOwnLicenseRadioButton.isSelected() &&
              !licenseIncludedRadioButton.isSelected()) {
        final String errorMessage = "Select a license type.";
        Messages.showErrorDialog(errorMessage, "License type");
        return false;
      }

      return true;
    };

    if (!inputParametersValidator.get()) {
      return;
    }

    // Declaration of supplier that returns the workload type chosen by user.
    final Supplier<CreateAutonomousDatabaseBase.DbWorkload> workloadTypeSupplier = () -> {
      if (dataWarehouseRadioButton.isSelected()) {
        return CreateAutonomousDatabaseBase.DbWorkload.Dw;
      } else if (transactionProcessingRadioButton.isSelected()) {
        return CreateAutonomousDatabaseBase.DbWorkload.Oltp;
      } else if (jsonRadioButton.isSelected()) {
        return CreateAutonomousDatabaseBase.DbWorkload.Ajd;
      } else if (apexRadioButton.isSelected()) {
        return CreateAutonomousDatabaseBase.DbWorkload.Apex;
      }

      final String errorMessage = "No workload type is selected.";
      Messages.showErrorDialog(errorMessage, "Select a workload type");
      throw new IllegalStateException(errorMessage);
    };

    final CreateAutonomousDatabaseDetails.Builder createAutonomousDatabaseDetailsBuilder =
            CreateAutonomousDatabaseDetails.builder();

    // Build the common parameters.
    createAutonomousDatabaseDetailsBuilder
            .compartmentId(selectedCompartment.getId())
            .displayName(displayNameTextField.getText())
            .dbName(databaseNameTextField.getText())
            .dbWorkload(workloadTypeSupplier.get())
            .cpuCoreCount((Integer) ocpuCountSpinner.getValue())
            .adminPassword(String.valueOf(passwordField.getPassword()));

    if (showOnlyAlwaysFreeCheckBox.isSelected()) {
      configureAlwaysFreeParameters(createAutonomousDatabaseDetailsBuilder);
    } else {
      createAutonomousDatabaseDetailsBuilder
              .dataStorageSizeInTBs((Integer) storageSpinner.getValue());

      if (sharedInfrastructureRadioButton.isSelected()) {
        configureParametersForDeploymentInSharedInfra(createAutonomousDatabaseDetailsBuilder);
      } else if (dedicatedInfrastructureRadioButton.isSelected()) {
        configureParametersForDeploymentInDedicatedInfra(createAutonomousDatabaseDetailsBuilder);
      }
    }

    submitRequest(createAutonomousDatabaseDetailsBuilder);
    close(DialogWrapper.OK_EXIT_CODE);
  }

  private void configureAlwaysFreeParameters(
          CreateAutonomousDatabaseDetails.Builder createAutonomousDatabaseDetailsBuilder) {
    createAutonomousDatabaseDetailsBuilder
            .isFreeTier(true)
            /* The OCI cloud web console shows 0.02 TB as default for
               always free account. But the API expect minimum 1 TB.
               So to make this work, we show 0.02 in the plugin UI
               and we supply 1 TB as default storage size in the API call.
             */
            .dataStorageSizeInTBs(AutonomousDatabaseConstants.ALWAYS_FREE_STORAGE_TB_DUMMY)
            .isDedicated(false)
            .licenseModel(CreateAutonomousDatabaseBase.LicenseModel.LicenseIncluded);
  }

  private void submitRequest(CreateAutonomousDatabaseDetails.Builder createAutonomousDatabaseDetailsBuilder) {
    // Prepare the asynchronous request.
    Runnable createAtpDbRequestRunnable = () -> {
      try {
        OracleCloudAccount.getInstance()
                .getDatabaseClient().createInstance(createAutonomousDatabaseDetailsBuilder.build());
        UIUtil.fireNotification(NotificationType.INFORMATION, "Autonomous Database instance created successfully.", "Create");
      } catch (Exception ex) {
        UIUtil.fireNotification(NotificationType.ERROR,"Autonomous Database instance creation failed : " + ex.getMessage(), null);
      }
    };

    // Do this asynchronously.
    UIUtil.executeAndUpdateUIAsync(createAtpDbRequestRunnable, null);
  }

  private void configureParametersForDeploymentInSharedInfra(
          final CreateAutonomousDatabaseDetails.Builder createAutonomousDatabaseDetailsBuilder) {

    // This is not dedicated infra deployment. This is Shared infra.
    final boolean isDedicatedInfra = false;

    createAutonomousDatabaseDetailsBuilder
            .isAutoScalingEnabled(autoScalingCheckBox.isSelected())
            .isDedicated(isDedicatedInfra);

    // Declaration of supplier that returns the license type chosen by user.
    final Supplier<CreateAutonomousDatabaseBase.LicenseModel> licenseModelSupplier = () ->
    {
      if (bringYourOwnLicenseRadioButton.isSelected()) {
        return CreateAutonomousDatabaseBase.LicenseModel.BringYourOwnLicense;
      } else {
        return CreateAutonomousDatabaseBase.LicenseModel.LicenseIncluded;
      }
    };

    if (dataWarehouseRadioButton.isSelected() ||
      transactionProcessingRadioButton.isSelected()) {
      createAutonomousDatabaseDetailsBuilder.licenseModel(licenseModelSupplier.get());
    } else if (jsonRadioButton.isSelected() ||
            apexRadioButton.isSelected()) {
      // For JSON or APEX workload types, only "LicenseIncluded" type is given.
      createAutonomousDatabaseDetailsBuilder
              .licenseModel(CreateAutonomousDatabaseBase.LicenseModel.LicenseIncluded);
    }

    final Function<String, List<CustomerContact>> maintenanceContactsExtractor = (maintenanceContacts) -> {
      final List<CustomerContact> listOfCustomerContact = new ArrayList<>();
      final StringTokenizer contactsTokenizer = new StringTokenizer(maintenanceContacts.trim(), ";");

      for (int i = 0; contactsTokenizer.hasMoreElements() && i < 10; i++) {
        final String email = contactsTokenizer.nextToken().trim();
        listOfCustomerContact.add(CustomerContact.builder().email(email).build());
      }
      return listOfCustomerContact;
    };

    createAutonomousDatabaseDetailsBuilder
            .customerContacts(maintenanceContactsExtractor.apply(contactEmailTextField.getText()));
  }

  private void configureParametersForDeploymentInDedicatedInfra(
          final CreateAutonomousDatabaseDetails.Builder createAutonomousDatabaseDetailsBuilder) {
      createAutonomousDatabaseDetailsBuilder
            .dataStorageSizeInTBs((Integer) storageSpinner.getValue())
            .isDedicated(true)
            .isDataGuardEnabled(autonomousDataGuardEnabledCheckBox.isSelected())
            .autonomousContainerDatabaseId(autonomousContainerDatabaseTextField.getText());
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
      return new JBScrollPane(mainPanel);
  }
}
