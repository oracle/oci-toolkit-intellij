package com.oracle.oci.intellij.ui.appstack.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.oracle.oci.intellij.ui.appstack.AppStackDashboard;

public class RefreshAction extends AbstractAction {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private final AppStackDashboard appStackDashBoard;

  public RefreshAction(AppStackDashboard adbDetails, String actionName) {
    super(actionName);
    this.appStackDashBoard = adbDetails;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    appStackDashBoard.populateTableData();
  }
}