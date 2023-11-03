package com.oracle.oci.intellij.ui.appstack.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        // TODO:
        Map<String, String> variables = new HashMap<>();
        OracleCloudAccount.getInstance().getResourceManagerClientProxy().createStack(variables);
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
  }