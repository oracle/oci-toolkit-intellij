/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.oci.intellij.ui.database.ADBConstants;
import com.oracle.oci.intellij.ui.database.ADBInstanceWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ADBInfoWizard extends DialogWrapper {
    private JPanel panel1;
    private JTable table1;

    protected ADBInfoWizard(ADBInstanceWrapper info) {
        super(true);
        setTitle("Autonomous Database Information");
        setOKButtonText("OK");
        init();
        final DefaultTableModel model = new DefaultTableModel(new String[]{"Key", "Value"},0);
        table1.setModel(model);
        model.addRow(new String[]{"Display Name", info.getDisplayName()});
        model.addRow(new String[]{"Database Name", info.getDatabaseName()});
        model.addRow(new String[]{"Lifecycle State", info.getLifeCycleState()});
        model.addRow(new String[]{"Dedicated Infrastructure", info.getDedicatedInfra()});
        model.addRow(new String[]{"CPU Core Count", info.getCPUCoreCount() +""});
        if(info.isFreeTierInstance())
            model.addRow(new String[]{"Storage (TB)", ADBConstants.ALWAYS_FREE_STORAGE_TB +""});
        else
            model.addRow(new String[]{"Storage (TB)", info.getDataStorageSizeInTBs() +""});

        if("No".equals(info.getDedicatedInfra())) {
            model.addRow(new String[]{"Auto Scaling", info.getAutoScaling()});
            model.addRow(new String[]{"License Type", info.getLicenseType()});
        }
        else {
            model.addRow(new String[]{"Container Database Id", info.getAutonomousContainerDatabaseId()});
        }
        model.addRow(new String[]{"Workload Type", info.getWorkloadType()});
        model.addRow(new String[]{"Created", info.getTimeCreated()});
        model.addRow(new String[]{"Compartment", info.getCompartment()});
        model.addRow(new String[]{"OCID", info.getOCID()});
        model.addRow(new String[]{"Database Version", info.getDatabaseVersion()});
        model.addRow(new String[]{"Tags", info.getFreeformTags().toString()});
        model.addRow(new String[]{"Instance Type", info.getInstanceType()});

    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[] {getOKAction()};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        panel1.setPreferredSize(new Dimension(650,420));
        return panel1;
    }
}
