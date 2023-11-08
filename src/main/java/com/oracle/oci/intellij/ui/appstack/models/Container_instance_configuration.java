package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Container_instance_configuration extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @VariableMetaData(title="Container instance shape",description="A shape is a template that determines the number of OCPUs, amount of memory, and other resources that are allocated to a container instance.",defaultVal="CI.Standard.E3.Flex",type="enum",required=true,enumValues ="[CI.Standard.E3.Flex, CI.Standard.E4.Flex]")
    private enum shape{
        CI_Standard_E3_Flex,
        CI_Standard_E4_Flex,
    }

;

    @VariableMetaData(title="Memory (GB)",description="Min - 1 GB or a value matching the number of OCPUs, whichever is greater. Max - 64 GB per OCPU, up to 1024 GB total",defaultVal="8",type="number",required=true)
    private int memory_in_gbs;

    @VariableMetaData(title="OCPU",description="Min - 1 OCPU. Max - 64 OCPU",defaultVal="2",type="number",required=true)
    private int ocpus;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}