package com.oracle.oci.intellij.appStackGroup.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Application extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private String application_name;

    private int nb_copies;

    private enum Application_source{
        IMAGE,
        ARTIFACT,
        SOURCE_CODE,
    }

    private Application_source application_source;

;

    private enum Application_type{
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

    public Application_type getApplication_type() {
        return application_type;
    }

    public void setApplication_type(Application_type application_type) {
        this.application_type = application_type;
    }

    public String getApplication_name() {
        return application_name;
    }

    public void setApplication_name(String application_name) {
        this.application_name = application_name;
    }

    public int getNb_copies() {
        return nb_copies;
    }

    public void setNb_copies(int nb_copies) {
        this.nb_copies = nb_copies;
    }

    public Object getDevops_compartment() {
        return devops_compartment;
    }

    public void setDevops_compartment(Object devops_compartment) {
        this.devops_compartment = devops_compartment;
    }

    public String getRepo_name() {
        return repo_name;
    }

    public void setRepo_name(String repo_name) {
        this.repo_name = repo_name;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBuild_command() {
        return build_command;
    }

    public void setBuild_command(String build_command) {
        this.build_command = build_command;
    }

    public String getArtifact_location() {
        return artifact_location;
    }

    public void setArtifact_location(String artifact_location) {
        this.artifact_location = artifact_location;
    }

    public String getRegistry_id() {
        return registry_id;
    }

    public void setRegistry_id(String registry_id) {
        this.registry_id = registry_id;
    }

    public String getArtifact_id() {
        return artifact_id;
    }

    public void setArtifact_id(String artifact_id) {
        this.artifact_id = artifact_id;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getExposed_port() {
        return exposed_port;
    }

    public void setExposed_port(String exposed_port) {
        this.exposed_port = exposed_port;
    }

    public Application_source getApplication_source() {
        return application_source;
    }

    public void setApplication_source(Application_source application_source) {
        this.application_source = application_source;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}