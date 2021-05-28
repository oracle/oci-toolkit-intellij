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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CompartmentSelection extends DialogWrapper {
  private JPanel mainPanel;
  private JTree compartmentTree;
  private JPanel secondPane;
  private JPanel compartmentTreePanel;
  private JScrollPane compartmentTreeScrollPane;

  private Compartment selectedCompartment = null;
  private DefaultMutableTreeNode rootTreeNode = null;
  private DefaultTreeModel treeModel = null;
  private final List<Thread> liveThreadsList = Collections.synchronizedList(new LinkedList<>());
  private final Lock lock = new ReentrantLock();
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
    compartmentTree.setEnabled(false);
    getOKAction().setEnabled(false);
    getOKAction().putValue(Action.NAME, "Loading...");

    final Compartment rootCompartment = OracleCloudAccount.getInstance()
            .getIdentityClient().getRootCompartment();
    rootTreeNode = new DefaultMutableTreeNode(rootCompartment);
    initCompartmentTree(rootTreeNode);

    runAsync(new CompartmentsTreeBuilder(rootCompartment, rootTreeNode, Integer.MAX_VALUE /*any depth*/));
  }

  private class CompartmentsTreeBuilder implements Runnable {
    private final Compartment givenCompartment;
    private final DefaultMutableTreeNode treeNode;
    private final int treeDepth;

    public CompartmentsTreeBuilder(Compartment compartment, DefaultMutableTreeNode parentNode, int treeDepth) {
      givenCompartment = compartment;
      treeNode = parentNode;
      this.treeDepth = treeDepth;
    }

    @Override
    public void run() {
      final List<Compartment> childCompartments =
              OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(givenCompartment);

      for(Compartment childCompartment: childCompartments) {
        final DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(childCompartment);
        treeNode.add(childTreeNode);

        if (treeDepth > 0) {
          runAsync(new CompartmentsTreeBuilder(childCompartment, childTreeNode, treeDepth-1));
        }
      }
    }
  }

  private void initCompartmentTree(DefaultMutableTreeNode treeNode) {
    treeModel = new DefaultTreeModel(treeNode);
    compartmentTree.setModel(treeModel);

    compartmentTree.getSelectionModel().addTreeSelectionListener(event -> selectedCompartment =
            compartmentTree.getLastSelectedPathComponent() == null ? null :
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

    // Not used, intentionally.
    final TreeExpansionListener treeExpansionListener = new TreeExpansionListener() {
      @Override
      public void treeExpanded(TreeExpansionEvent event) {
        final DefaultMutableTreeNode lastPathTreeNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        final Compartment lastPathCompartment = (Compartment) lastPathTreeNode.getUserObject();

        // root
        final DefaultMutableTreeNode lastPathParentTreeNode = (DefaultMutableTreeNode) lastPathTreeNode.getParent();
        if (lastPathParentTreeNode != null) {
          lastPathParentTreeNode.remove(lastPathTreeNode);

          // create a new tree node for last path component.
          final DefaultMutableTreeNode lastPathTreeNewNode = new DefaultMutableTreeNode(lastPathCompartment);
          lastPathParentTreeNode.add(lastPathTreeNewNode);
          runAsync(new CompartmentsTreeBuilder(lastPathCompartment, lastPathTreeNewNode, Integer.MAX_VALUE /*any depth*/));
        }
      }

      @Override
      public void treeCollapsed(TreeExpansionEvent event) {
        // Do nothing.
      }
    };
    /**
     * The present implementation fetches all sub compartments of any depth.
     * If the implementation needs to have sub compartments fetched only on
     * tree expand event, then the following needs to be enabled and tree
     * should be built with the required depth only, initially.
     */
    //compartmentTree.addTreeExpansionListener(treeExpansionListener);
  }

  private void runAsync(Runnable givenRunnable) {
    liveTasksCount.incrementAndGet();

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      liveThreadsList.add(Thread.currentThread());
      givenRunnable.run();

      if(liveTasksCount.decrementAndGet() == 0) {
        getOKAction().putValue(Action.NAME, "Select");
        getOKAction().setEnabled(true);
        compartmentTree.setEnabled(true);
      }
      repaintTree();
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

  private void repaintTree() {
    if(lock.tryLock()) {
      treeModel.setRoot(rootTreeNode);
      compartmentTree.repaint();
      lock.unlock();
    }
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
