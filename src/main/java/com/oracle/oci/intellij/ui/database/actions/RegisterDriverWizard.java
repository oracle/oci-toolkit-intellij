/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RegisterDriverWizard extends DialogWrapper {
    private JPanel panel1;
    private JTextField textField1;
    private JButton browseBtn;

    protected RegisterDriverWizard() {
        super(true);
        init();
        setTitle("Register Oracle Driver");
        setOKButtonText("Register");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel1;
    }
}
