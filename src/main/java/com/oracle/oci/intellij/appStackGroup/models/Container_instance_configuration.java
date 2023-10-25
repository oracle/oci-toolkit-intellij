package com.oracle.oci.intellij.appStackGroup.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Container_instance_configuration extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private enum Shape{
        CI_Standard_E3_Flex,
        CI_Standard_E4_Flex,
    }

    private Shape shape;

;

    private int memory_in_gbs;

    private int ocpus;

    public int getMemory_in_gbs() {
        return memory_in_gbs;
    }

    public void setMemory_in_gbs(int memory_in_gbs) {
        this.memory_in_gbs = memory_in_gbs;
    }

    public int getOcpus() {
        return ocpus;
    }

    public void setOcpus(int ocpus) {
        this.ocpus = ocpus;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}