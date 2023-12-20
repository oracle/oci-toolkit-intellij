package com.oracle.oci.intellij.ui.appstack.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

public class VariableGroup {
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    protected  final VetoableChangeSupport vcp = new VetoableChangeSupport(this);


    public void addVetoableChangeListener(VetoableChangeListener listener) {
        this.vcp.addVetoableChangeListener(listener);
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        this.vcp.removeVetoableChangeListener(listener);
    }
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
}
