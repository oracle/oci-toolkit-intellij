package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Network extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @VariableMetaData(title="Create new VCN",defaultVal="true",type="boolean",required=true)
    private boolean create_new_vcn;

    @VariableMetaData(title="The compartment of the existing VCN.",defaultVal="compartment_ocid",type="oci:identity:compartment:id",required=true,visible="not(create_new_vcn)")
    private java.lang.Object vcn_compartment_id;

    @VariableMetaData(title="Select to VCN",type="oci:core:vcn:id",dependsOn="{compartmentId=${vcn_compartment_id}}",required=true,visible="not(create_new_vcn)")
    private java.lang.Object existing_vcn_id;

    @VariableMetaData(title="VCN IPv4 CIDR Blocks",description="This VCN will be used for all resources created by the stack.",defaultVal="10.0.0.0/24",type="string",required=true,visible="create_new_vcn")
    private java.lang.String vcn_cidr;

    @VariableMetaData(title="Use existing Application Subnet",defaultVal="false",type="boolean",required=true,visible="not(create_new_vcn)")
    private boolean use_existing_app_subnet;

    @VariableMetaData(title="Select the application subnet",type="oci:core:subnet:id",dependsOn="{compartmentId=${vcn_compartment_id}, vcnId=${existing_vcn_id}, hidePublicSubnet=true}",required=true,visible="use_existing_app_subnet")
    private java.lang.Object existing_app_subnet_id;

    @VariableMetaData(title="Application Subnet IPv4 CIDR Blocks",description="The container instances running the application will be created in this subnet.",defaultVal="10.0.0.0/25",type="string",required=true,visible="not(use_existing_app_subnet)")
    private java.lang.String app_subnet_cidr;

    @VariableMetaData(title="Use existing Database Subnet",defaultVal="false",type="boolean",required=true,visible="and(not(create_new_vcn),not(use_existing_database))")
    private boolean use_existing_db_subnet;
    @VariableMetaData(title="Select the database subnet",type="oci:core:subnet:id",dependsOn="{compartmentId=${vcn_compartment_id}, vcnId=${existing_vcn_id}, hidePublicSubnet=true}",required=true,visible="use_existing_db_subnet")
    private java.lang.Object existing_db_subnet_id;

    @VariableMetaData(title="Database Subnet Creation: IPv4 CIDR Blocks",description="The Autonomous Database will be created in this subnet. For example: 10.0.0.128/26",defaultVal="10.0.0.128/26",type="string",required=true,visible="and(not(use_existing_db_subnet),not(use_existing_database))")
    private java.lang.String db_subnet_cidr;

    @VariableMetaData(title="Use existing Load Balancer Subnet",defaultVal="false",type="boolean",required=true,visible="not(create_new_vcn)")
    private boolean use_existing_lb_subnet;

    @VariableMetaData(title="Select the load balancer subnet",type="oci:core:subnet:id",dependsOn="{compartmentId=${vcn_compartment_id}, vcnId=${existing_vcn_id}, hidePublicSubnet=false}",required=true,visible="use_existing_lb_subnet")
    private java.lang.Object existing_lb_subnet_id;

    @VariableMetaData(title="Load balancer Subnet IPv4 CIDR Blocks",description="The load balancer will be created in this subnet.",defaultVal="10.0.0.192/26",type="string",required=true,visible="not(use_existing_lb_subnet)")
    private java.lang.String lb_subnet_cidr;

    @VariableMetaData(title="Open load balancer's HTTPS port",description="By checking this checkbox you agree to make the load balancer subnet public and to open the HTTPS port of the load balancer to the Internet.",defaultVal="false",type="boolean",required=true)
    private boolean open_https_port;

    @VariableMetaData(title="Use default load balancer configuration",defaultVal="true",type="boolean",required=true)
    private boolean use_default_lb_configuration;

    @VariableMetaData(title="Maximum bandwidth (Mbps)",description="10Mbps for always free load balancer",defaultVal="10",type="number",required=true,visible="not(use_default_lb_configuration)")
    private int maximum_bandwidth_in_mbps;

    @VariableMetaData(title="Minimum bandwidth (Mbps)",description="10Mbps for always free load balancer",defaultVal="10",type="number",required=true,visible="not(use_default_lb_configuration)")
    private int minimum_bandwidth_in_mbps;

    @VariableMetaData(title="URL path (URI)",description="This url will be used by the health checker to verify that the application is running",defaultVal="/",type="string",required=true,visible="not(use_default_lb_configuration)")
    private java.lang.String health_checker_url_path;

    @VariableMetaData(title="Status code",description="Status code returned by the health checker url when the application is running",defaultVal="200",type="number",required=true,visible="not(use_default_lb_configuration)")
    private int health_checker_return_code;

    @VariableMetaData(title="Enable cookie-based session persistence",defaultVal="false",type="boolean",required=true,visible="use_default_lb_configuration")
    private boolean enable_session_affinity;

    @VariableMetaData(title="Session persistence",description="Specify whether the cookie is generated by your application server or by the load balancer.",type="enum",required=true,enumValues ="[Enable application cookie persistence, Enable load balancer cookie persistence]",visible="enable_session_affinity")
    private enum session_affinity{
        Enable_application_cookie_persistence,
        Enable_load_balancer_cookie_persistence,
    }

;

    @VariableMetaData(title="Cookie name",defaultVal="X-Oracle-BMC-LBS-Route",type="string",required=true,visible="enable_session_affinity")
    private java.lang.String session_affinity_cookie_name;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}