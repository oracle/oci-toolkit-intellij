package com.oracle.oci.intellij.ui.appstack.models;

import com.intellij.openapi.externalSystem.util.Order;
import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Application extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private String application_name;
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
    private Object devops_compartment;
    private String repo_name;
    private String branch;
    private String build_command;
    private String artifact_location;
    private String registry_id;
    private String artifact_id;
    private String image_path;

    private String exposed_port;
    @PropertyOrder(1)
    public String getApplication_name() {
        return application_name;
    }

    public void setApplication_name(String application_name) {
        this.application_name = application_name;
    }
    @PropertyOrder(2)
    public int getNb_copies() {
        return nb_copies;
    }

    public void setNb_copies(int nb_copies) {
        this.nb_copies = nb_copies;
    }
    @PropertyOrder(3)
    public Application_source getApplication_source() {
        return application_source;
    }

    public void setApplication_source(Application_source application_source) {
        this.application_source = application_source;
    }
    @PropertyOrder(4)
    public Application_type getApplication_type() {
        return application_type;
    }

    public void setApplication_type(Application_type application_type) {
        this.application_type = application_type;
    }
    @PropertyOrder(5)
    public Object getDevops_compartment() {
        return devops_compartment;
    }

    public void setDevops_compartment(Object devops_compartment) {
        this.devops_compartment = devops_compartment;
    }
    @PropertyOrder(6)
    public String getRepo_name() {
        return repo_name;
    }

    public void setRepo_name(String repo_name) {
        this.repo_name = repo_name;
    }
    @PropertyOrder(7)
    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
    @PropertyOrder(8)
    public String getBuild_command() {
        return build_command;
    }

    public void setBuild_command(String build_command) {
        this.build_command = build_command;
    }
    @PropertyOrder(9)
    public String getArtifact_location() {
        return artifact_location;
    }

    public void setArtifact_location(String artifact_location) {
        this.artifact_location = artifact_location;
    }
    @PropertyOrder(10)
    public String getRegistry_id() {
        return registry_id;
    }

    public void setRegistry_id(String registry_id) {
        this.registry_id = registry_id;
    }
    @PropertyOrder(11)
    public String getArtifact_id() {
        return artifact_id;
    }

    public void setArtifact_id(String artifact_id) {
        this.artifact_id = artifact_id;
    }
    @PropertyOrder(12)
    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }
    @PropertyOrder(13)
    public String getExposed_port() {
        return exposed_port;
    }

    public void setExposed_port(String exposed_port) {
        this.exposed_port = exposed_port;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}