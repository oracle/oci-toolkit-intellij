package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Database extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean use_existing_database;

    private java.lang.String autonomous_database_display_name;

    private java.lang.Object autonomous_database_admin_password;

    private int data_storage_size_in_tbs;

    private int cpu_core_count;

    private int ocpu_count;

    private java.lang.Object autonomous_database;

    private java.lang.String autonomous_database_user;

    private java.lang.Object autonomous_database_password;

    private boolean use_connection_url_env;

    private java.lang.String connection_url_env;

    private boolean use_tns_admin_env;

    private java.lang.String tns_admin_env;

    private boolean use_username_env;

    private java.lang.String username_env;

    private boolean use_password_env;

    private java.lang.String password_env;

    @PropertyOrder(1)
    @VariableMetaData(title="Use existing database (Autonomous Database Serverless)",defaultVal="true",type="boolean",required=true)

    public boolean isUse_existing_database() {
        return use_existing_database;
    }
    @PropertyOrder(2)
    @VariableMetaData(title="Autonomous Database display name",description="A user-friendly name to help you easily identify the resource.",type="string",required=true,visible="not(use_existing_database)")

    public String getAutonomous_database_display_name() {
        return autonomous_database_display_name;
    }
    @PropertyOrder(3)
    @VariableMetaData(title="Database ADMIN password",description="Password must be 12 to 30 characters and contain at least one uppercase letter, one lowercase letter, and one number. The password cannot contain the double quote (\") character or the username \"admin\".",type="password",required=true,visible="not(use_existing_database)")

    public Object getAutonomous_database_admin_password() {
        return autonomous_database_admin_password;
    }
    @PropertyOrder(4)
    @VariableMetaData(title="Storage (TB)",description="The amount of storage to allocate.",defaultVal="1",type="number",required=true,visible="not(use_existing_database)")

    public int getData_storage_size_in_tbs() {
        return data_storage_size_in_tbs;
    }
    @PropertyOrder(5)
    @VariableMetaData(title="CPU core count",description="The number of OCPU cores to be made available to the database",defaultVal="2",type="number",required=true,visible="not(use_existing_database)")

    public int getCpu_core_count() {
        return cpu_core_count;
    }
    @PropertyOrder(6)
    @VariableMetaData(title="OCPU count",description="The number of OCPU cores to enable. Available cores are subject to your tenancy's service limits.",defaultVal="1",type="number",required=true,visible="not(use_existing_database)")

    public int getOcpu_count() {
        return ocpu_count;
    }
    @PropertyOrder(7)
    @VariableMetaData(title="Autonomous Database",description="The Autonomous Database used by the application.",type="oci:database:autonomousdatabase:id",dependsOn="{compartmentId=${compartment_id}}",required=true,visible="and(use_existing_database)")

    public Object getAutonomous_database() {
        return autonomous_database;
    }
    @PropertyOrder(8)
    @VariableMetaData(title="DB username",description="The username used to connect to the database.",defaultVal="",type="string",required=true,visible="and(use_existing_database)")

    public String getAutonomous_database_user() {
        return autonomous_database_user;
    }
    @PropertyOrder(9)
    @VariableMetaData(title="DB user password",description="The password of the user used to access the database.",type="password",required=true,visible="and(use_existing_database)")

    public Object getAutonomous_database_password() {
        return autonomous_database_password;
    }
    @PropertyOrder(10)
    @VariableMetaData(title="Set connection URL environment variable",description="Assuming that your application can consume an environment variable to configure the URL, this field can be used to specify the name of the environment variable.",defaultVal="true",type="boolean")

    public boolean isUse_connection_url_env() {
        return use_connection_url_env;
    }
    @PropertyOrder(11)
    @VariableMetaData(title="Connection URL environment variable name",description="Specify the name of the environment variable. Its value will be set automatically by the stack.",defaultVal="SPRING_DATASOURCE_URL",type="string",required=true,visible="use_connection_url_env")

    public String getConnection_url_env() {
        return connection_url_env;
    }
    @PropertyOrder(12)
    @VariableMetaData(title="Set TNS_ADMIN environment variable",description="Assuming that your application can consume an environment variable to configure TNS_ADMIN, this field can be used to specify the name of the environment variable.",defaultVal="true",type="boolean",visible="eq(application_source,'IMAGE')")

    public boolean isUse_tns_admin_env() {
        return use_tns_admin_env;
    }
    @PropertyOrder(13)
    @VariableMetaData(title="TNS_ADMIN environment variable name",description="Specify the name of the environment variable (Ex. TNS_ADMIN).",defaultVal="TNS_ADMIN",type="string",required=true,visible="and(use_tns_admin_env,eq(application_source,'IMAGE'))")

    public String getTns_admin_env() {
        return tns_admin_env;
    }
    @PropertyOrder(14)
    @VariableMetaData(title="Set username environment variable",description="Assuming that your application can consume an environment variable to configure the database username, this field can be used to specify the name of the environment variable.",defaultVal="false",type="boolean",visible="eq(application_source,'IMAGE')")

    public boolean isUse_username_env() {
        return use_username_env;
    }
    @PropertyOrder(15)
    @VariableMetaData(title="Database user environment variable name",description="Only the name of the environment variable is needed. The value will be automatically set. If a new database is created, the database ADMIN user will be used.",defaultVal="SPRING_DATASOURCE_USERNAME",type="string",required=true,visible="use_username_env")

    public String getUsername_env() {
        return username_env;
    }
    @PropertyOrder(16)
    @VariableMetaData(title="Set password environment variable",description="Assuming that your application can consume an environment variable to configure the database user's password, this field can be used to specify the name of the environment variable.",defaultVal="false",type="boolean",visible="eq(application_source,'IMAGE')")

    public boolean isUse_password_env() {
        return use_password_env;
    }
    @PropertyOrder(17)
    @VariableMetaData(title="Database user's password environment variable name",description="Specify the name of the environment variable. Its value will be set automatically by the stack. If a new database is created, the database ADMIN user will be used.",defaultVal="SPRING_DATASOURCE_PASSWORD",type="string",required=true,visible="use_password_env")

    public String getPassword_env() {
        return password_env;
    }

    public void setUse_existing_database(boolean use_existing_database) {
        this.use_existing_database = use_existing_database;
    }

    public void setAutonomous_database_display_name(String autonomous_database_display_name) {
        this.autonomous_database_display_name = autonomous_database_display_name;
    }

    public void setAutonomous_database_admin_password(Object autonomous_database_admin_password) {
        this.autonomous_database_admin_password = autonomous_database_admin_password;
    }

    public void setData_storage_size_in_tbs(int data_storage_size_in_tbs) {
        this.data_storage_size_in_tbs = data_storage_size_in_tbs;
    }

    public void setCpu_core_count(int cpu_core_count) {
        this.cpu_core_count = cpu_core_count;
    }

    public void setOcpu_count(int ocpu_count) {
        this.ocpu_count = ocpu_count;
    }

    public void setAutonomous_database(Object autonomous_database) {
        this.autonomous_database = autonomous_database;
    }

    public void setAutonomous_database_user(String autonomous_database_user) {
        this.autonomous_database_user = autonomous_database_user;
    }

    public void setAutonomous_database_password(Object autonomous_database_password) {
        this.autonomous_database_password = autonomous_database_password;
    }

    public void setUse_connection_url_env(boolean use_connection_url_env) {
        this.use_connection_url_env = use_connection_url_env;
    }

    public void setConnection_url_env(String connection_url_env) {
        this.connection_url_env = connection_url_env;
    }

    public void setUse_tns_admin_env(boolean use_tns_admin_env) {
        this.use_tns_admin_env = use_tns_admin_env;
    }

    public void setTns_admin_env(String tns_admin_env) {
        this.tns_admin_env = tns_admin_env;
    }

    public void setUse_username_env(boolean use_username_env) {
        this.use_username_env = use_username_env;
    }

    public void setUsername_env(String username_env) {
        this.username_env = username_env;
    }

    public void setUse_password_env(boolean use_password_env) {
        this.use_password_env = use_password_env;
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