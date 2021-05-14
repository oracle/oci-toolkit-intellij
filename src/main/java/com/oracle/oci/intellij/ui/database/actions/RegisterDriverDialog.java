/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RegisterDriverDialog extends DialogWrapper {
    private JPanel panel;
    private JTextField textField;
    private JButton browseBtn;
    private JTextField jdbcJarFileTextField;
    private JLabel jdbcJarFileLabel;
    private JPanel jdbcJarFilePanel;
    private JPanel registerJdbcDriverPanel;

    protected RegisterDriverDialog() {
        super(true);
        init();
        setTitle("Register Oracle Driver");
        setOKButtonText("Register");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }
}
