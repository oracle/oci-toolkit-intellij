package com.oracle.oci.intellij.ui.appstack.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class VariableGroup {
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
}
