/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.common;

import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.util.LogHandler;
import com.oracle.oci.intellij.account.AuthenticationDetails;
import com.oracle.oci.intellij.account.Identity;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;

public class CompartmentSelection extends DialogWrapper {
  private JPanel mainPanel;
  private JTree compartmentTree;
  private Compartment selectedCompartment;
  private DefaultMutableTreeNode selectedNode;

  public CompartmentSelection() {
    super(true);
    buildCompartmentTree();
    init();
  }

  public Compartment getSelectedCompartment() {
    return selectedCompartment;
  }

  private void buildCompartmentTree() {
    try {
      final Compartment rootCompartment = Identity.getInstance()
          .getRootCompartment();
      final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
          rootCompartment);
      selectedNode = rootNode;
      addChildren(rootCompartment, rootNode);
      final DefaultTreeModel dtm = new DefaultTreeModel(rootNode);
      compartmentTree.setModel(dtm);
      compartmentTree.getSelectionModel()
          .addTreeSelectionListener(new TreeSelectionListener() {
            @Override public void valueChanged(TreeSelectionEvent e) {
              selectedCompartment = ((Compartment) ((DefaultMutableTreeNode) compartmentTree
                  .getLastSelectedPathComponent()).getUserObject());
            }
          });

      compartmentTree.setCellRenderer(new DefaultTreeCellRenderer() {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
          return new JLabel(
              ((Compartment) ((DefaultMutableTreeNode) value).getUserObject())
                  .getName());
        }
      });

      compartmentTree.setSelectionPath(new TreePath(selectedNode.getPath()));
    }
    catch(Exception e) {
      LogHandler.error(e.getMessage(), e);
    }

  }

  private void addChildren(Compartment parent,
      DefaultMutableTreeNode parentNode) {
    final List<Compartment> compartments = Identity.getInstance()
        .getCompartmentList(parent);
    for (Compartment compartment : compartments) {
      final DefaultMutableTreeNode compartmentNode = new DefaultMutableTreeNode(
          compartment);

      if (compartment.getId()
          .equals(AuthenticationDetails.getInstance().getCompartmentId()))
        selectedNode = compartmentNode;

      parentNode.add(compartmentNode);
      addChildren(compartment, compartmentNode);
    }
  }

  @Nullable @Override protected JComponent createCenterPanel() {
    mainPanel.setPreferredSize(new Dimension(450, 300));
    return mainPanel;
  }
}
