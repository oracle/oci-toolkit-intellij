package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseCloneDetails;
import com.oracle.bmc.database.model.CustomerContact;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.common.AutonomousDatabaseConstants;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CreateAutonomousDatabaseCloneDialog extends DialogWrapper {
  private static final String WINDOW_TITLE = "Create Autonomous Database Clone";
  private JPanel mainPanel;
  private JPanel chooseCloneTypePanel;
  private JPanel cloneSourcePanel;
  private JRadioButton fullCloneRadioButton;
  private JRadioButton refreshableCloneRadioButton;
  private JRadioButton metadataCloneRadioButton;
  private JRadioButton cloneFromDatabaseInstanceRadioButton;
  private JRadioButton cloneFromABackupRadioButton;
  private JLabel sourceDatabaseNameLabel;
  private JTextField sourceDatabaseNameTextField;
  private JLabel displayNameLabel;
  private JTextField displayNameTextField;
  private JLabel databaseNameLabel;
  private JTextField databaseNameTextField;
  private JLabel chooseDatabaseVersionLabel;
  private JComboBox chooseDatabaseVersionComboBox;
  private JPanel configureDatabasePanel;
  private JLabel ocpuCountLabel;
  private JSpinner ocpuCountSpinner;
  private JLabel storageLabel;
  private JSpinner storageSpinner;
  private JCheckBox autoScalingCheckBox;
  private JPanel createAdministratorCredentialsPanel;
  private JLabel usernameLabel;
  private JLabel passwordLabel;
  private JPasswordField passwordField;
  private JPasswordField confirmPasswordField;
  private JTextField usernameTextField;
  private JLabel confirmPasswordLabel;
  private JPanel chooseNetworkAccessPanel;
  private JPanel accessTypePanel;
  private JRadioButton secureAccessFromEveryWhereRadioButton;
  private JRadioButton privateEndPointAccessOnlyRadioButton;
  private JCheckBox configureAccessControlRulesCheckBox;
  private JPanel ipNotationTypeOuterPanel;
  private JPanel ipNotationTypeInnerPanel;
  private JPanel ipNotationValuesPanel;
  private JComboBox ipNotationComboBox;
  private JTextField ipNotationValuesTextField;
  private JRadioButton bringYourOwnLicenseRadioButton;
  private JRadioButton licenseIncludedRadioButton;
  private JPanel virtualCloudNetworkOuterPanel;
  private JPanel virtualCloudNetworkInnerPanel;
  private JButton virtualNetworkChangeCompartmentButton;
  private JPanel subnetPanel;
  private JButton subnetChangeCompartmentButton;
  private JComboBox subnetComboBox;
  private JComboBox nsgComboBox;
  private JPanel nsgOuterPanel;
  private JPanel nsgInnerPanel;
  private JButton nsgChangeCompartmentButton;
  private JButton anotherNetworkSecurityGroupButton;
  private JComboBox virtualCloudNetworkComboBox;
  private JPanel anotherEntrySpacingPanel;
  private JButton anotherEntryButton;
  private JTextField compartmentTextField;
  private JButton selectCompartmentButton;
  private JPanel basicInformationPanel;
  private JPanel createInCompartmentPanel;
  private JTextField contactEmailTextField;
  private JButton addContactButton;
  private JPanel addContactPanel;
  private JPanel contactEmailPanel;
  private JPanel provideTenMaintenanceContactsPanel;
  private Compartment selectedCompartment;

  private static final String VIRTUAL_CLOUD_NETWORK_PANEL_TEXT = "Virtual cloud network in ";
  private static final String SUBNET_PANEL_TEXT = "Subnet in ";
  private static final String NETWORK_SECURITY_GROUP_PANEL_TEXT = "Network security group in ";

  private static int contactEmailTextFieldsCount = 1;

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

    // CreateAutonomousDatabaseCloneDetails.CloneType does not have
    // "Refreshable clone" type. So removed from UI.
    refreshableCloneRadioButton.setVisible(false);

    final ButtonGroup cloneSourceButtonGroup = new ButtonGroup();
    cloneSourceButtonGroup.add(cloneFromDatabaseInstanceRadioButton);
    cloneSourceButtonGroup.add(cloneFromABackupRadioButton);

    // Support this option in the next release.
    cloneFromABackupRadioButton.setVisible(false);

    final ButtonGroup accessTypeButtonGroup = new ButtonGroup();
    accessTypeButtonGroup.add(secureAccessFromEveryWhereRadioButton);
    accessTypeButtonGroup.add(privateEndPointAccessOnlyRadioButton);

    final ButtonGroup licenseTypeButtonGroup = new ButtonGroup();
    licenseTypeButtonGroup.add(bringYourOwnLicenseRadioButton);
    licenseTypeButtonGroup.add(licenseIncludedRadioButton);

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
    chooseNetworkAccessPanel.setVisible(false);

    // Set the initial border title for panels that show compartment name, on selection, in its title.
    Border initialTitleBorder = BorderFactory.createTitledBorder(VIRTUAL_CLOUD_NETWORK_PANEL_TEXT);
    virtualCloudNetworkInnerPanel.setBorder(initialTitleBorder);

    virtualNetworkChangeCompartmentButton.addActionListener(actionEvent -> {
      final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

      if(compartmentSelection.showAndGet()) {
        final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
        if(selectedCompartment != null) {
          final Border newTitleBorder = BorderFactory
                  .createTitledBorder(VIRTUAL_CLOUD_NETWORK_PANEL_TEXT + selectedCompartment.getName());
          virtualCloudNetworkInnerPanel.setBorder(newTitleBorder);
        }
      }
    });

    initialTitleBorder = BorderFactory.createTitledBorder(SUBNET_PANEL_TEXT);
    subnetPanel.setBorder(initialTitleBorder);

    subnetChangeCompartmentButton.addActionListener(actionEvent -> {
      final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

      if(compartmentSelection.showAndGet()) {
        final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
        if(selectedCompartment != null) {
          final Border newTitleBorder = BorderFactory
                  .createTitledBorder(SUBNET_PANEL_TEXT + selectedCompartment.getName());
          subnetPanel.setBorder(newTitleBorder);
        }
      }
    });

    initialTitleBorder = BorderFactory.createTitledBorder(NETWORK_SECURITY_GROUP_PANEL_TEXT);
    nsgInnerPanel.setBorder(initialTitleBorder);

    nsgChangeCompartmentButton.addActionListener(actionEvent -> {
      final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

      if(compartmentSelection.showAndGet()) {
        final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
        if(selectedCompartment != null) {
          final Border newTitleBorder = BorderFactory
                  .createTitledBorder(NETWORK_SECURITY_GROUP_PANEL_TEXT + selectedCompartment.getName());
          nsgInnerPanel.setBorder(newTitleBorder);
        }
      }
    });

    ipNotationTypeOuterPanel.setVisible(false);
    virtualCloudNetworkOuterPanel.setVisible(false);

    fullCloneRadioButton.addActionListener((event) -> {
      cloneSourcePanel.setVisible(true);
    });

    refreshableCloneRadioButton.addActionListener((event) -> {
      cloneSourcePanel.setVisible(false);
    });

    metadataCloneRadioButton.addActionListener((event) -> {
      cloneSourcePanel.setVisible(true);
    });

    secureAccessFromEveryWhereRadioButton.addActionListener((event) -> {
      virtualCloudNetworkOuterPanel.setVisible(false);

      configureAccessControlRulesCheckBox.setVisible(true);
      if (configureAccessControlRulesCheckBox.isSelected()) {
        ipNotationTypeOuterPanel.setVisible(true);
      } else {
        ipNotationTypeOuterPanel.setVisible(false);
      }
    });

    privateEndPointAccessOnlyRadioButton.addActionListener((event) -> {
      ipNotationTypeOuterPanel.setVisible(false);
      virtualCloudNetworkOuterPanel.setVisible(true);
      configureAccessControlRulesCheckBox.setVisible(false);
    });

    configureAccessControlRulesCheckBox.addActionListener((event) -> {
      virtualCloudNetworkOuterPanel.setVisible(false);

      if (configureAccessControlRulesCheckBox.isSelected()) {
        ipNotationTypeOuterPanel.setVisible(true);
      } else {
        ipNotationTypeOuterPanel.setVisible(false);
      }
    });
    addContactPanel.setVisible(false);

    final JBScrollPane jbScrollPane = new JBScrollPane(mainPanel);
    jbScrollPane.createVerticalScrollBar();
    jbScrollPane.createHorizontalScrollBar();
    getContentPane().add(jbScrollPane);
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
      } else if (!String.valueOf(passwordField.getPassword())
              .equals(String.valueOf(confirmPasswordField.getPassword()))) {
        Messages.showErrorDialog("Password and confirmation must match.",
                "Passwords mismatch");
        return false;
      }

      return true;
    };

    if (inputParametersValidator.get() == false) {
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
      } else if (licenseIncludedRadioButton.isSelected()) {
        return CreateAutonomousDatabaseBase.LicenseModel.LicenseIncluded;
      }

      final String errorMessage = "No license type is selected.";
      Messages.showErrorDialog(errorMessage, "Select a license type");
      throw new IllegalStateException(errorMessage);
    };
    
    createAutonomousDatabaseCloneDetailsBuilder
            .licenseModel(licenseModelSupplier.get());

    final Function<String, List<CustomerContact>> maintenanceContactsExtractor = (maintenanceContacts) -> {
      final List<CustomerContact> listOfCustomerContact = new ArrayList<>();
      final StringTokenizer contactsTokenizer = new StringTokenizer(maintenanceContacts, ";");

      for (int i = 0; contactsTokenizer.hasMoreElements() && i < 10; i++) {
        String email = contactsTokenizer.nextToken().trim();
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
        ApplicationManager.getApplication().invokeLater(() -> {
          UIUtil.fireNotification(NotificationType.INFORMATION,"Autonomous Database instance cloned successfully.");
          SystemPreferences.fireADBInstanceUpdateEvent("Clone");
        });
      } catch (Exception e) {
        ApplicationManager.getApplication().invokeLater(
                () -> UIUtil.fireNotification(NotificationType.ERROR, "Failed to clone Autonomous Database instance : " + e.getMessage()));
      }
    };

    // Do this asynchronously.
    UIUtil.executeAndUpdateUIAsync(createAtpDbRequestRunnable, null);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel(){
    return mainPanel;
  }
}
