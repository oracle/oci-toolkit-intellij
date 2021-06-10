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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CompartmentSelection extends DialogWrapper {
  private JPanel mainPanel;
  private JTree compartmentTree;

  private Compartment selectedCompartment = null;
  private DefaultMutableTreeNode rootTreeNode = null;
  private final List<Thread> liveThreadsList = Collections.synchronizedList(new LinkedList<>());
  private final AtomicInteger liveTasksCount = new AtomicInteger();

  public static CompartmentSelection newInstance() {
    return new CompartmentSelection();
  }

  private CompartmentSelection() {
    super(true);
    setTitle("Compartment");
    init();
    buildCompartmentsAsync();
  }

  private void buildCompartmentsAsync() {
    beforeCompartmentsLoading();

    final Compartment rootCompartment = OracleCloudAccount.getInstance()
            .getIdentityClient().getRootCompartment();
    rootTreeNode = new DefaultMutableTreeNode(rootCompartment);
    initCompartmentTree(rootTreeNode);
    runAsync(new CompartmentsTreeBuilder(rootTreeNode, 1));
  }

  private void beforeCompartmentsLoading() {
    compartmentTree.setEnabled(false);
    getOKAction().setEnabled(false);
    getOKAction().putValue(Action.NAME, "Loading...");
  }

  private void afterCompartmentsLoading() {
    compartmentTree.expandPath(new TreePath(rootTreeNode.getPath()));
    getOKAction().putValue(Action.NAME, "Select");
    getOKAction().setEnabled(true);
    compartmentTree.setEnabled(true);
  }

  private class CompartmentsTreeBuilder implements Runnable {
    private final DefaultMutableTreeNode givenNode;
    private final int treeDepth;

    public CompartmentsTreeBuilder(DefaultMutableTreeNode node, int treeDepth) {
      givenNode = node;
      this.treeDepth = treeDepth;
    }

    @Override
    public void run() {
      final Compartment givenCompartment = (Compartment) givenNode.getUserObject();

      if (givenNode.getChildCount() > 0) {
        final Iterator<TreeNode> treeNodeIterator = givenNode.children().asIterator();
        treeNodeIterator.forEachRemaining((treeNode) -> {
          if (treeDepth > 0) {
            runAsync(new CompartmentsTreeBuilder((DefaultMutableTreeNode) treeNode, treeDepth-1));
          }
        });
      } else {
        final List<Compartment> childCompartments =
                OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(givenCompartment);

        for(Compartment childCompartment: childCompartments) {
          final DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(childCompartment);
          givenNode.add(childTreeNode);

          if (treeDepth > 0) {
            runAsync(new CompartmentsTreeBuilder(childTreeNode, treeDepth-1));
          }
        }
      }
    }
  }

  private void initCompartmentTree(DefaultMutableTreeNode treeNode) {
    final DefaultTreeModel treeModel = new DefaultTreeModel(treeNode);
    compartmentTree.setModel(treeModel);

    compartmentTree.getSelectionModel().addTreeSelectionListener(event -> selectedCompartment =
            compartmentTree.getLastSelectedPathComponent() == null ? null :
                    ((Compartment) ((DefaultMutableTreeNode) compartmentTree.getLastSelectedPathComponent()).getUserObject()));

    compartmentTree.setCellRenderer(new DefaultTreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                    boolean sel, boolean expanded, boolean leaf, int row,
                                                    boolean hasFocus) {
        return new JLabel(((Compartment) ((DefaultMutableTreeNode) value).getUserObject()).getName());
      }
    });

    final TreeExpansionListener treeExpansionListener = new TreeExpansionListener() {
      @Override
      public void treeExpanded(TreeExpansionEvent event) {

        final DefaultMutableTreeNode lastPathTreeNode =
                (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        final DefaultMutableTreeNode lastPathParentTreeNode = (DefaultMutableTreeNode) lastPathTreeNode.getParent();

        // The null parent node indicates that user expanded the 'root' node.
        // Expanding 'root' node should be a no-op because we have fetched all children of root already.
        if (lastPathParentTreeNode != null) {
          beforeCompartmentsLoading();
          runAsync(new CompartmentsTreeBuilder(lastPathTreeNode, 2));
        }
      }

      @Override
      public void treeCollapsed(TreeExpansionEvent event) {
        // Do nothing.
      }
    };
    compartmentTree.addTreeExpansionListener(treeExpansionListener);
  }

  private void runAsync(Runnable givenRunnable) {
    liveTasksCount.incrementAndGet();

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      liveThreadsList.add(Thread.currentThread());
      givenRunnable.run();

      if(liveTasksCount.decrementAndGet() == 0) {
        afterCompartmentsLoading();
      }
      liveThreadsList.remove(Thread.currentThread());
    });
  }

  public Compartment getSelectedCompartment() {
    // Return root if no compartment is selected.
    if (selectedCompartment == null) {
      return OracleCloudAccount.getInstance()
              .getIdentityClient().getRootCompartment();
    }
    return selectedCompartment;
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
    // Interrupt all live threads on cancel action.
    for(Thread thread : liveThreadsList) {
      thread.interrupt();
    }
    super.doCancelAction();
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
