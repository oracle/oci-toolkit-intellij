/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase;
import com.oracle.oci.intellij.util.LogHandler;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;
import com.oracle.oci.intellij.ui.database.ADBInstanceWrapper;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class WizardActionHandler extends AbstractAction {

  public enum Action {
    ADB_INFO, ADMIN_PWD_CHANGE, CLONE_DB, CREATE_ADB, CREATE_CONNECTION,
    DOWNLOAD_CREDENTIALS, RESTORE_ADB, SCALE_ADB, UPDATE_LICENSE
  }
  private final Action action;
  private final AutonomousDatabaseSummary adbSummary;
  private final CreateAutonomousDatabaseBase.DbWorkload workloadType;

  public WizardActionHandler(Action action,
      AutonomousDatabaseSummary adbSummary, String actionName) {
    super(actionName);
    this.action = action;
    this.adbSummary = adbSummary;
    this.workloadType = null;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final DialogWrapper wizard = createWizard(action);
    if(wizard != null) {
      wizard.showAndGet();
    } else {
      LogHandler.error("Unable to create wizard for the type : " + action.toString());
    }
  }

  private DialogWrapper createWizard(Action action) {
    switch (action) {
    case ADB_INFO:
      ADBInstanceWrapper adbInstance = ADBInstanceClient.getInstance()
          .getInstanceDetails(adbSummary.getId());
      return new ADBInfoWizard(adbInstance);
    case CLONE_DB:
      return new CloneWizard(adbSummary);
    case SCALE_ADB:
      return new ScaleDBWizard(adbSummary);
    case CREATE_ADB:
      return new CreateAutonomousDatabaseWizard();
    case RESTORE_ADB:
      return new RestoreWizard(adbSummary);
    case UPDATE_LICENSE:
      return new UpdateLicenseWizard(adbSummary);
    case ADMIN_PWD_CHANGE:
      return new AdminPasswordWizard(adbSummary);
    case CREATE_CONNECTION:
      return new CreateConnectionWizard(adbSummary);
    case DOWNLOAD_CREDENTIALS:
      return new DownloadCredentialsWizard(adbSummary);
    default:
      return null;
    }
  }

}
