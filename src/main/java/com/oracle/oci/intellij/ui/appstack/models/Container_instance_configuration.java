package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Container_instance_configuration extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public enum Shape{
        CI_Standard_E3_Flex,
        CI_Standard_E4_Flex,
    }
    private Shape shape;

;

    private int memory_in_gbs;

    private int ocpus;

    @PropertyOrder(1)
    @VariableMetaData(title="Container instance shape",description="A shape is a template that determines the number of OCPUs, amount of memory, and other resources that are allocated to a container instance.",defaultVal="CI.Standard.E3.Flex",type="enum",required=true,enumValues ="[CI.Standard.E3.Flex, CI.Standard.E4.Flex]")
    public Shape getShape() {
        return shape;
    }
    @PropertyOrder(2)
    @VariableMetaData(title="Memory (GB)",description="Min - 1 GB or a value matching the number of OCPUs, whichever is greater. Max - 64 GB per OCPU, up to 1024 GB total",defaultVal="8",type="number",required=true)

    public int getMemory_in_gbs() {
        return memory_in_gbs;
    }
    @PropertyOrder(3)
    @VariableMetaData(title="OCPU",description="Min - 1 OCPU. Max - 64 OCPU",defaultVal="2",type="number",required=true)

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