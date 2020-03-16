package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.ui.Messages;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;
import com.oracle.oci.intellij.ui.database.DatabaseDetails;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SimpleActionHandler extends AbstractAction {

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;
  private final ActionType actionType;

  public enum ActionType {
    STOP("Stop", "Do you want to stop the ADB Instance?"), START("Start",
        "Do you want to start the ADB Instance?"), TERMINATE("Terminate",
        "Please enter the ADB Instance name to confirm : ");
    private final String actionName;
    private final String actionDescription;

    ActionType(String actionName, String actionDescription) {
      this.actionName = actionName;
      this.actionDescription = actionDescription;
    }

  }

  public SimpleActionHandler(
      AutonomousDatabaseSummary autonomousDatabaseSummary,
      ActionType actionType) {
    super(actionType.actionName);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    this.actionType = actionType;
  }

  @Override public void actionPerformed(ActionEvent e) {
    if (actionType == ActionType.TERMINATE) {
      String dbName = Messages.showInputDialog(
          "Do you want terminate the instance " + autonomousDatabaseSummary
              .getDbName()
              + "?. \nPlease confirm termination by entering the ADB Instance name.. ",
          "Confirm ADB Instance Name", Messages.getInformationIcon());
      if (dbName != null && dbName
          .equals(autonomousDatabaseSummary.getDbName())) {
        ADBInstanceClient.getInstance()
            .terminate(autonomousDatabaseSummary.getId());
        Messages.showInfoMessage("Successfully Terminated.", "Success");
      }
      else {
        Messages.showErrorDialog("Invalid ADB Instance Name", "Error");
      }
    }
    else {
      int result = Messages.showOkCancelDialog(actionType.actionDescription,
          actionType.actionName, "Yes", "No", Messages.getQuestionIcon());
      if (result == Messages.OK) {
        if (actionType == ActionType.START)
          ADBInstanceClient.getInstance()
              .startInstance(autonomousDatabaseSummary);
        else
          ADBInstanceClient.getInstance()
              .stopInstance(autonomousDatabaseSummary);
        Messages.showInfoMessage("Updated Successfully.", "Success");
      }
    }
  }
}
