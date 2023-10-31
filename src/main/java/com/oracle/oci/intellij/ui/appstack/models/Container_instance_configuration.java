package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;

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
    @PropertyOrder(1)
    public Shape getShape() {
        return shape;
    }
    @PropertyOrder(2)
    public int getMemory_in_gbs() {
        return memory_in_gbs;
    }
    @PropertyOrder(3)
    public int getOcpus() {
        return ocpus;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public void setMemory_in_gbs(int memory_in_gbs) {
        this.memory_in_gbs = memory_in_gbs;
    }

    public void setOcpus(int ocpus) {
        this.ocpus = ocpus;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}