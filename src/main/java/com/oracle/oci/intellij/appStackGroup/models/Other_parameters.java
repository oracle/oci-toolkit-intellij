package com.oracle.oci.intellij.appStackGroup.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Other_parameters extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private String other_environment_variables;

    private String vm_options;

    private String program_arguments;

    public String getOther_environment_variables() {
        return other_environment_variables;
    }

    public void setOther_environment_variables(String other_environment_variables) {
        this.other_environment_variables = other_environment_variables;
    }

    public String getVm_options() {
        return vm_options;
    }

    public void setVm_options(String vm_options) {
        this.vm_options = vm_options;
    }

    public String getProgram_arguments() {
        return program_arguments;
    }

    public void setProgram_arguments(String program_arguments) {
        this.program_arguments = program_arguments;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}