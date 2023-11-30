package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Application_Performance_Monitoring extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean is_free_tier;
    @VariableMetaData(title="Create as Always Free Domain",description="Up to 1000 spans per hour, 31 day storage for trace data and 10 monitor runs per hour.",defaultVal="false",type="boolean",required=true)
    public boolean isIs_free_tier() {
        return is_free_tier;
    }

    public void setIs_free_tier(boolean newValue) {
        Object oldValue = this.is_free_tier;
        this.is_free_tier = newValue;
        pcs.firePropertyChange("is_free_tier", oldValue, newValue);
    }
    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}