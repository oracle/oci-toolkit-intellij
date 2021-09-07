/*
  Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseCloneDetails;
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

public class CreateAutonomousDatabaseCloneDialog extends DialogWrapper {
  private static final String WINDOW_TITLE = "Create Clone";
  private JPanel mainPanel;
  private JPanel cloneSourcePanel;
  private JRadioButton fullCloneRadioButton;
  private JRadioButton refreshableCloneRadioButton;
  private JRadioButton metadataCloneRadioButton;
  private JRadioButton cloneFromDatabaseInstanceRadioButton;
  private JRadioButton cloneFromABackupRadioButton;
  private JTextField sourceDatabaseNameTextField;
  private JTextField displayNameTextField;
  private JTextField databaseNameTextField;
  private JComboBox<String> chooseDatabaseVersionComboBox;
  private JSpinner ocpuCountSpinner;
  private JSpinner storageSpinner;
  private JCheckBox autoScalingCheckBox;
  private JPasswordField passwordField;
  private JPasswordField confirmPasswordField;
  private JTextField usernameTextField;
  private JPanel chooseNetworkAccessOptionsPanel;
  private JPanel ipNotationTypeOuterPanel;
  private JRadioButton bringYourOwnLicenseRadioButton;
  private JRadioButton licenseIncludedRadioButton;
  private JPanel virtualCloudNetworkOuterPanel;
  private JPanel virtualCloudNetworkInnerPanel;
  private JButton virtualNetworkChangeCompartmentButton;
  private JPanel subnetPanel;
  private JButton subnetChangeCompartmentButton;
  private JPanel nsgInnerPanel;
  private JButton nsgChangeCompartmentButton;
  private JTextField compartmentTextField;
  private JButton selectCompartmentButton;
  private JTextField contactEmailTextField;
  private JPanel addContactPanel;
  private JRadioButton secureAccessFromEverywhereRadioButton;
  private JRadioButton privateEndpointAccessOnlyRadioButton;
  private JCheckBox configureAccessControlRulesCheckBox;
  private Compartment selectedCompartment;

  private static final String VIRTUAL_CLOUD_NETWORK_PANEL_TEXT = "Virtual cloud network in ";
  private static final String SUBNET_PANEL_TEXT = "Subnet in ";
  private static final String NETWORK_SECURITY_GROUP_PANEL_TEXT = "Network security group in ";

  private final AutonomousDatabaseSummary adbSummary;

  public CreateAutonomousDatabaseCloneDialog(AutonomousDatabaseSummary adbSummary) {
    super(true);
    init();
    setTitle(WINDOW_TITLE);
    setOKButtonText(WINDOW_TITLE);
    this.adbSummary = adbSummary;

    final ButtonGroup cloneTypeButtonGroup = new ButtonGroup();
    cloneTypeButtonGroup.add(fullCloneRadioButton);
    cloneTypeButtonGroup.add(refreshableCloneRadioButton);
    cloneTypeButtonGroup.add(metadataCloneRadioButton);

    final ButtonGroup cloneSourceButtonGroup = new ButtonGroup();
    cloneSourceButtonGroup.add(cloneFromDatabaseInstanceRadioButton);
    cloneSourceButtonGroup.add(cloneFromABackupRadioButton);

    // CreateAutonomousDatabaseCloneDetails.CloneType does not have
    // "Refreshable clone" type. So removed from UI.
    refreshableCloneRadioButton.setEnabled(false);
    fullCloneRadioButton.setSelected(true);
    cloneFromDatabaseInstanceRadioButton.setSelected(true);
    // Support this option in the next release.
    cloneFromABackupRadioButton.setEnabled(false);

    final ButtonGroup accessTypeButtonGroup = new ButtonGroup();
    accessTypeButtonGroup.add(secureAccessFromEverywhereRadioButton);
    accessTypeButtonGroup.add(privateEndpointAccessOnlyRadioButton);

    final ButtonGroup licenseTypeButtonGroup = new ButtonGroup();
    licenseTypeButtonGroup.add(bringYourOwnLicenseRadioButton);
    licenseTypeButtonGroup.add(licenseIncludedRadioButton);

    bringYourOwnLicenseRadioButton.setSelected(false);
    licenseIncludedRadioButton.setSelected(false);

    selectCompartmentButton.addActionListener(actionEvent -> {
      final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

      if(compartmentSelection.showAndGet()) {
        selectedCompartment = compartmentSelection.getSelectedCompartment();
        compartmentTextField.setText(selectedCompartment.getName());
      }
    });

    // Set database name and display name with default values.
    sourceDatabaseNameTextField.setText(adbSummary.getDbName());
    displayNameTextField.setText("Clone of " + adbSummary.getDbName());

    final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
    final String presentDateTime =  DATE_TIME_FORMAT.format(new Date());
    final String databaseNameDefault = "DB" + presentDateTime;
    databaseNameTextField.setText(databaseNameDefault);

    // Initialize the spinners with defaults.
    chooseDatabaseVersionComboBox.addItem(adbSummary.getDbVersion());
    ocpuCountSpinner.setModel(new SpinnerNumberModel(adbSummary.getCpuCoreCount().intValue(),
            AutonomousDatabaseConstants.CPU_CORE_COUNT_MIN, AutonomousDatabaseConstants.CPU_CORE_COUNT_MAX,
            AutonomousDatabaseConstants.CPU_CORE_COUNT_INCREMENT));
    storageSpinner.setModel(new SpinnerNumberModel(adbSummary.getDataStorageSizeInTBs().intValue(),
            AutonomousDatabaseConstants.STORAGE_IN_TB_MIN, AutonomousDatabaseConstants.STORAGE_IN_TB_MAX,
            AutonomousDatabaseConstants.STORAGE_IN_TB_INCREMENT));
    autoScalingCheckBox.setSelected(true);

    usernameTextField.setText(AutonomousDatabaseConstants.DATABASE_DEFAULT_USERNAME);
    // Removing secure access and private end-point options in this release.
    secureAccessFromEverywhereRadioButton.setEnabled(false);
    secureAccessFromEverywhereRadioButton.setSelected(true);
    privateEndpointAccessOnlyRadioButton.setEnabled(false);
    configureAccessControlRulesCheckBox.setEnabled(false);
    configureAccessControlRulesCheckBox.setSelected(false);
    chooseNetworkAccessOptionsPanel.setVisible(false);

    // Set the initial border title for panels that show compartment name, on selection, in its title.
    Border initialTitleBorder = BorderFactory.createTitledBorder(VIRTUAL_CLOUD_NETWORK_PANEL_TEXT);
    virtualCloudNetworkInnerPanel.setBorder(initialTitleBorder);

    virtualNetworkChangeCompartmentButton.addActionListener(actionEvent -> {
      final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

      if(compartmentSelection.showAndGet()) {
        final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
        final Border newTitleBorder = BorderFactory
                .createTitledBorder(VIRTUAL_CLOUD_NETWORK_PANEL_TEXT + selectedCompartment.getName());
        virtualCloudNetworkInnerPanel.setBorder(newTitleBorder);
      }
    });

    initialTitleBorder = BorderFactory.createTitledBorder(SUBNET_PANEL_TEXT);
    subnetPanel.setBorder(initialTitleBorder);

    subnetChangeCompartmentButton.addActionListener(actionEvent -> {
      final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

      if(compartmentSelection.showAndGet()) {
        final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
        final Border newTitleBorder = BorderFactory
                .createTitledBorder(SUBNET_PANEL_TEXT + selectedCompartment.getName());
        subnetPanel.setBorder(newTitleBorder);
      }
    });

    initialTitleBorder = BorderFactory.createTitledBorder(NETWORK_SECURITY_GROUP_PANEL_TEXT);
    nsgInnerPanel.setBorder(initialTitleBorder);

    nsgChangeCompartmentButton.addActionListener(actionEvent -> {
      final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

      if(compartmentSelection.showAndGet()) {
        final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
        final Border newTitleBorder = BorderFactory
                .createTitledBorder(NETWORK_SECURITY_GROUP_PANEL_TEXT + selectedCompartment.getName());
        nsgInnerPanel.setBorder(newTitleBorder);
      }
    });

    ipNotationTypeOuterPanel.setVisible(false);
    virtualCloudNetworkOuterPanel.setVisible(false);

    fullCloneRadioButton.addActionListener((event) -> cloneSourcePanel.setVisible(true));

    refreshableCloneRadioButton.addActionListener((event) -> cloneSourcePanel.setVisible(false));

    metadataCloneRadioButton.addActionListener((event) -> cloneSourcePanel.setVisible(true));

    secureAccessFromEverywhereRadioButton.addActionListener((event) -> {
      virtualCloudNetworkOuterPanel.setVisible(false);

      configureAccessControlRulesCheckBox.setVisible(true);
      ipNotationTypeOuterPanel.setVisible(configureAccessControlRulesCheckBox.isSelected());
    });

    privateEndpointAccessOnlyRadioButton.addActionListener((event) -> {
      ipNotationTypeOuterPanel.setVisible(false);
      virtualCloudNetworkOuterPanel.setVisible(true);
      configureAccessControlRulesCheckBox.setVisible(false);
    });

    configureAccessControlRulesCheckBox.addActionListener((event) -> {
      virtualCloudNetworkOuterPanel.setVisible(false);
      ipNotationTypeOuterPanel.setVisible(configureAccessControlRulesCheckBox.isSelected());
    });
    addContactPanel.setVisible(false);
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
        Messages.showErrorDialog("ADMIN passwords don't match.",
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

    final Supplier<CreateAutonomousDatabaseCloneDetails.CloneType> cloneTypeSupplier = () -> {
      if (fullCloneRadioButton.isSelected()) {
        return CreateAutonomousDatabaseCloneDetails.CloneType.Full;
      } else if (metadataCloneRadioButton.isSelected()) {
        return CreateAutonomousDatabaseCloneDetails.CloneType.Metadata;
      }

      final String errorMessage = "No clone type is selected.";
      Messages.showErrorDialog(errorMessage, "Select a clone type");
      throw new IllegalStateException(errorMessage);
    };

    final CreateAutonomousDatabaseCloneDetails.Builder createAutonomousDatabaseCloneDetailsBuilder
            = CreateAutonomousDatabaseCloneDetails.builder();

    final Supplier<CreateAutonomousDatabaseBase.DbWorkload> workloadSupplier = () -> {
      if (adbSummary.getDbWorkload() == AutonomousDatabaseSummary.DbWorkload.Dw) {
        return CreateAutonomousDatabaseBase.DbWorkload.Dw;
      } else if (adbSummary.getDbWorkload() == AutonomousDatabaseSummary.DbWorkload.Ajd) {
        return CreateAutonomousDatabaseBase.DbWorkload.Ajd;
      } else if (adbSummary.getDbWorkload() == AutonomousDatabaseSummary.DbWorkload.Oltp) {
        return CreateAutonomousDatabaseBase.DbWorkload.Oltp;
      } else if (adbSummary.getDbWorkload() == AutonomousDatabaseSummary.DbWorkload.Apex) {
        return CreateAutonomousDatabaseBase.DbWorkload.Apex;
      }

      final String errorMessage = "Unknown workload type : " + adbSummary.getDbWorkload();
      Messages.showErrorDialog(errorMessage, "Workload type");
      throw new IllegalStateException(errorMessage);
    };

    // Build the common parameters.
    createAutonomousDatabaseCloneDetailsBuilder
            .cloneType(cloneTypeSupplier.get())
            .compartmentId(selectedCompartment.getId())
            .sourceId(adbSummary.getId())
            .displayName(displayNameTextField.getText())
            .dbName(databaseNameTextField.getText())
            .dbVersion((String) chooseDatabaseVersionComboBox.getSelectedItem())
            .cpuCoreCount((Integer) ocpuCountSpinner.getValue())
            .dataStorageSizeInTBs((Integer) storageSpinner.getValue())
            .isAutoScalingEnabled(autoScalingCheckBox.isSelected())
            .adminPassword(String.valueOf(passwordField.getPassword()))
            .dbWorkload(workloadSupplier.get())
            .isFreeTier(adbSummary.getIsFreeTier())
            .autonomousContainerDatabaseId(adbSummary.getAutonomousContainerDatabaseId())
            .isDataGuardEnabled(adbSummary.getIsDataGuardEnabled())
            .isDedicated(adbSummary.getIsDedicated())
            .privateEndpointLabel(adbSummary.getPrivateEndpointLabel())
            .freeformTags(adbSummary.getFreeformTags())
            .definedTags(adbSummary.getDefinedTags())
            .isPreviewVersionWithServiceTermsAccepted(adbSummary.getIsPreview());

    // Declaration of supplier that returns the license type chosen by user.
    final Supplier<CreateAutonomousDatabaseBase.LicenseModel> licenseModelSupplier = () -> {
      if (bringYourOwnLicenseRadioButton.isSelected()) {
        return CreateAutonomousDatabaseBase.LicenseModel.BringYourOwnLicense;
      } else {
        return CreateAutonomousDatabaseBase.LicenseModel.LicenseIncluded;
      }
    };

    createAutonomousDatabaseCloneDetailsBuilder
            .licenseModel(licenseModelSupplier.get());

    final Function<String, List<CustomerContact>> maintenanceContactsExtractor = (maintenanceContacts) -> {
      final List<CustomerContact> listOfCustomerContact = new ArrayList<>();
      final StringTokenizer contactsTokenizer = new StringTokenizer(maintenanceContacts.trim(), ";");

      for (int i = 0; contactsTokenizer.hasMoreElements() && i < 10; i++) {
        final String email = contactsTokenizer.nextToken().trim();
        listOfCustomerContact.add(CustomerContact.builder().email(email).build());
      }
      return listOfCustomerContact;
    };

    createAutonomousDatabaseCloneDetailsBuilder
            .customerContacts(maintenanceContactsExtractor.apply(contactEmailTextField.getText()));

    submitRequest(createAutonomousDatabaseCloneDetailsBuilder);
    close(DialogWrapper.OK_EXIT_CODE);
  }

  private void submitRequest(CreateAutonomousDatabaseCloneDetails.Builder createAutonomousDatabaseCloneDetailsBuilder) {
    // Prepare asynchronous request.
    final Runnable createAtpDbRequestRunnable = () -> {
      try {
        OracleCloudAccount.getInstance().getDatabaseClient().createClone(createAutonomousDatabaseCloneDetailsBuilder.build());
        UIUtil.fireNotification(NotificationType.INFORMATION,"Autonomous Database instance cloned successfully.", "Clone");
      } catch (Exception e) {
        UIUtil.fireNotification(NotificationType.ERROR, "Failed to clone Autonomous Database instance : " + e.getMessage(), null);
      }
    };

    // Do this asynchronously.
    UIUtil.executeAndUpdateUIAsync(createAtpDbRequestRunnable, null);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel(){
    return new JBScrollPane(mainPanel);
  }
}
