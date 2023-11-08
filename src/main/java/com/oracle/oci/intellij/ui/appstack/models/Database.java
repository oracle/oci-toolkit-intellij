package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Database extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @VariableMetaData(title="Use existing database (Autonomous Database Serverless)",defaultVal="true",type="boolean",required=true)
    private boolean use_existing_database;

    @VariableMetaData(title="Autonomous Database display name",description="A user-friendly name to help you easily identify the resource.",type="string",required=true,visible="not(use_existing_database)")
    private java.lang.String autonomous_database_display_name;

    @VariableMetaData(title="Database ADMIN password",description="Password must be 12 to 30 characters and contain at least one uppercase letter, one lowercase letter, and one number. The password cannot contain the double quote (\") character or the username \"admin\".",type="password",required=true,visible="not(use_existing_database)")
    private java.lang.Object autonomous_database_admin_password;

    @VariableMetaData(title="Storage (TB)",description="The amount of storage to allocate.",defaultVal="1",type="number",required=true,visible="not(use_existing_database)")
    private int data_storage_size_in_tbs;

    @VariableMetaData(title="CPU core count",description="The number of OCPU cores to be made available to the database",defaultVal="2",type="number",required=true,visible="not(use_existing_database)")
    private int cpu_core_count;

    @VariableMetaData(title="OCPU count",description="The number of OCPU cores to enable. Available cores are subject to your tenancy's service limits.",defaultVal="1",type="number",required=true,visible="not(use_existing_database)")
    private int ocpu_count;

    @VariableMetaData(title="Autonomous Database",description="The Autonomous Database used by the application.",type="oci:database:autonomousdatabase:id",dependsOn="{compartmentId=${compartment_id}}",required=true,visible="and(use_existing_database)")
    private java.lang.Object autonomous_database;

    @VariableMetaData(title="DB username",description="The username used to connect to the database.",defaultVal="",type="string",required=true,visible="and(use_existing_database)")
    private java.lang.String autonomous_database_user;

    @VariableMetaData(title="DB user password",description="The password of the user used to access the database.",type="password",required=true,visible="and(use_existing_database)")
    private java.lang.Object autonomous_database_password;

    @VariableMetaData(title="Set connection URL environment variable",description="Assuming that your application can consume an environment variable to configure the URL, this field can be used to specify the name of the environment variable.",defaultVal="true",type="boolean")
    private boolean use_connection_url_env;

    @VariableMetaData(title="Connection URL environment variable name",description="Specify the name of the environment variable. Its value will be set automatically by the stack.",defaultVal="SPRING_DATASOURCE_URL",type="string",required=true,visible="use_connection_url_env")
    private java.lang.String connection_url_env;

    @VariableMetaData(title="Set TNS_ADMIN environment variable",description="Assuming that your application can consume an environment variable to configure TNS_ADMIN, this field can be used to specify the name of the environment variable.",defaultVal="true",type="boolean",visible="eq(application_source,'IMAGE')")
    private boolean use_tns_admin_env;

    @VariableMetaData(title="TNS_ADMIN environment variable name",description="Specify the name of the environment variable (Ex. TNS_ADMIN).",defaultVal="TNS_ADMIN",type="string",required=true,visible="and(use_tns_admin_env,eq(application_source,'IMAGE'))")
    private java.lang.String tns_admin_env;

    @VariableMetaData(title="Set username environment variable",description="Assuming that your application can consume an environment variable to configure the database username, this field can be used to specify the name of the environment variable.",defaultVal="false",type="boolean",visible="eq(application_source,'IMAGE')")
    private boolean use_username_env;

    @VariableMetaData(title="Database user environment variable name",description="Only the name of the environment variable is needed. The value will be automatically set. If a new database is created, the database ADMIN user will be used.",defaultVal="SPRING_DATASOURCE_USERNAME",type="string",required=true,visible="use_username_env")
    private java.lang.String username_env;

    @VariableMetaData(title="Set password environment variable",description="Assuming that your application can consume an environment variable to configure the database user's password, this field can be used to specify the name of the environment variable.",defaultVal="false",type="boolean",visible="eq(application_source,'IMAGE')")
    private boolean use_password_env;

    @VariableMetaData(title="Database user's password environment variable name",description="Specify the name of the environment variable. Its value will be set automatically by the stack. If a new database is created, the database ADMIN user will be used.",defaultVal="SPRING_DATASOURCE_PASSWORD",type="string",required=true,visible="use_password_env")
    private java.lang.String password_env;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}