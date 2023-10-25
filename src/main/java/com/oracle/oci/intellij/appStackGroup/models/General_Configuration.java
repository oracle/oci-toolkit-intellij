package com.oracle.oci.intellij.appStackGroup.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class General_Configuration extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private Object compartment_id;

    private Object availability_domain;


    public Object getCompartment_id() {
        return compartment_id;
    }

    public void setCompartment_id(Object compartment_id) {
        this.compartment_id = compartment_id;
    }

    public Object getAvailability_domain() {
        return availability_domain;
    }

    public void setAvailability_domain(Object availability_domain) {
        this.availability_domain = availability_domain;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}