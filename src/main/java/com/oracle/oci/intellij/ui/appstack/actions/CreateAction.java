package com.oracle.oci.intellij.ui.appstack.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import com.oracle.oci.intellij.account.OracleCloudAccount;

public class CreateAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CreateAction(String actionName) {
      super(actionName);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//      final DialogWrapper wizard = new CreateAutonomousDatabaseDialog();
//      wizard.showAndGet();
      try {
        OracleCloudAccount.getInstance().getResourceManagerClientProxy().createStack();
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
  }