package com.oracle.oci.intellij.ui.account;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.Compartment.LifecycleState;
import com.oracle.oci.intellij.account.AuthProvider;
import com.oracle.oci.intellij.account.GlobalEventHandler;
import com.oracle.oci.intellij.account.IdentClient;
import com.oracle.oci.intellij.account.PreferencesWrapper;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class CompartmentSelectionAction extends AnAction {

  CompartmentSelection compartmentSelection;

  public CompartmentSelectionAction() {
    super("Compartment", "Select Compartment", new ImageIcon(
        RegionSelectionAction.class.getResource("/icons/compartments.png")));

  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    compartmentSelection = new CompartmentSelection();
    compartmentSelection.showAndGet();
    if (compartmentSelection.isOK()) {
      final Compartment selectedCompartment = compartmentSelection
          .getSelectedCompartment();
      if (!selectedCompartment.getId()
          .equals(AuthProvider.getInstance().getCompartmentId())) {
        IdentClient.getInstance().
            setCurrentCompartmentName(selectedCompartment.getName());
        PreferencesWrapper.setCompartment(selectedCompartment.getId());
      }
    }

  }

}
