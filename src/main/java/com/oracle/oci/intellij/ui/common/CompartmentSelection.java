/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.common;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.util.LogHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;

public class CompartmentSelection extends DialogWrapper {
  private JPanel mainPanel;
  private JTree compartmentTree;
  private JPanel secondPane;

  private Compartment selectedCompartment = null;
  private DefaultMutableTreeNode rootNode = null;
  private Thread pooledThread = null;

  public static CompartmentSelection newInstance() {
    return new CompartmentSelection();
  }

  private CompartmentSelection() {
    super(true);
    setTitle("Compartment");
    populateCompartmentTree();
    init();
  }

  private void populateCompartmentTree() {
    loadCompartmentsAsync();
  }

  public void loadCompartmentsAsync() {
    compartmentTree.setEnabled(false);
    getOKAction().setEnabled(false);
    getOKAction().putValue(Action.NAME, "Loading...");

    final Runnable loadCompartments = () -> {
      pooledThread = Thread.currentThread();
      buildCompartmentTree();
      updateUI();

      getOKAction().putValue(Action.NAME, "Select");
      getOKAction().setEnabled(true);
      pooledThread = null;
      compartmentTree.setEnabled(true);
    };

    ApplicationManager.getApplication().executeOnPooledThread(loadCompartments);
  }

  private void buildCompartmentTree() {
    final Compartment rootCompartment = OracleCloudAccount.getInstance()
            .getIdentityClient().getRootCompartment();

    rootNode = new DefaultMutableTreeNode(rootCompartment);
    addChildren(rootCompartment, rootNode);
  }

  public Compartment getSelectedCompartment() {
    if (selectedCompartment == null) {
      // Return root if no compartment is chosen.
      return OracleCloudAccount.getInstance()
              .getIdentityClient().getRootCompartment();
    }
    return selectedCompartment;
  }

  private void addChildren(Compartment parent,
                           DefaultMutableTreeNode parentNode) {
    updateUI();
    try {
      final List<Compartment> compartments =
              OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(parent);

      for (Compartment compartment : compartments) {
        final DefaultMutableTreeNode compartmentNode = new DefaultMutableTreeNode(compartment);

        parentNode.add(compartmentNode);
        addChildren(compartment, compartmentNode);
      }
    } catch (Exception ex) {
      LogHandler.info(ex.getMessage());
    }
  }

  private void updateUI() {
    final DefaultTreeModel defaultTreeModel = new DefaultTreeModel(rootNode);
    compartmentTree.setModel(defaultTreeModel);

    compartmentTree.getSelectionModel().addTreeSelectionListener(event -> selectedCompartment =
            ((Compartment) ((DefaultMutableTreeNode) compartmentTree.getLastSelectedPathComponent()).getUserObject())
    );

    compartmentTree.setCellRenderer(new DefaultTreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                    boolean sel, boolean expanded, boolean leaf, int row,
                                                    boolean hasFocus) {
        return new JLabel(((Compartment) ((DefaultMutableTreeNode) value).getUserObject()).getName());
      }
    });

    repaint();
  }

  @Override
  protected void doOKAction() {
    if (selectedCompartment == null) {
      Messages.showErrorDialog("Select a compartment.", "Compartment");
    } else {
      close(DialogWrapper.OK_EXIT_CODE);
    }
  }

  @Override
  public void doCancelAction() {
    if (pooledThread != null) {
      pooledThread.interrupt();
    }
    close(DialogWrapper.CANCEL_EXIT_CODE);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    mainPanel.setPreferredSize(new Dimension(450, 300));
    return mainPanel;
  }

  @NotNull
  @Override
  protected Action @NotNull [] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

}
