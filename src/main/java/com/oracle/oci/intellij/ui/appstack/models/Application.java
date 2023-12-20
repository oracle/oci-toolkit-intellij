package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;

public class Application extends VariableGroup {

    private java.lang.String application_name;

    private int nb_copies;

    public enum Application_source{
        IMAGE,
        ARTIFACT,
        SOURCE_CODE,
    }
    private Application_source application_source;

;

    public enum Application_type{
        not_selected,
        JAR,
        WAR,
    }

    private Application_type application_type;

;

    private java.lang.Object devops_compartment;

    private java.lang.String repo_name;

    private java.lang.String branch;

    private java.lang.String build_command;

    private java.lang.String artifact_location;

    private java.lang.String registry_id;

    private java.lang.String artifact_id;

    private java.lang.String image_path;

    private java.lang.String exposed_port;
    @PropertyOrder(1)
    @VariableMetaData(title="Application name",description="This name will be used to name other needed resources.",type="string",required=true)
    public String getApplication_name() {
        return application_name;
    }

    public void setApplication_name(String newValue) throws PropertyVetoException {
        String oldValue = this.application_name;
        this.application_name = newValue;
        pcs.firePropertyChange("application_name", oldValue, newValue);
        vcp.fireVetoableChange("application_name", oldValue, newValue);
    }
    @PropertyOrder(2)
    @VariableMetaData(title="Number of deployments",description="This is the number of container instances that will be deployed.",type="number",required=true)

    public int getNb_copies() {
        return nb_copies;
    }

    public void setNb_copies(int newValue) throws PropertyVetoException {
        Object oldValue = this.nb_copies;
        this.nb_copies = newValue;
        pcs.firePropertyChange("nb_copies", oldValue, newValue);
        vcp.fireVetoableChange("nb_copies", oldValue, newValue);
    }
    @PropertyOrder(3)
    @VariableMetaData(title="Application source",description="You can deploy an application that is either a container image, a Java artifact (JAR/WAR) or from the source code.",defaultVal="SOURCE_CODE",type="enum",required=true,enumValues ="IMAGE,ARTIFACT,SOURCE_CODE")

    public Application_source getApplication_source() {
        return application_source;
    }

    public void setApplication_source(Application_source newValue) throws PropertyVetoException {
        Object oldValue = this.application_source;
        this.application_source = newValue;
        pcs.firePropertyChange("application_source", oldValue, newValue);
        vcp.fireVetoableChange("application_source", oldValue, newValue);
    }
    @PropertyOrder(4)
    @VariableMetaData(title="Artifact type",description="The stack can deploy either an executable JAR (using Java runtime) or a WAR (through Tomcat).",defaultVal="JAR",type="enum",required=true,enumValues ="not_selected,JAR,WAR",visible="not(eq(application_source,'IMAGE'))")

    public Application_type getApplication_type() {
        return application_type;
    }

    public void setApplication_type(Application_type newValue) throws PropertyVetoException {
        Object oldValue = this.application_type;
        this.application_type = newValue;
        pcs.firePropertyChange("application_type", oldValue, newValue);
        vcp.fireVetoableChange("application_type", oldValue, newValue);
    }
    @PropertyOrder(5)
    @VariableMetaData(title="DevOps compartment",description="Compartment containing the DevOps project",defaultVal="${compartment_id}",type="oci:identity:compartment:id",required=true)

    public Object getDevops_compartment() {
        return devops_compartment;
    }

    public void setDevops_compartment(Object newValue) throws PropertyVetoException {
        Object oldValue = this.devops_compartment;
        this.devops_compartment = newValue;
        pcs.firePropertyChange("devops_compartment", oldValue, newValue);
        vcp.fireVetoableChange("devops_compartment", oldValue, newValue);
    }
    @PropertyOrder(6)
    @VariableMetaData(title="DevOps repository name (OCID)",description="OCID of the repository containing the application source code.",type="string",required=true,visible="eq(application_source,'SOURCE_CODE')")

    public String getRepo_name() {
        return repo_name;
    }

    public void setRepo_name(String newValue) throws PropertyVetoException {
        Object oldValue = this.repo_name;

        this.repo_name = newValue;
        pcs.firePropertyChange("repo_name", oldValue, newValue);
        vcp.fireVetoableChange("repo_name", oldValue, newValue);
    }
    @PropertyOrder(7)
    @VariableMetaData(title="Branch used for build / deployment",description="Name of the branch to be built, deployed and on which a trigger will be installed for continuous deployment.",defaultVal="main",type="string",required=true,visible="eq(application_source,'SOURCE_CODE')")

    public String getBranch() {
        return branch;
    }

    public void setBranch(String newValue) throws PropertyVetoException {
        Object oldValue = this.branch;
        this.branch = newValue;
        pcs.firePropertyChange("branch", oldValue, newValue);
        vcp.fireVetoableChange("branch", oldValue, newValue);
    }
    @PropertyOrder(8)
    @VariableMetaData(title="Application build command",description="For example: mvn install",defaultVal="mvn install",type="string",required=true,visible="eq(application_source,'SOURCE_CODE')")

    public String getBuild_command() {
        return build_command;
    }

    public void setBuild_command(String newValue) throws PropertyVetoException {
        Object oldValue = this.build_command;
        this.build_command = newValue;
        pcs.firePropertyChange("build_command", oldValue, newValue);
        vcp.fireVetoableChange("build_command", oldValue, newValue);
    }
    @PropertyOrder(9)
    @VariableMetaData(title="Artifact path",description="For example: target/MyApplication.jar",type="string",required=true,visible="eq(application_source,'SOURCE_CODE')")

    public String getArtifact_location() {
        return artifact_location;
    }

    public void setArtifact_location(String newValue) throws PropertyVetoException {
        Object oldValue = this.artifact_location;
        this.artifact_location = newValue;
        pcs.firePropertyChange("artifact_location", oldValue, newValue);
        vcp.fireVetoableChange("artifact_location", oldValue, newValue);
    }
    @PropertyOrder(10)
    @VariableMetaData(title="Artifact repository OCID",type="string",required=true,visible="eq(application_source,'ARTIFACT')")

    public String getRegistry_id() {
        return registry_id;
    }

    public void setRegistry_id(String newValue) throws PropertyVetoException {
        Object oldValue = this.registry_id;
        this.registry_id = newValue;
        pcs.firePropertyChange("registry_id", oldValue, newValue);
        vcp.fireVetoableChange("registry_id", oldValue, newValue);
    }
    @PropertyOrder(11)
    @VariableMetaData(title="Artifact OCID",type="string",required=true,visible="eq(application_source,'ARTIFACT')")

    public String getArtifact_id() {
        return artifact_id;
    }

    public void setArtifact_id(String newValue) throws PropertyVetoException {
        Object oldValue = this.artifact_id;
        this.artifact_id = newValue;
        pcs.firePropertyChange("artifact_id", oldValue, newValue);
        vcp.fireVetoableChange("artifact_id", oldValue, newValue);
    }
    @PropertyOrder(12)
    @VariableMetaData(title="Full path to the image in container registry",type="string",required=true,visible="eq(application_source,'IMAGE')")

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String newValue) throws PropertyVetoException {
        Object oldValue = this.image_path;
        this.image_path = newValue;
        pcs.firePropertyChange("image_path", oldValue, newValue);
        vcp.fireVetoableChange("image_path", oldValue, newValue);
    }
    @PropertyOrder(13)
    @VariableMetaData(title="Exposed port",description="This is the backend port on which the application is listening.",defaultVal="8443",type="string",required=true,visible="eq(application_source,'IMAGE')")

    public String getExposed_port() {
        return exposed_port;
    }

    public void setExposed_port(String newValue) throws PropertyVetoException {
        Object oldValue = this.exposed_port;
        this.exposed_port = newValue;
        pcs.firePropertyChange("exposed_port", oldValue, newValue);
        vcp.fireVetoableChange("exposed_port", oldValue, newValue);
    }

}