/*
  Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.intellij.ui.common.AutonomousDatabaseConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AutonomousDatabaseDialog extends DialogWrapper {
    private JPanel mainPanel;
    private JTable table;

    protected AutonomousDatabaseDialog(AutonomousDatabaseSummary autonomousDatabaseSummary) {
        super(true);
        setTitle("Autonomous Database Information");
        setOKButtonText("OK");
        init();

        final DefaultTableModel model = new DefaultTableModel(new String[]{"Key", "Value"},0);
        table.setModel(model);
        model.addRow(new String[]{"Display Name", autonomousDatabaseSummary.getDisplayName()});
        model.addRow(new String[]{"Database Name", autonomousDatabaseSummary.getDbName()});
        model.addRow(new String[]{"Lifecycle State", autonomousDatabaseSummary.getLifecycleState().getValue()});
        model.addRow(new String[]{"Dedicated Infrastructure", autonomousDatabaseSummary.getIsDedicated() ? "Yes" : "No"});
        model.addRow(new String[]{"CPU Core Count", autonomousDatabaseSummary.getCpuCoreCount().toString()});

        if(autonomousDatabaseSummary.getIsFreeTier()) {
            model.addRow(new String[]{"Storage (TB)", AutonomousDatabaseConstants.ALWAYS_FREE_STORAGE_TB + ""});
        }
        else {
            model.addRow(new String[]{"Storage (TB)", autonomousDatabaseSummary.getDataStorageSizeInTBs().toString()});
        }

        if (!autonomousDatabaseSummary.getIsDedicated()) {
            model.addRow(new String[]{"Auto Scaling", autonomousDatabaseSummary.getIsAutoScalingEnabled() ? "Enabled" : "Disabled"});
            model.addRow(new String[]{"License Type", autonomousDatabaseSummary.getLicenseModel().getValue()});
        }
        else {
            model.addRow(new String[]{"Container Database Id", autonomousDatabaseSummary.getAutonomousContainerDatabaseId()});
        }
        model.addRow(new String[]{"Workload Type", autonomousDatabaseSummary.getDbWorkload().getValue()});
        model.addRow(new String[]{"Created", autonomousDatabaseSummary.getTimeCreated().toString()});
        model.addRow(new String[]{"Compartment", autonomousDatabaseSummary.getCompartmentId()});
        model.addRow(new String[]{"OCID", autonomousDatabaseSummary.getId()});
        model.addRow(new String[]{"Database Version", autonomousDatabaseSummary.getDbVersion()});
        model.addRow(new String[]{"Tags", autonomousDatabaseSummary.getFreeformTags().toString()});
        model.addRow(new String[]{"Instance Type", autonomousDatabaseSummary.getIsFreeTier() ? "Free" : "Paid"});
    }

    @NotNull
    @Override
    protected Action @NotNull [] createActions() {
        return new Action[] {getOKAction()};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        mainPanel.setPreferredSize(new Dimension(650,420));
        return new JBScrollPane(mainPanel);
    }
}
