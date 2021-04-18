/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.database.dataSource.DatabaseDriver;
import com.intellij.database.dataSource.DatabaseDriverManager;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.editor.DatabaseEditorHelper;
import com.intellij.database.psi.DataSourceManager;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.psi.DefaultDbPsiManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.containers.ContainerUtil;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.util.LogHandler;
import com.oracle.oci.intellij.account.AuthenticationDetails;
import com.oracle.oci.intellij.ui.common.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class CreateConnectionWizard extends DialogWrapper {

  private final String TNS_FILENAME = "tnsnames.ora";

  private JPanel mainPanel;
  private JPanel createNewConnectionPanel;

  private JLabel walletPathLabel;
  private TextFieldWithBrowseButton walletPathTextField;

  private JLabel tnsNameAliasLabel;
  private JComboBox tnsNameAliasComboBox;

  private JLabel userNameLabel;
  private JTextField userNameTextField;

  private JLabel passwordLabel;
  private JPasswordField passwordField;

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;

  private final JFileChooser fileChooser = new JFileChooser();

  protected CreateConnectionWizard(AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    init();
    setTitle("Create Connection");
    setOKButtonText("Create");

    mainPanel.setPreferredSize(new Dimension(400, 175));
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;

    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    walletPathTextField.addActionListener((e) -> {
      int dialogReturnState = fileChooser.showDialog(mainPanel, "Select Wallet Path");

      if (dialogReturnState == JFileChooser.APPROVE_OPTION) {
        final String walletPath = fileChooser.getSelectedFile().getAbsolutePath();
        walletPathTextField.setText(walletPath);
        onWalletPathChange(walletPath);
      }
    });
  }

  private void onWalletPathChange(String walletPath) {
    final Set<String> profileNames = getTnsEntries(walletPath);

    tnsNameAliasComboBox.removeAllItems();
    profileNames.forEach(e -> tnsNameAliasComboBox.addItem(e));
    if (profileNames.size() > 0) {
      tnsNameAliasComboBox.setSelectedIndex(1);
    }
  }

  /**
   *
   * @param walletPath
   * @return
   */
  private Set<String> getTnsEntries(String walletPath) {
    final Set<String> tnsEntries = new LinkedHashSet<>();
    final String dbName = autonomousDatabaseSummary.getDbName();

    try {
      final String tnsnamesFilePath = walletPath + File.separator + TNS_FILENAME;
      final File file = new File(tnsnamesFilePath);

      try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
        String line;
        while((line = bufferedReader.readLine()) != null) {
          if (line != null && line.trim().startsWith(dbName.toLowerCase() + "_")) {
            int index = line.indexOf("=");
            if (index != -1) {
              String alias = line.substring(0, index).trim();
              tnsEntries.add(alias);
            }
          }
        }
      }
    } catch (IOException ioEx) {
      UIUtil.fireNotification(NotificationType.ERROR, "Error reading tnsnames.ora : " + ioEx.getMessage());
      LogHandler.error("Error occurred while reading tnsnames.ora file for database: " + dbName, ioEx);
    }

    return tnsEntries;
  }

  public void doOKAction() {
    final String user = userNameTextField.getText();
    final char[] password = passwordField.getPassword();
    final String aliasName = (String) tnsNameAliasComboBox.getSelectedItem();
    final String walletLocation = walletPathTextField.getText()
        .replace('\\', '/');

    if (!validateInput(user, password, walletLocation, aliasName)) {
      return; // Return if validation fails.
    }

    final String regionName = AuthenticationDetails.getInstance().getRegion().toString();
    final String profileName = user.toUpperCase() + "." + aliasName + "." + regionName;
    final String url = "jdbc:oracle:thin:@" + aliasName + "?TNS_ADMIN=" + walletLocation;

    // Creates a DataSource using the configurations
    final LocalDataSource ds = LocalDataSource.create(profileName, "oracle", url, user);
    ds.setDriverClass("oracle.jdbc.OracleDriver");
    addDataSourceToDatabaseTool(ds);
    Arrays.fill(password, ' ');
    close(DialogWrapper.OK_EXIT_CODE);
  }

  private void addDataSourceToDatabaseTool(final LocalDataSource ds) {
    final Project currentProject = UIUtil.getCurrentProject();
    final List<DataSourceManager<?>> dbManagers = DbPsiFacade
        .getInstance(currentProject).getDbManagers();
    final DefaultDbPsiManager psiManager = ContainerUtil
        .findInstance(dbManagers, DefaultDbPsiManager.class);

    final DatabaseDriver oracleDriver = getOracleDriver();

    if (oracleDriver == null) {
      Messages.showErrorDialog(
          "OracleDriver not registered. Please register it through Database Tools.",
          "Driver not registered !!");
      return;
    }

    ds.setDatabaseDriver(oracleDriver);
    final LocalDataSource duplicateDS = getDataSource(psiManager, ds.getName());
    if (duplicateDS != null) {
      final int result = Messages.showYesNoDialog("Data Source " + ds.getName()
              + " already exists. Do you want to replace?",
          "Data Source already exists", Messages.getQuestionIcon());
      if (result == Messages.YES) {
        psiManager.removeDataSource(duplicateDS);
      }
      else {
        // Do not add the datasource as user selected "No".
        return;
      }
    }

    psiManager.addDataSource(ds);
    openDatabaseView(currentProject);
    DatabaseEditorHelper.openConsoleFile(currentProject, ds, null, true);
  }

  private LocalDataSource getDataSource(final DefaultDbPsiManager psiManager,
      final String profileName) {
    final List<LocalDataSource> dataSources = psiManager.getDataSources();
    for (LocalDataSource lds : dataSources) {
      if (lds.getName().equals(profileName))
        return lds;
    }
    return null;
  }

  private void openDatabaseView(final Project currentProject) {
    final ToolWindow dataBaseToolWindow = ToolWindowManager
            .getInstance(currentProject).getToolWindow("Database");
    if (dataBaseToolWindow != null && (!dataBaseToolWindow.isVisible()
            || !dataBaseToolWindow.isActive())) {
      dataBaseToolWindow.show(null);
    }
  }

  private DatabaseDriver getOracleDriver() {
    return DatabaseDriverManager.getInstance().getDrivers().stream().filter(
            (driver) -> Objects.equals(driver.getDriverClass(), "oracle.jdbc.OracleDriver"))
            .findFirst().orElse(null);

  }

  private boolean validateInput(final String user, final char[] password,
      final String walletLocation, final String aliasName) {

    if (user == null || user.trim().equals("")) {
      Messages.showErrorDialog("Database user name cannot be empty",
          "Database user required error");
      return false;
    }

    if (password == null || password.length == 0) {
      Messages.showErrorDialog("User's password cannot be empty",
          "Password required error");
      return false;
    }

    if (walletLocation == null || walletLocation.trim().equals("")) {
      Messages.showErrorDialog("Wallet location cannot be empty",
          "Wallet location required error");
      return false;
    }

    if (aliasName == null || aliasName.trim().equals("")) {
      Messages.showErrorDialog(
          "Unable to find matching tnsnames alias for the database, make sure wallet location is correct",
          "Alias name required error");
      return false;
    }

    return true;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return mainPanel;
  }

}
