/*
  Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.account;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Action handler for selection event of UI component 'Compartment'.
 */
public class CompartmentAction extends AnAction {

  public CompartmentAction() {
    super("Compartment", "Select compartment", new ImageIcon(
            RegionAction.class.getResource("/icons/compartments.png")));
  }

  /**
   * Event handler.
   *
   * @param event event.
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

    if (compartmentSelection.showAndGet()) {
      final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
      SystemPreferences.setCompartment(selectedCompartment);
    }
  }

}
