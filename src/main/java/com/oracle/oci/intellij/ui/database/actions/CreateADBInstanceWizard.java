package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.ADBConstants;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;
import org.jetbrains.annotations.Nullable;

import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase.DbWorkload;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseDetails;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseDetails.Builder;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

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
  private TextFieldWithBrowseButton compartmentCmb;
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
  private JComboBox adcCompartmentCmb;
  private JComboBox databaseContainerCmb;
  private JLabel deploymentLabel;
  private JLabel adcCompartmentLabel;
  private JLabel dbContainerLabel;
  private ButtonGroup licenseTypeGrp;
  private ButtonGroup deploymentTypeGrp;

  private Map<String, String> compartmentMap = new LinkedHashMap<String, String>();
  private DbWorkload workloadType;
  private CompartmentSelection compartmentSelection;

  protected CreateADBInstanceWizard(DbWorkload workloadType) {
    super(true);
    setTitle("Create ADB Instance");
    setOKButtonText("Create");
    licenseTypeGrp = new ButtonGroup();
    deploymentTypeGrp = new ButtonGroup();
    licenseTypeGrp.add(byolRBtn);
    licenseTypeGrp.add(licenseIncldBtn);
    deploymentTypeGrp.add(dedicatedRBtn);
    deploymentTypeGrp.add(serverlessRBtn);
    init();
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

    serverlessRBtn.setSelected(true);
    byolRBtn.setSelected(true);

    compartmentCmb.setEditable(false);
    compartmentSelection = new CompartmentSelection();
    compartmentCmb.addActionListener((e) -> {
      compartmentSelection.showAndGet();
      compartmentCmb
          .setText(compartmentSelection.getSelectedCompartment().getName());

    });

    alwayFreeChk.addChangeListener((e) -> {
      if (alwayFreeChk.isSelected()) {
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
      byolRBtn.setEnabled(!dedicatedRBtn.isSelected());
      licenseIncldBtn.setEnabled(!dedicatedRBtn.isSelected());
    });

    if (workloadType.equals(DbWorkload.Dw)) {
      deploymentTypePanel.setVisible(false);
      deploymentLabel.setVisible(false);
      adcCompartmentCmb.setVisible(false);
      adcCompartmentLabel.setVisible(false);
      dbContainerLabel.setVisible(false);
      databaseContainerCmb.setVisible(false);
    }
  }

  public void doOKAction() {

    if (!isValidPassword() || !isValidContainerDatabase())
      return;

    final String compartmentId = compartmentSelection.getSelectedCompartment()
        .getCompartmentId();
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
    final boolean isDedicated = dedicatedRBtn.isSelected();
    if (isDedicated) {
      createADBRequest = createADBRequestBuilder.isDedicated(isDedicated)
          .autonomousContainerDatabaseId(null)
          .build(); // TODO: Handle the isDedicated case
    }
    else {
      createADBRequest = createADBRequestBuilder
          .isAutoScalingEnabled(autoScalingChk.isSelected())
          .isFreeTier(alwayFreeChk.isSelected()).build();
    }

    Runnable nonblockingUpdate = () -> {
      try {
        ADBInstanceClient.getInstance().createInstance(createADBRequest);
        ApplicationManager.getApplication().invokeLater(() -> UIUtil
            .fireSuccessNotification("ADB Instance created successfully."));
      }
      catch (Exception e) {
        ApplicationManager.getApplication().invokeLater(
            () -> UIUtil.fireErrorNotification("Create ADB Instance failed."));
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

  public String getStorageInTB() {
    if (alwayFreeChk.isSelected())
      return ADBConstants.ALWAYS_FREE_STORAGE_TB_DUMMY;
    return storageSpnr.getValue().toString();
  }

  private boolean isValidPassword() {
    final String adminPassword = new String(passwordTxt.getPassword());
    final String confirmAdminPassword = new String(
        confirmPasswordTxt.getPassword());

    if (adminPassword == null || adminPassword.trim().equals("")) {
      Messages.showErrorDialog("Admin password required error",
          "Admin password cannot be empty");
      return false;

    }
    else if (!adminPassword.equals(confirmAdminPassword)) {
      Messages.showErrorDialog("Admin password mismatch error",
          "Confirm Admin password must match Admin password");
      return false;
    }

    return true;
  }

  private boolean isValidContainerDatabase() {
        /*
        if (page.isDedicatedInfra()
                && (page.getSelectedContainerDbId() == null || "".equals(page.getSelectedContainerDbId()))) {
            MessageDialog.openError(getShell(), "Container Database ID required error",
                    "The Autonomous Database's Container Database ID cannot be null.");
            return false;
        }

         */

    return true;
  }

  @Nullable @Override protected JComponent createCenterPanel() {
    return mainPanel;
  }
}
