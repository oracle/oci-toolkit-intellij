/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.common;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
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

/**
 * The compartment selection dialog implementation.
 */
public class CompartmentSelection extends DialogWrapper {
  private JPanel mainPanel;
  private JTree compartmentTree;

  // The user selected compartment.
  private Compartment selectedCompartment = null;
  private DefaultMutableTreeNode rootTreeNode = null;
  private final List<Thread> liveThreadsList = Collections.synchronizedList(new LinkedList<>());
  private final AtomicInteger liveTasksCount = new AtomicInteger();

  /**
   * Factory method for new instance.
   * @return a new instance
   */
  public static CompartmentSelection newInstance() {
    return new CompartmentSelection();
  }

  /**
   * The constructor. Builds the first two levels under
   * the root compartment.
   */
  private CompartmentSelection() {
    super(true);
    setTitle("Compartment");
    init();
    buildCompartmentsAsync();
  }

  /**
   * Initialises and asynchronously builds the
   * first two levels under the root.
   */
  private void buildCompartmentsAsync() {
    beforeCompartmentsLoading();

    final Compartment rootCompartment = OracleCloudAccount.getInstance()
            .getIdentityClient().getRootCompartment();
    rootTreeNode = new DefaultMutableTreeNode(rootCompartment);
    initCompartmentTree(rootTreeNode);
    runAsync(new CompartmentsTreeBuilder(rootTreeNode, 1));
  }

  /**
   * Disables the UI components before loading the compartments.
   */
  private void beforeCompartmentsLoading() {
    compartmentTree.setEnabled(false);
    getOKAction().setEnabled(false);
    getOKAction().putValue(Action.NAME, "Loading...");
  }

  /**
   * Enables the UI components after loading the compartments.
   */
  private void afterCompartmentsLoading() {
    compartmentTree.expandPath(new TreePath(rootTreeNode.getPath()));
    getOKAction().putValue(Action.NAME, "Select");
    getOKAction().setEnabled(true);
    compartmentTree.setEnabled(true);
  }

  /**
   * The runnable that builds the sub compartments at given depth
   * under the given compartment node.
   */
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

      /**
       * If the sub compartments are already in place for the given compartment,
       * just iterate up to the requested level. Fetch only the missing
       * compartments, if any.
       */
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

  /**
   * Initializes the UI components before building the compartments tree.
   * @param treeNode
   */
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

  /**
   * Uses the application manager's thread to complete task asynchronously.
   * @param givenRunnable
   */
  private void runAsync(Runnable givenRunnable) {
    liveTasksCount.incrementAndGet();

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      liveThreadsList.add(Thread.currentThread());
      try {
        givenRunnable.run();
      } finally {
        if(liveTasksCount.decrementAndGet() == 0) {
          afterCompartmentsLoading();
        }
        liveThreadsList.remove(Thread.currentThread());
      }
    });
  }

  /**
   * Returns the user selected compartment.
   * @return compartment
   */
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

  /**
   * Interrupt the running asynchronous tasks, if any,
   * before closing the dialog.
   */
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
    return new JBScrollPane(mainPanel);
  }

  @NotNull
  @Override
  protected Action @NotNull [] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

}
