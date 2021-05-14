/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.util.LogHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AutonomousDatabaseMoreActions extends AbstractAction {

  public enum Action {
    ADB_INFO, ADMIN_PWD_CHANGE, CLONE_DB, CREATE_ADB, CREATE_CONNECTION,
    DOWNLOAD_CREDENTIALS, RESTORE_ADB, SCALE_ADB, UPDATE_LICENSE
  }
  private final Action action;
  private final AutonomousDatabaseSummary adbSummary;

  public AutonomousDatabaseMoreActions(Action action,
                                       AutonomousDatabaseSummary adbSummary, String actionName) {
    super(actionName);
    this.action = action;
    this.adbSummary = adbSummary;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final DialogWrapper wizard = createWizard(action);
    if(wizard != null) {
      wizard.showAndGet();
    } else {
      LogHandler.error("Unable to create wizard for the type : " + action);
    }
  }

  private DialogWrapper createWizard(Action action) {
    switch (action) {
    case ADB_INFO:
      final AutonomousDatabaseSummary autonomousDatabaseSummary =
              OracleCloudAccount.getInstance().getDatabaseClient()
                      .getAutonomousDatabaseSummary(adbSummary.getId());
      return new AutonomousDatabaseDialog(autonomousDatabaseSummary);
    case CLONE_DB:
      return new CreateAutonomousDatabaseCloneDialog(adbSummary);
    case SCALE_ADB:
      return new ScaleUpDownDialog(adbSummary);
    case CREATE_ADB:
      return new CreateAutonomousDatabaseDialog();
    case RESTORE_ADB:
      return new RestoreDialog(adbSummary);
    case UPDATE_LICENSE:
      return new UpdateLicenseDialog(adbSummary);
    case ADMIN_PWD_CHANGE:
      return new AdminPasswordDialog(adbSummary);
    case CREATE_CONNECTION:
      return new DBConnectionDialog(adbSummary);
    case DOWNLOAD_CREDENTIALS:
      return new DownloadCredentialsDialog(adbSummary);
    default:
      return null;
    }
  }

}
