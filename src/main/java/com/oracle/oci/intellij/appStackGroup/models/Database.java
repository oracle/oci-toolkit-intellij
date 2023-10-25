package com.oracle.oci.intellij.appStackGroup.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Database extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean use_existing_database;

    private String autonomous_database_display_name;

    private Object autonomous_database_admin_password;

    private int data_storage_size_in_tbs;

    private int cpu_core_count;

    private int ocpu_count;

    private Object autonomous_database;

    private String autonomous_database_user;

    private Object autonomous_database_password;

    private boolean use_connection_url_env;

    private String connection_url_env;

    private boolean use_tns_admin_env;

    private String tns_admin_env;

    private boolean use_username_env;

    private String username_env;

    private boolean use_password_env;

    private String password_env;

    public boolean isUse_existing_database() {
        return use_existing_database;
    }

    public void setUse_existing_database(boolean use_existing_database) {
        this.use_existing_database = use_existing_database;
    }

    public String getAutonomous_database_display_name() {
        return autonomous_database_display_name;
    }

    public void setAutonomous_database_display_name(String autonomous_database_display_name) {
        this.autonomous_database_display_name = autonomous_database_display_name;
    }

    public Object getAutonomous_database_admin_password() {
        return autonomous_database_admin_password;
    }

    public void setAutonomous_database_admin_password(Object autonomous_database_admin_password) {
        this.autonomous_database_admin_password = autonomous_database_admin_password;
    }

    public int getData_storage_size_in_tbs() {
        return data_storage_size_in_tbs;
    }

    public void setData_storage_size_in_tbs(int data_storage_size_in_tbs) {
        this.data_storage_size_in_tbs = data_storage_size_in_tbs;
    }

    public int getCpu_core_count() {
        return cpu_core_count;
    }

    public void setCpu_core_count(int cpu_core_count) {
        this.cpu_core_count = cpu_core_count;
    }

    public int getOcpu_count() {
        return ocpu_count;
    }

    public void setOcpu_count(int ocpu_count) {
        this.ocpu_count = ocpu_count;
    }

    public Object getAutonomous_database() {
        return autonomous_database;
    }

    public void setAutonomous_database(Object autonomous_database) {
        this.autonomous_database = autonomous_database;
    }

    public String getAutonomous_database_user() {
        return autonomous_database_user;
    }

    public void setAutonomous_database_user(String autonomous_database_user) {
        this.autonomous_database_user = autonomous_database_user;
    }

    public Object getAutonomous_database_password() {
        return autonomous_database_password;
    }

    public void setAutonomous_database_password(Object autonomous_database_password) {
        this.autonomous_database_password = autonomous_database_password;
    }

    public boolean isUse_connection_url_env() {
        return use_connection_url_env;
    }

    public void setUse_connection_url_env(boolean use_connection_url_env) {
        this.use_connection_url_env = use_connection_url_env;
    }

    public String getConnection_url_env() {
        return connection_url_env;
    }

    public void setConnection_url_env(String connection_url_env) {
        this.connection_url_env = connection_url_env;
    }

    public boolean isUse_tns_admin_env() {
        return use_tns_admin_env;
    }

    public void setUse_tns_admin_env(boolean use_tns_admin_env) {
        this.use_tns_admin_env = use_tns_admin_env;
    }

    public String getTns_admin_env() {
        return tns_admin_env;
    }

    public void setTns_admin_env(String tns_admin_env) {
        this.tns_admin_env = tns_admin_env;
    }

    public boolean isUse_username_env() {
        return use_username_env;
    }

    public void setUse_username_env(boolean use_username_env) {
        this.use_username_env = use_username_env;
    }

    public String getUsername_env() {
        return username_env;
    }

    public void setUsername_env(String username_env) {
        this.username_env = username_env;
    }

    public boolean isUse_password_env() {
        return use_password_env;
    }

    public void setUse_password_env(boolean use_password_env) {
        this.use_password_env = use_password_env;
    }

    public String getPassword_env() {
        return password_env;
    }

    public void setPassword_env(String password_env) {
        this.password_env = password_env;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}