package com.oracle.oci.intellij.ui.appstack.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Hide_constants_and_internal_variables extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private String tenancy_ocid;

    private String compartment_ocid;

    private String current_user_ocid;

    private String region;

    private String marketplace_source_images;

    private String env_variables;

    private String devops_pipeline_image;

    private String devops_deploy_shape;

    private String devops_memory;

    private String devops_ocpu;

    private String lb_health_check_timeout_in_millis;

    private String lb_health_check_interval_ms;

    private String lb_health_check_retries;

    private String lb_listener_cypher_suite;

    private String db_version;

    private String db_license_model;

    public String getTenancy_ocid() {
        return tenancy_ocid;
    }

    public void setTenancy_ocid(String tenancy_ocid) {
        this.tenancy_ocid = tenancy_ocid;
    }

    public String getCompartment_ocid() {
        return compartment_ocid;
    }

    public void setCompartment_ocid(String compartment_ocid) {
        this.compartment_ocid = compartment_ocid;
    }

    public String getCurrent_user_ocid() {
        return current_user_ocid;
    }

    public void setCurrent_user_ocid(String current_user_ocid) {
        this.current_user_ocid = current_user_ocid;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getMarketplace_source_images() {
        return marketplace_source_images;
    }

    public void setMarketplace_source_images(String marketplace_source_images) {
        this.marketplace_source_images = marketplace_source_images;
    }

    public String getEnv_variables() {
        return env_variables;
    }

    public void setEnv_variables(String env_variables) {
        this.env_variables = env_variables;
    }

    public String getDevops_pipeline_image() {
        return devops_pipeline_image;
    }

    public void setDevops_pipeline_image(String devops_pipeline_image) {
        this.devops_pipeline_image = devops_pipeline_image;
    }

    public String getDevops_deploy_shape() {
        return devops_deploy_shape;
    }

    public void setDevops_deploy_shape(String devops_deploy_shape) {
        this.devops_deploy_shape = devops_deploy_shape;
    }

    public String getDevops_memory() {
        return devops_memory;
    }

    public void setDevops_memory(String devops_memory) {
        this.devops_memory = devops_memory;
    }

    public String getDevops_ocpu() {
        return devops_ocpu;
    }

    public void setDevops_ocpu(String devops_ocpu) {
        this.devops_ocpu = devops_ocpu;
    }

    public String getLb_health_check_timeout_in_millis() {
        return lb_health_check_timeout_in_millis;
    }

    public void setLb_health_check_timeout_in_millis(String lb_health_check_timeout_in_millis) {
        this.lb_health_check_timeout_in_millis = lb_health_check_timeout_in_millis;
    }

    public String getLb_health_check_interval_ms() {
        return lb_health_check_interval_ms;
    }

    public void setLb_health_check_interval_ms(String lb_health_check_interval_ms) {
        this.lb_health_check_interval_ms = lb_health_check_interval_ms;
    }

    public String getLb_health_check_retries() {
        return lb_health_check_retries;
    }

    public void setLb_health_check_retries(String lb_health_check_retries) {
        this.lb_health_check_retries = lb_health_check_retries;
    }

    public String getLb_listener_cypher_suite() {
        return lb_listener_cypher_suite;
    }

    public void setLb_listener_cypher_suite(String lb_listener_cypher_suite) {
        this.lb_listener_cypher_suite = lb_listener_cypher_suite;
    }

    public String getDb_version() {
        return db_version;
    }

    public void setDb_version(String db_version) {
        this.db_version = db_version;
    }

    public String getDb_license_model() {
        return db_license_model;
    }

    public void setDb_license_model(String db_license_model) {
        this.db_license_model = db_license_model;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}