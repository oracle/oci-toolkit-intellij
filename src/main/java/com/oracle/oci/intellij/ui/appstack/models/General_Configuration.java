package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;

public class General_Configuration extends VariableGroup {

    private java.lang.Object compartment_id;

    private java.lang.Object availability_domain;

    @PropertyOrder(1)
    @VariableMetaData(title="Compute Compartment",description="The compartment in which to create all Compute resources.",defaultVal="compartment_ocid",type="oci:identity:compartment:id",required=true)

    public Object getCompartment_id() {
        return compartment_id;
    }

    public void setCompartment_id(Object newValue) throws PropertyVetoException {
        Object oldValue = this.compartment_id;
        this.compartment_id = newValue;
        pcs.firePropertyChange("compartment_id", oldValue, newValue);
        vcp.fireVetoableChange("compartment_id", oldValue, newValue);
    }
    @PropertyOrder(2)
    @VariableMetaData(title="Availability domain",description="The availability domain in which to create all Compute resources.",type="oci:identity:availabilitydomain:name",dependsOn="{compartmentId=${compartment_id}}",required=true)

    public Object getAvailability_domain() {
        return availability_domain;
    }

    public void setAvailability_domain(Object newValue) throws PropertyVetoException {
        Object oldValue = this.availability_domain;
        this.availability_domain = newValue;
        pcs.firePropertyChange("availability_domain", oldValue, newValue);
        vcp.fireVetoableChange("availability_domain", oldValue, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}