package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Other_Parameters extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private java.lang.String other_environment_variables;

    private java.lang.String vm_options;

    private java.lang.String program_arguments;

    @PropertyOrder(1)
    @VariableMetaData(title="Other environment variables",description="If your application can be configured through environment variables you can configure them here. Separate variables with semicolon (var1=value1;var2=value2).",type="string")
    public String getOther_environment_variables() {
        return other_environment_variables;
    }

    public void setOther_environment_variables(String other_environment_variables) {
        this.other_environment_variables = other_environment_variables;
    }
    @PropertyOrder(2)
    @VariableMetaData(title="JVM options",description="For example : -Xms=2G -Dspring.sql.init.data-locations=/temp/script.sql",type="string",visible="not(eq(application_source,'IMAGE'))")

    public String getVm_options() {
        return vm_options;
    }

    public void setVm_options(String vm_options) {
        this.vm_options = vm_options;
    }
    @PropertyOrder(3)
    @VariableMetaData(title="Program arguments",description="These space-separated program arguments are passed to the java process at startup.",type="string",visible="and(eq(application_type,'JAR'),not(eq(application_source,'IMAGE')))")
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