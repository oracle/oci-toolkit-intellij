/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.common;

import com.intellij.openapi.ui.DialogWrapper;
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
import java.awt.event.ActionEvent;
import java.util.List;

public class CompartmentSelection extends DialogWrapper {
  private JPanel mainPanel;
  private JTree compartmentTree;
  private JPanel secondPane;

  private Compartment selectedCompartment = null;
  private static DefaultMutableTreeNode rootNode = null;

  public static CompartmentSelection newInstance() {
    return new CompartmentSelection();
  }

  private CompartmentSelection() {
    super(true);
    setTitle("Compartment");
    populateCompartmentTree();
    init();
  }

  private void buildCompartmentTree(boolean isRefresh) {
    final Compartment rootCompartment = OracleCloudAccount.getInstance()
            .getIdentityClient().getRootCompartment(isRefresh);

    rootNode = new DefaultMutableTreeNode(rootCompartment);
    addChildren(rootCompartment, rootNode);
  }

  public Compartment getSelectedCompartment() {
    return selectedCompartment;
  }

  private void populateCompartmentTree() {
    try {
      buildCompartmentTree(false);

      final DefaultTreeModel dtm = new DefaultTreeModel(rootNode);
      compartmentTree.setModel(dtm);
      compartmentTree.getSelectionModel()
              .addTreeSelectionListener(e -> selectedCompartment = ((Compartment) ((DefaultMutableTreeNode) compartmentTree
                      .getLastSelectedPathComponent()).getUserObject()));

      compartmentTree.setCellRenderer(new DefaultTreeCellRenderer() {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus){
          return new JLabel( ((Compartment) ((DefaultMutableTreeNode) value).getUserObject()).getName());
        }
      });

    } catch (Exception e) {
      LogHandler.error(e.getMessage(), e);
    }
  }

  private void addChildren(Compartment parent,
                           DefaultMutableTreeNode parentNode) {
    final List<Compartment> compartments =
            OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(parent);

    for (Compartment compartment : compartments) {
      final DefaultMutableTreeNode compartmentNode = new DefaultMutableTreeNode(
              compartment);

      parentNode.add(compartmentNode);
      addChildren(compartment, compartmentNode);
    }
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
    return new Action[]{new RefreshAction(), getOKAction(), getCancelAction()};
  }

  private class RefreshAction extends AbstractAction {
    RefreshAction() {
      putValue(Action.NAME, "Refresh");
      putValue(Action.SHORT_DESCRIPTION, "Refresh compartments list.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      buildCompartmentTree(true);
    }
  }
}
