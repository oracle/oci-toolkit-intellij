/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.IdentClient;
import com.oracle.oci.intellij.account.PreferencesWrapper;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.ADBConstants;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;
import org.jetbrains.annotations.Nullable;

import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase.DbWorkload;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseDetails;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseDetails.Builder;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class CreateADBInstanceWizard extends DialogWrapper {

  private static final String PASSWORD_TOOlTIP =
      "Password must be 12 to 30 characters and contain at least one uppercase letter,\n"
          + " one lowercase letter, and one number. The password cannot contain the double \n"
          + "quote (\") character or the username \"admin\".";
  private static final String DEFAULT_USERNAME = "ADMIN";

  private JPanel mainPanel;
  private JPanel topPanel;
  private JPanel bottomPanel;
  private JPanel centerPanel;
  private JTextField displayNameTxt;
  private JTextField compartmentCmb;
  private JTextField dbNameTxt;
  private JCheckBox alwayFreeChk;
  private JSpinner cpuCountSpnr;
  private JSpinner storageSpnr;
  private JCheckBox autoScalingChk;
  private JTextField userNameTxt;
  private JPasswordField passwordTxt;
  private JPasswordField confirmPasswordTxt;
  private JRadioButton dedicatedRBtn;
  private JRadioButton serverlessRBtn;
  private JRadioButton byolRBtn;
  private JRadioButton licenseIncldBtn;
  private JPanel licenseTypePanel;
  private JPanel deploymentTypePanel;
  private JTextField adcCompartmentCmb;
  private JComboBox databaseContainerCmb;
  private JLabel deploymentLabel;
  private JLabel adcCompartmentLabel;
  private JLabel dbContainerLabel;
  private JLabel pwdInstructionLbl;
  private JButton adcCompartmentBtn;
  private JButton compartmentBtn;
  private ButtonGroup licenseTypeGrp;
  private ButtonGroup deploymentTypeGrp;

  private DbWorkload workloadType;
  private Compartment selectedCompartment;
  private Compartment selectedADCCompartment;
  private Map<String, String> containerMap = new TreeMap<>();

  protected CreateADBInstanceWizard(DbWorkload workloadType) {
    super(true);
    setTitle("Create ADB Instance");
    setOKButtonText("Create");
    init();
    licenseTypeGrp = new ButtonGroup();
    deploymentTypeGrp = new ButtonGroup();
    licenseTypeGrp.add(byolRBtn);
    licenseTypeGrp.add(licenseIncldBtn);
    deploymentTypeGrp.add(dedicatedRBtn);
    deploymentTypeGrp.add(serverlessRBtn);
    adcCompartmentCmb.setEditable(false);
    databaseContainerCmb.setEnabled(false);
    adcCompartmentBtn.setEnabled(false);
    this.workloadType = workloadType;

    final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(
        "yyyyMMddHHmm");
    final String defaultDBName = "DB" + DATE_TIME_FORMAT.format(new Date());
    dbNameTxt.setText(defaultDBName);
    displayNameTxt.setText(defaultDBName);
    cpuCountSpnr.setModel(
        new SpinnerNumberModel(ADBConstants.CPU_CORE_COUNT_DEFAULT,
            ADBConstants.CPU_CORE_COUNT_MIN, ADBConstants.CPU_CORE_COUNT_MAX,
            ADBConstants.CPU_CORE_COUNT_INCREMENT));
    storageSpnr.setModel(
        new SpinnerNumberModel(ADBConstants.STORAGE_IN_TB_DEFAULT,
            ADBConstants.STORAGE_IN_TB_MIN, ADBConstants.STORAGE_IN_TB_MAX,
            ADBConstants.STORAGE_IN_TB_INCREMENT));

    userNameTxt.setText(DEFAULT_USERNAME);
    userNameTxt.setEditable(false);
    passwordTxt.setToolTipText(PASSWORD_TOOlTIP);

    pwdInstructionLbl.setText("<html>Password must be 12 to 30 characters and contain at least one "
        + "uppercase letter, one lowercase letter, and one number. "
        + "The password cannot contain the double quote (\") character or the "
        + "username \"admin\"</html>");

    serverlessRBtn.setSelected(true);
    byolRBtn.setSelected(true);

    selectedCompartment = IdentClient.getInstance().getRootCompartment();
    if(selectedCompartment != null)
      compartmentCmb.setText(selectedCompartment.getName());
    else
      compartmentCmb.setText("Select Compartment");
    compartmentCmb.setEditable(false);

    compartmentBtn.addActionListener((e) -> {
      compartmentBtn.setEnabled(false);
      try {
        CompartmentSelection compartmentSelection = new CompartmentSelection();
        compartmentSelection.showAndGet();
        if(compartmentSelection.isOK()) {
          selectedCompartment = compartmentSelection.getSelectedCompartment();
          if(selectedCompartment != null)
            compartmentCmb.setText(selectedCompartment.getName());
        }
      }
      catch(Exception ex) {
        Messages.showErrorDialog("Unable to load compartment details.", "Select Compartment");
      }
      compartmentBtn.setEnabled(true);
    });

    adcCompartmentBtn.addActionListener((e) -> {
      adcCompartmentBtn.setEnabled(false);
      try {
        CompartmentSelection compartmentSelection = new CompartmentSelection();
        compartmentSelection.showAndGet();
        if(compartmentSelection.isOK()) {
          selectedADCCompartment = compartmentSelection.getSelectedCompartment();
          if(selectedADCCompartment != null) {
            adcCompartmentCmb.setText(selectedADCCompartment.getName());
            handleADCCompartmentChange();
          }
        }
      }
      catch(Exception ex) {
        Messages.showErrorDialog("Unable to load compartment details.", "Select Compartment");
      }
      adcCompartmentBtn.setEnabled(true);
    });

    alwayFreeChk.addChangeListener((e) -> {
      if (alwayFreeChk.isSelected()) {
        autoScalingChk.setSelected(false);
        autoScalingChk.setEnabled(false);
        cpuCountSpnr.setValue(1);
        cpuCountSpnr.setEnabled(false);
        storageSpnr.setValue(0.02);
        storageSpnr.setEnabled(false);
        licenseIncldBtn.setSelected(true);
        serverlessRBtn.setSelected(true);

        dedicatedRBtn.setEnabled(false);
        serverlessRBtn.setEnabled(false);
        byolRBtn.setEnabled(false);
        licenseIncldBtn.setEnabled(false);

      }
      else {
        autoScalingChk.setEnabled(true);
        cpuCountSpnr.setValue(1);
        cpuCountSpnr.setEnabled(true);
        storageSpnr.setValue(1);
        storageSpnr.setEnabled(true);
        byolRBtn.setSelected(true);
        serverlessRBtn.setSelected(true);

        dedicatedRBtn.setEnabled(true);
        serverlessRBtn.setEnabled(true);
        byolRBtn.setEnabled(true);
        licenseIncldBtn.setEnabled(true);

      }
    });
    dedicatedRBtn.addChangeListener((e) -> {
      adcCompartmentCmb.setEnabled(dedicatedRBtn.isSelected());
      databaseContainerCmb.setEnabled(dedicatedRBtn.isSelected());
      adcCompartmentBtn.setEnabled(dedicatedRBtn.isSelected());
      byolRBtn.setEnabled(!dedicatedRBtn.isSelected());
      licenseIncldBtn.setEnabled(!dedicatedRBtn.isSelected());
      if(dedicatedRBtn.isSelected()) {
        selectedADCCompartment = IdentClient.getInstance().getRootCompartment();
        handleADCCompartmentChange();
      }
    });

    // Hide all the OLTP specific controls if open in ADW mode.
    if (workloadType.equals(DbWorkload.Dw)) {
      deploymentTypePanel.setVisible(false);
      deploymentLabel.setVisible(false);
      adcCompartmentBtn.setVisible(false);
      adcCompartmentCmb.setVisible(false);
      adcCompartmentLabel.setVisible(false);
      dbContainerLabel.setVisible(false);
      databaseContainerCmb.setVisible(false);
    }

  }

  private void handleADCCompartmentChange() {
    containerMap.clear();
    databaseContainerCmb.removeAllItems();
    adcCompartmentCmb.setText(selectedADCCompartment.getName());
    containerMap.putAll(
        ADBInstanceClient.getInstance()
            .getContainerDatabaseMap(selectedADCCompartment.getId()));
    containerMap.keySet().forEach((e) -> databaseContainerCmb.addItem(e));
    if(containerMap.size() > 0)
      databaseContainerCmb.setSelectedIndex(0);
  }

  public void doOKAction() {
    if(selectedCompartment == null) {
      Messages.showErrorDialog("Invalid Compartment. Please select a Compartment",
          "Invalid Compartment");
      return;
    }
    final boolean isDedicated = dedicatedRBtn.isSelected();
    final String containerDB = isDedicated ? getContainerDatabaseId() : "";
    if (!isValidPassword() || !isValidContainerDatabase(containerDB))
      return;

    final String compartmentId = selectedCompartment.getId();
    final String storage = alwayFreeChk.isSelected() ?
        ADBConstants.ALWAYS_FREE_STORAGE_TB_DUMMY :
        storageSpnr.getValue().toString();
    final Builder createADBRequestBuilder = CreateAutonomousDatabaseDetails
        .builder().compartmentId(compartmentId)
        .cpuCoreCount(Integer.valueOf(cpuCountSpnr.getValue().toString()))
        .dataStorageSizeInTBs(Integer.parseInt(storage))
        .displayName(displayNameTxt.getText().trim())
        .adminPassword(new String(passwordTxt.getPassword()))
        .dbName(dbNameTxt.getText().trim()).dbWorkload(workloadType)
        .licenseModel(getLicenseModel());

    final CreateAutonomousDatabaseDetails createADBRequest;

    if (isDedicated) {
      createADBRequest = createADBRequestBuilder.isDedicated(true)
          .autonomousContainerDatabaseId(containerDB)
          .build();
    }
    else {
      createADBRequest = createADBRequestBuilder
          .isAutoScalingEnabled(autoScalingChk.isSelected())
          .isFreeTier(alwayFreeChk.isSelected()).build();
    }

    Runnable nonblockingUpdate = () -> {
      try {
        ADBInstanceClient.getInstance().createInstance(createADBRequest);
        ApplicationManager.getApplication().invokeLater(() -> {
          UIUtil.fireSuccessNotification("ADB Instance created successfully.");
          PreferencesWrapper.fireADBInstanceUpdateEvent("Create");
        });
      }
      catch (Exception e) {
        ApplicationManager.getApplication().invokeLater(
            () -> UIUtil.fireErrorNotification("Failed to create ADB Instance : " + e.getMessage()));
      }
    };

    // Do this in background
    UIUtil.fetchAndUpdateUI(nonblockingUpdate, null);

    close(DialogWrapper.OK_EXIT_CODE);
  }

  private CreateAutonomousDatabaseBase.LicenseModel getLicenseModel() {
    if (dedicatedRBtn.isSelected())
      return null;

    if (licenseIncldBtn.isSelected()) {
      return CreateAutonomousDatabaseBase.LicenseModel.LicenseIncluded;
    }
    else {
      return CreateAutonomousDatabaseBase.LicenseModel.BringYourOwnLicense;
    }
  }

  private String getContainerDatabaseId() {
    final String selected = (String)databaseContainerCmb.getSelectedItem();
    if(selected == null)
      return "";
    else
      return containerMap.get(databaseContainerCmb.getSelectedItem());
  }
/*
  public String getStorageInTB() {
    if (alwayFreeChk.isSelected())
      return ADBConstants.ALWAYS_FREE_STORAGE_TB_DUMMY;
    return storageSpnr.getValue().toString();
  }
*/
  private boolean isValidPassword() {
    final String adminPassword = new String(passwordTxt.getPassword());
    final String confirmAdminPassword = new String(
        confirmPasswordTxt.getPassword());

    if (!UIUtil.isValidAdminPassword(adminPassword)) {
      Messages.showErrorDialog("Admin password entered is not valid.",
          "Invalid Password");
      return false;

    }

    if (!adminPassword.equals(confirmAdminPassword)) {
      Messages.showErrorDialog("Confirm Admin password must match Admin password.",
          "Password mismatch error");
      return false;
    }

    return true;
  }

  private boolean isValidContainerDatabase(final String selectedContainerDBID) {
    if(!dedicatedRBtn.isSelected())
      return true; // Not required to check for non-dedicated infra

    if(selectedContainerDBID == null || "".equals(selectedContainerDBID)) {
      Messages.showErrorDialog("The Autonomous Database's Container Database ID cannot be null.",
          "Invalid Container Database");
      return false;
    }
    return true;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    mainPanel.setPreferredSize(new Dimension(475,500));
    return mainPanel;
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }
}
