package com.oracle.oci.intellij.appStackGroup.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Application_Performance_Monitoring extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean is_free_tier;

    public boolean isIs_free_tier() {
        return is_free_tier;
    }

    public void setIs_free_tier(boolean is_free_tier) {
        this.is_free_tier = is_free_tier;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}