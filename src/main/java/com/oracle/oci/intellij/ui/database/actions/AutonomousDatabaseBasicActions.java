/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.Messages;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.ui.common.UIUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AutonomousDatabaseBasicActions extends AbstractAction {
  private static final String CHANGE_TYPE_CONFIRM_MSG = "<html>You can convert Oracle Autonomous Databases to Oracle Transaction Processing Databases.<br>"
      + "This action is not reversible. After conversion, Oracle will bill you for the following<br><br>"
      + "Oracle Autonomous Transaction Processing OCPU Per Hour (B90453)<br>"
      + "Oracle Autonomous Transaction Processing Exadata Storage (B90455)<br><br>"
      + "Are you sure you want to convert this database to Autonomous Transaction Processing?";

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;
  private final ActionType actionType;

  public enum ActionType {
    STOP("Stop", "Are you sure you want to stop the Autonomous Database?",
        "Autonomous Database stopped successfully.", "Failed to stop Autonomous Database."),

    START("Start", "Are you sure you want to start the Autonomous Database?",
        "Autonomous Database started successfully.", "Failed to start Autonomous Database."),

    TERMINATE("Terminate",
            "Are you sure you want to terminate Autonomous Database %s? " +
                    "Terminating the Autonomous Database %s permanently deletes it. " +
                    "You cannot recover a terminated Autonomous Database. " +
                    "\n\nType in the database display name shown in this message to confirm the termination",
        "Autonomous Database terminated successfully.",
            "Failed to terminate Autonomous Database."),

    SERVICE_CONSOLE("Service Console", "", "", ""),

    CHANGE_WORKLOAD_TYPE("Change Workload Type", CHANGE_TYPE_CONFIRM_MSG,
        "Workload type changed to OLTP successfully", "Failed to change the workload type");

    private final String actionName;
    private final String actionConfirmation;
    private final String actionSuccessMessage;

    ActionType(String actionName, String actionConfirmation,
               String actionSuccessMessage, String actionFailMsg) {
      this.actionName = actionName;
      this.actionConfirmation = actionConfirmation;
      this.actionSuccessMessage = actionSuccessMessage;
    }
  }

  public AutonomousDatabaseBasicActions(
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
    } else if(actionType == ActionType.SERVICE_CONSOLE) {
      BrowserUtil.browse(autonomousDatabaseSummary.getConnectionUrls().getSqlDevWebUrl());
    } else if(actionType == ActionType.CHANGE_WORKLOAD_TYPE) {
      changeWorkloadType();
    } else {
      startStop();
    }
  }

  private void changeWorkloadType() {
    int result = Messages.showOkCancelDialog(actionType.actionConfirmation,
        actionType.actionName, "Change", "Cancel", Messages.getQuestionIcon());
    if (result == Messages.OK) {
      OracleCloudAccount.getInstance().getDatabaseClient().changeWorkloadTypeToOLTP(autonomousDatabaseSummary.getId());
      UIUtil.fireNotification(NotificationType.INFORMATION, actionType.actionSuccessMessage, actionType.name());
    }
  }

  private void startStop() {
    int result = Messages.showOkCancelDialog(actionType.actionConfirmation,
        actionType.actionName, "Yes", "No", Messages.getQuestionIcon());

    if (result == Messages.OK) {
      if (actionType == ActionType.START) {
        OracleCloudAccount.getInstance().getDatabaseClient().startInstance(autonomousDatabaseSummary);
      }
      else {
        OracleCloudAccount.getInstance().getDatabaseClient().stopInstance(autonomousDatabaseSummary);
      }
      UIUtil.fireNotification(NotificationType.INFORMATION, actionType.actionSuccessMessage, actionType.name());
    }
  }

  private void terminate() {
    final String autonomousDatabaseName = autonomousDatabaseSummary.getDbName();
    String userInput = Messages.showInputDialog(
        "Are you sure you want to terminate Autonomous Database "
                + autonomousDatabaseName
                + "? Terminating the Autonomous Database "
                + autonomousDatabaseName
                + " permanently deletes it. \nYou cannot recover a terminated Autonomous Database. "
            + "\n\nType in the Database name to confirm the termination.",
        "Terminate Autonomous Database", Messages.getInformationIcon());

    if (userInput != null) {
      if (userInput.equals(autonomousDatabaseSummary.getDbName())) {
        OracleCloudAccount.getInstance().getDatabaseClient().terminate(autonomousDatabaseSummary.getId());
        UIUtil.fireNotification(NotificationType.INFORMATION,userInput + " terminated successfully.", actionType.name());
      }
      else {
        Messages.showErrorDialog(
                "Database name does not match the existing value.",
                "Database name mismatch");
      }
    }
  }
}
