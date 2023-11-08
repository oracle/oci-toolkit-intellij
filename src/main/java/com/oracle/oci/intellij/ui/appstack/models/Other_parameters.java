package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Other_parameters extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @VariableMetaData(title="Other environment variables",description="If your application can be configured through environment variables you can configure them here. Separate variables with semicolon (var1=value1;var2=value2).",type="string")
    private java.lang.String other_environment_variables;

    @VariableMetaData(title="JVM options",description="For example : -Xms=2G -Dspring.sql.init.data-locations=/temp/script.sql",type="string",visible="not(eq(application_source,'IMAGE'))")
    private java.lang.String vm_options;

    @VariableMetaData(title="Program arguments",description="These space-separated program arguments are passed to the java process at startup.",type="string",visible="and(eq(application_type,'JAR'),not(eq(application_source,'IMAGE')))")
    private java.lang.String program_arguments;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}