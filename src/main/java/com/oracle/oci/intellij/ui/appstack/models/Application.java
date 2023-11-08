package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Application extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @VariableMetaData(title="Application name",description="This name will be used to name other needed resources.",type="string",required=true)
    private java.lang.String application_name;

    @VariableMetaData(title="Number of deployments",description="This is the number of container instances that will be deployed.",type="number",required=true)
    private int nb_copies;

    @VariableMetaData(title="Application source",description="You can deploy an application that is either a container image, a Java artifact (JAR/WAR) or from the source code.",defaultVal="SOURCE_CODE",type="enum",required=true,enumValues ="[IMAGE, ARTIFACT, SOURCE_CODE]")
    private enum application_source{
        IMAGE,
        ARTIFACT,
        SOURCE_CODE,
    }

;

    @VariableMetaData(title="Artifact type",description="The stack can deploy either an executable JAR (using Java runtime) or a WAR (through Tomcat).",defaultVal="JAR",type="enum",required=true,enumValues ="[not selected, JAR, WAR]",visible="not(eq(application_source,'IMAGE'))")
    private enum application_type{
        not_selected,
        JAR,
        WAR,
    }

;

    @VariableMetaData(title="DevOps compartment",description="Compartment containing the DevOps project",defaultVal="${compartment_id}",type="oci:identity:compartment:id",required=true)
    private java.lang.Object devops_compartment;

    @VariableMetaData(title="DevOps repository name (OCID)",description="OCID of the repository containing the application source code.",type="string",required=true,visible="eq(application_source,'SOURCE_CODE')")
    private java.lang.String repo_name;

    @VariableMetaData(title="Branch used for build / deployment",description="Name of the branch to be built, deployed and on which a trigger will be installed for continuous deployment.",defaultVal="main",type="string",required=true,visible="eq(application_source,'SOURCE_CODE')")
    private java.lang.String branch;

    @VariableMetaData(title="Application build command",description="For example: mvn install",defaultVal="mvn install",type="string",required=true,visible="eq(application_source,'SOURCE_CODE')")
    private java.lang.String build_command;

    @VariableMetaData(title="Artifact path",description="For example: target/MyApplication.jar",type="string",required=true,visible="eq(application_source,'SOURCE_CODE')")
    private java.lang.String artifact_location;

    @VariableMetaData(title="Artifact repository OCID",type="string",required=true,visible="eq(application_source,'ARTIFACT')")
    private java.lang.String registry_id;

    @VariableMetaData(title="Artifact OCID",type="string",required=true,visible="eq(application_source,'ARTIFACT')")
    private java.lang.String artifact_id;

    @VariableMetaData(title="Full path to the image in container registry",type="string",required=true,visible="eq(application_source,'IMAGE')")
    private java.lang.String image_path;

    @VariableMetaData(title="Exposed port",description="This is the backend port on which the application is listening.",defaultVal="8443",type="string",required=true,visible="eq(application_source,'IMAGE')")
    private java.lang.String exposed_port;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}