package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class General_Configuration extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @VariableMetaData(title="Compute Compartment",description="The compartment in which to create all Compute resources.",defaultVal="compartment_ocid",type="oci:identity:compartment:id",required=true)
    private java.lang.Object compartment_id;

    @VariableMetaData(title="Availability domain",description="The availability domain in which to create all Compute resources.",type="oci:identity:availabilitydomain:name",dependsOn="{compartmentId=${compartment_id}}",required=true)
    private java.lang.Object availability_domain;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}