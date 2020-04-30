package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.ui.Messages;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.account.PreferencesWrapper;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SimpleActionHandler extends AbstractAction {
  private final AutonomousDatabaseSummary autonomousDatabaseSummary;
  private final ActionType actionType;

  public enum ActionType {
    STOP("Stop", "Do you want to stop the ADB Instance?",
        "ADB Instance stopped successfully", "Failed to stop ADB Instance"),
    START("Start", "Do you want to start the ADB Instance?",
        "ADB Instance started successfully", "Failed to start ADB Instance"),
    TERMINATE("Terminate", "Please enter the ADB Instance name to confirm : ",
        "ADB Instance terminated successfully", "Failed to terminate ADB Instance");
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
      String dbName = Messages.showInputDialog(
          "Do you want terminate the instance " + autonomousDatabaseSummary
              .getDbName()
              + "?. \nPlease confirm termination by entering the ADB Instance name.. ",
          "Confirm ADB Instance Name", Messages.getInformationIcon());

      if(dbName == null)
        return; // Selected cancel

      if (dbName.equals(autonomousDatabaseSummary.getDbName())) {
        try {
          ADBInstanceClient.getInstance().terminate(autonomousDatabaseSummary.getId());
          UIUtil.fireSuccessNotification(dbName + " terminated successfully.");
          PreferencesWrapper.fireADBInstanceUpdateEvent(actionType.name());
        }
        catch(Exception ex) {
          UIUtil.fireErrorNotification(actionType.actionFailMsg + " : " + ex.getMessage());
        }
      }
      else {
        UIUtil.fireErrorNotification("Terminate Failed : Invalid ADB Instance Name");
      }
    }
    else {
      int result = Messages.showOkCancelDialog(actionType.actionConfirmation,
          actionType.actionName, "Yes", "No", Messages.getQuestionIcon());
      try{
        if (result == Messages.OK) {
          if (actionType == ActionType.START) {
            ADBInstanceClient.getInstance().startInstance(autonomousDatabaseSummary);
          }
          else {
            ADBInstanceClient.getInstance().stopInstance(autonomousDatabaseSummary);
          }
          UIUtil.fireSuccessNotification(actionType.actionSuccessMsg);
          PreferencesWrapper.fireADBInstanceUpdateEvent(actionType.name());
        }
      }
      catch(Exception ex) {
        UIUtil.fireErrorNotification(actionType.actionFailMsg + " : " + ex.getMessage());
      }
    }
  }
}
