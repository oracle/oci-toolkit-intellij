/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.Messages;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.account.ServicePreferences;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SimpleActionHandler extends AbstractAction {
  private static final String CHANGETYPE_CONFIRM_MSG = "<html>You can convert Oracle Autonomous Databases to Oracle Transaction Processing Databases.<br>"
      + "This action is not reversible. After conversion, Oracle will bill you for the following<br><br>"
      + "Oracle Autonomous Transaction Processing OCPU Per Hour (B90453)<br>"
      + "Oracle Autonomous Transaction Processing Exadata Storage (B90455)<br><br>"
      + "Are you sure you want to convert this database to Autonomous Transaction Processing?";
  private final AutonomousDatabaseSummary autonomousDatabaseSummary;
  private final ActionType actionType;

  public enum ActionType {
    STOP("Stop", "Do you want to stop the ADB Instance?",
        "ADB Instance stopped successfully", "Failed to stop ADB Instance"),
    START("Start", "Do you want to start the ADB Instance?",
        "ADB Instance started successfully", "Failed to start ADB Instance"),
    TERMINATE("Terminate", "Please enter the ADB Instance name to confirm : ",
        "ADB Instance terminated successfully", "Failed to terminate ADB Instance"),
    SERVICE_CONSOLE("Service Console", "", "", ""),
    CHANGE_WORKLOAD_TYPE("Change Workload Type", CHANGETYPE_CONFIRM_MSG,
        "Workload type changed to OLTP successfully", "Failed to change the workload type");
    private final String actionName;
    private final String actionConfirmation;
    private final String actionSuccessMsg;
    private final String actionFailMsg;
    ActionType(String actionName, String actionConfirmation,
        String actionSuccessMsg, String actionFailMsg) {
      this.actionName = actionName;
      this.actionConfirmation = actionConfirmation;
      this.actionSuccessMsg = actionSuccessMsg;
      this.actionFailMsg = actionFailMsg;
    }
  }

  public SimpleActionHandler(
      AutonomousDatabaseSummary autonomousDatabaseSummary,
      ActionType actionType) {
    super(actionType.actionName);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    this.actionType = actionType;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (actionType == ActionType.TERMINATE) {
      terminate();
    }
    else if(actionType == ActionType.SERVICE_CONSOLE) {
      BrowserUtil.browse(autonomousDatabaseSummary.getServiceConsoleUrl());
    }
    else if(actionType == ActionType.CHANGE_WORKLOAD_TYPE) {
      changeWorkloadType();
    }
    else {
      startStopTerminate();
    }
  }

  private void changeWorkloadType() {

    int result = Messages.showOkCancelDialog(actionType.actionConfirmation,
        actionType.actionName, "Change", "Cancel", Messages.getQuestionIcon());
    if (result == Messages.OK) {
      ADBInstanceClient.getInstance()
              .changeWorkloadTypeToOLTP(autonomousDatabaseSummary.getId());
      UIUtil.fireNotification(NotificationType.INFORMATION, actionType.actionSuccessMsg);
      ServicePreferences.fireADBInstanceUpdateEvent(actionType.name());
    }
  }

  private void startStopTerminate() {
    int result = Messages.showOkCancelDialog(actionType.actionConfirmation,
        actionType.actionName, "Yes", "No", Messages.getQuestionIcon());

    if (result == Messages.OK) {
      if (actionType == ActionType.START) {
        ADBInstanceClient.getInstance().startInstance(autonomousDatabaseSummary);
      }
      else {
        ADBInstanceClient.getInstance().stopInstance(autonomousDatabaseSummary);
      }
      UIUtil.fireNotification(NotificationType.INFORMATION, actionType.actionSuccessMsg);
      ServicePreferences.fireADBInstanceUpdateEvent(actionType.name());
    }
  }

  private void terminate() {
    String dbName = Messages.showInputDialog(
        "Do you want terminate the instance " + autonomousDatabaseSummary
            .getDbName()
            + "?. \nPlease confirm termination by entering the ADB Instance name.. ",
        "Confirm ADB Instance Name", Messages.getInformationIcon());

    if (dbName == null)
      return; // Selected cancel

    if (dbName.equals(autonomousDatabaseSummary.getDbName())) {
      ADBInstanceClient.getInstance()
              .terminate(autonomousDatabaseSummary.getId());
      UIUtil.fireNotification(NotificationType.INFORMATION,dbName + " terminated successfully.");
      ServicePreferences.fireADBInstanceUpdateEvent(actionType.name());
    }
    else {
      UIUtil.fireNotification(NotificationType.ERROR,
          "Terminate Failed : Invalid ADB Instance Name");
    }
  }
}
