/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.database.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.DatabaseClientProxy;
import com.oracle.oci.intellij.util.LogHandler;

public class AutonomousDatabaseMoreActions extends AbstractAction {

  /**
     * 
     */
    private static final long serialVersionUID = 1L;

public enum Action {
    ADB_INFO, ADMIN_PWD_CHANGE, CLONE_DB, CREATE_ADB, DOWNLOAD_CREDENTIALS,
    RESTORE_ADB, SCALE_ADB, UPDATE_LICENSE, UPDATE_NETWORK_ACCESS
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
    updateAdbSummary();
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
    case DOWNLOAD_CREDENTIALS:
      return new DownloadCredentialsDialog(adbSummary);
    case UPDATE_NETWORK_ACCESS:
      return new UpdateNetworkAccessDialog(adbSummary);
    default:
      return null;
    }
  }

  private void updateAdbSummary() {
      DatabaseClientProxy databaseClient = OracleCloudAccount.getInstance().getDatabaseClient();
      // for some reason, unknownenumvalue == "ALL"
      databaseClient.getAutonomousDatabaseInstances(AutonomousDatabaseSummary.DbWorkload.UnknownEnumValue);
  }

}
