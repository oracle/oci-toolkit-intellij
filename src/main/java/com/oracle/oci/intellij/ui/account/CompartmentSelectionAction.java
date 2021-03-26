/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.account;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.AuthenticationDetails;
import com.oracle.oci.intellij.account.Identity;
import com.oracle.oci.intellij.account.ServicePreferences;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Action handler for selection event of UI component 'Compartment'.
 */
public class CompartmentSelectionAction extends AnAction {

  public CompartmentSelectionAction(){
    super("Compartment", "Select compartment", new ImageIcon(
            RegionSelectionAction.class.getResource("/icons/compartments.png")));
  }

  /**
   * Event handler.
   *
   * @param event event.
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent event){
    final CompartmentSelection compartmentSelection = new CompartmentSelection();
    compartmentSelection.showAndGet();

    if (compartmentSelection.isOK()) {
      final Compartment selectedCompartment = compartmentSelection
              .getSelectedCompartment();

      if (!selectedCompartment.getId()
              .equals(AuthenticationDetails.getInstance().getCompartmentId())) {
        Identity.getInstance().
                setCurrentCompartmentName(selectedCompartment.getName());

        // TODO: Study why compartment name/id is maintained in both
        //  AuthenticationDetails and Identity class.
        ServicePreferences.updateCompartment(selectedCompartment.getId());
      }
    }
  }

}
