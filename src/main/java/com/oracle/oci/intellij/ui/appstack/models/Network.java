package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Network extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean create_new_vcn;

    private java.lang.Object vcn_compartment_id;

    private java.lang.Object existing_vcn_id;

    private java.lang.String vcn_cidr;

    private boolean use_existing_app_subnet;

    private java.lang.Object existing_app_subnet_id;

    private java.lang.String app_subnet_cidr;

    private boolean use_existing_db_subnet;
    private java.lang.Object existing_db_subnet_id;

    private java.lang.String db_subnet_cidr;

    private boolean use_existing_lb_subnet;

    private java.lang.Object existing_lb_subnet_id;

    private java.lang.String lb_subnet_cidr;

    private boolean open_https_port;

    private boolean use_default_lb_configuration;

    private int maximum_bandwidth_in_mbps;

    private int minimum_bandwidth_in_mbps;

    private java.lang.String health_checker_url_path;

    private int health_checker_return_code;

    private boolean enable_session_affinity;

    public enum Session_affinity{
        Enable_application_cookie_persistence,
        Enable_load_balancer_cookie_persistence,
    }
    private Session_affinity session_affinity;

    private java.lang.String session_affinity_cookie_name;

    @PropertyOrder(1)
    @VariableMetaData(title="Create new VCN",defaultVal="true",type="boolean",required=true)

    public boolean isCreate_new_vcn() {
        return create_new_vcn;
    }
    @PropertyOrder(2)
    @VariableMetaData(title="The compartment of the existing VCN.",defaultVal="compartment_ocid",type="oci:identity:compartment:id",required=true,visible="not(create_new_vcn)")

    public Object getVcn_compartment_id() {
        return vcn_compartment_id;
    }
    @PropertyOrder(3)
    @VariableMetaData(title="Select to VCN",type="oci:core:vcn:id",dependsOn="{compartmentId=${vcn_compartment_id}}",required=true,visible="not(create_new_vcn)")

    public Object getExisting_vcn_id() {
        return existing_vcn_id;
    }
    @PropertyOrder(4)
    @VariableMetaData(title="VCN IPv4 CIDR Blocks",description="This VCN will be used for all resources created by the stack.",defaultVal="10.0.0.0/24",type="string",required=true,visible="create_new_vcn")

    public String getVcn_cidr() {
        return vcn_cidr;
    }
    @PropertyOrder(5)
    @VariableMetaData(title="Use existing Application Subnet",defaultVal="false",type="boolean",required=true,visible="not(create_new_vcn)")

    public boolean isUse_existing_app_subnet() {
        return use_existing_app_subnet;
    }
    @PropertyOrder(6)
    @VariableMetaData(title="Select the application subnet",type="oci:core:subnet:id",dependsOn="{compartmentId=${vcn_compartment_id}, vcnId=${existing_vcn_id}, hidePublicSubnet=true}",required=true,visible="use_existing_app_subnet")

    public Object getExisting_app_subnet_id() {
        return existing_app_subnet_id;
    }
    @PropertyOrder(7)
    @VariableMetaData(title="Application Subnet IPv4 CIDR Blocks",description="The container instances running the application will be created in this subnet.",defaultVal="10.0.0.0/25",type="string",required=true,visible="not(use_existing_app_subnet)")

    public String getApp_subnet_cidr() {
        return app_subnet_cidr;
    }
    @PropertyOrder(8)
    @VariableMetaData(title="Use existing Database Subnet",defaultVal="false",type="boolean",required=true,visible="and(not(create_new_vcn),not(use_existing_database))")

    public boolean isUse_existing_db_subnet() {
        return use_existing_db_subnet;
    }
    @PropertyOrder(9)
    @VariableMetaData(title="Select the database subnet",type="oci:core:subnet:id",dependsOn="{compartmentId=${vcn_compartment_id}, vcnId=${existing_vcn_id}, hidePublicSubnet=true}",required=true,visible="use_existing_db_subnet")

    public Object getExisting_db_subnet_id() {
        return existing_db_subnet_id;
    }
    @PropertyOrder(10)
    @VariableMetaData(title="Database Subnet Creation: IPv4 CIDR Blocks",description="The Autonomous Database will be created in this subnet. For example: 10.0.0.128/26",defaultVal="10.0.0.128/26",type="string",required=true,visible="and(not(use_existing_db_subnet),not(use_existing_database))")

    public String getDb_subnet_cidr() {
        return db_subnet_cidr;
    }
    @PropertyOrder(11)
    @VariableMetaData(title="Use existing Load Balancer Subnet",defaultVal="false",type="boolean",required=true,visible="not(create_new_vcn)")

    public boolean isUse_existing_lb_subnet() {
        return use_existing_lb_subnet;
    }
    @PropertyOrder(12)
    @VariableMetaData(title="Select the load balancer subnet",type="oci:core:subnet:id",dependsOn="{compartmentId=${vcn_compartment_id}, vcnId=${existing_vcn_id}, hidePublicSubnet=false}",required=true,visible="use_existing_lb_subnet")

    public Object getExisting_lb_subnet_id() {
        return existing_lb_subnet_id;
    }
    @PropertyOrder(13)
    @VariableMetaData(title="Load balancer Subnet IPv4 CIDR Blocks",description="The load balancer will be created in this subnet.",defaultVal="10.0.0.192/26",type="string",required=true,visible="not(use_existing_lb_subnet)")

    public String getLb_subnet_cidr() {
        return lb_subnet_cidr;
    }
    @PropertyOrder(14)
    @VariableMetaData(title="Open load balancer's HTTPS port",description="By checking this checkbox you agree to make the load balancer subnet public and to open the HTTPS port of the load balancer to the Internet.",defaultVal="false",type="boolean",required=true)

    public boolean isOpen_https_port() {
        return open_https_port;
    }
    @PropertyOrder(15)
    @VariableMetaData(title="Use default load balancer configuration",defaultVal="true",type="boolean",required=true)

    public boolean isUse_default_lb_configuration() {
        return use_default_lb_configuration;
    }
    @PropertyOrder(16)
    @VariableMetaData(title="Maximum bandwidth (Mbps)",description="10Mbps for always free load balancer",defaultVal="10",type="number",required=true,visible="not(use_default_lb_configuration)")

    public int getMaximum_bandwidth_in_mbps() {
        return maximum_bandwidth_in_mbps;
    }
    @PropertyOrder(17)
    @VariableMetaData(title="Minimum bandwidth (Mbps)",description="10Mbps for always free load balancer",defaultVal="10",type="number",required=true,visible="not(use_default_lb_configuration)")

    public int getMinimum_bandwidth_in_mbps() {
        return minimum_bandwidth_in_mbps;
    }
    @PropertyOrder(18)
    @VariableMetaData(title="URL path (URI)",description="This url will be used by the health checker to verify that the application is running",defaultVal="/",type="string",required=true,visible="not(use_default_lb_configuration)")

    public String getHealth_checker_url_path() {
        return health_checker_url_path;
    }
    @PropertyOrder(19)
    @VariableMetaData(title="Status code",description="Status code returned by the health checker url when the application is running",defaultVal="200",type="number",required=true,visible="not(use_default_lb_configuration)")

    public int getHealth_checker_return_code() {
        return health_checker_return_code;
    }
    @PropertyOrder(20)
    @VariableMetaData(title="Enable cookie-based session persistence",defaultVal="false",type="boolean",required=true,visible="use_default_lb_configuration")

    public boolean isEnable_session_affinity() {
        return enable_session_affinity;
    }
    @PropertyOrder(21)
    @VariableMetaData(title="Session persistence",description="Specify whether the cookie is generated by your application server or by the load balancer.",type="enum",required=true,enumValues ="Enable application cookie persistence, Enable load balancer cookie persistence",visible="enable_session_affinity")

    public Session_affinity getSession_affinity() {
        return session_affinity;
    }
    @PropertyOrder(22)
    @VariableMetaData(title="Cookie name",defaultVal="X-Oracle-BMC-LBS-Route",type="string",required=true,visible="enable_session_affinity")

    public String getSession_affinity_cookie_name() {
        return session_affinity_cookie_name;
    }

    public void setCreate_new_vcn(boolean create_new_vcn) {
        this.create_new_vcn = create_new_vcn;
    }

    public void setVcn_compartment_id(Object vcn_compartment_id) {
        this.vcn_compartment_id = vcn_compartment_id;
    }

    public void setExisting_vcn_id(Object existing_vcn_id) {
        this.existing_vcn_id = existing_vcn_id;
    }

    public void setVcn_cidr(String vcn_cidr) {
        this.vcn_cidr = vcn_cidr;
    }

    public void setUse_existing_app_subnet(boolean use_existing_app_subnet) {
        this.use_existing_app_subnet = use_existing_app_subnet;
    }

    public void setExisting_app_subnet_id(Object existing_app_subnet_id) {
        this.existing_app_subnet_id = existing_app_subnet_id;
    }

    public void setApp_subnet_cidr(String app_subnet_cidr) {
        this.app_subnet_cidr = app_subnet_cidr;
    }

    public void setUse_existing_db_subnet(boolean use_existing_db_subnet) {
        this.use_existing_db_subnet = use_existing_db_subnet;
    }

    public void setExisting_db_subnet_id(Object existing_db_subnet_id) {
        this.existing_db_subnet_id = existing_db_subnet_id;
    }

    public void setDb_subnet_cidr(String db_subnet_cidr) {
        this.db_subnet_cidr = db_subnet_cidr;
    }

    public void setUse_existing_lb_subnet(boolean use_existing_lb_subnet) {
        this.use_existing_lb_subnet = use_existing_lb_subnet;
    }

    public void setExisting_lb_subnet_id(Object existing_lb_subnet_id) {
        this.existing_lb_subnet_id = existing_lb_subnet_id;
    }

    public void setLb_subnet_cidr(String lb_subnet_cidr) {
        this.lb_subnet_cidr = lb_subnet_cidr;
    }

    public void setOpen_https_port(boolean open_https_port) {
        this.open_https_port = open_https_port;
    }

    public void setUse_default_lb_configuration(boolean use_default_lb_configuration) {
        this.use_default_lb_configuration = use_default_lb_configuration;
    }

    public void setMaximum_bandwidth_in_mbps(int maximum_bandwidth_in_mbps) {
        this.maximum_bandwidth_in_mbps = maximum_bandwidth_in_mbps;
    }

    public void setMinimum_bandwidth_in_mbps(int minimum_bandwidth_in_mbps) {
        this.minimum_bandwidth_in_mbps = minimum_bandwidth_in_mbps;
    }

    public void setHealth_checker_url_path(String health_checker_url_path) {
        this.health_checker_url_path = health_checker_url_path;
    }

    public void setHealth_checker_return_code(int health_checker_return_code) {
        this.health_checker_return_code = health_checker_return_code;
    }

    public void setEnable_session_affinity(boolean enable_session_affinity) {
        this.enable_session_affinity = enable_session_affinity;
    }

    public void setSession_affinity(Session_affinity session_affinity) {
        this.session_affinity = session_affinity;
    }

    public void setSession_affinity_cookie_name(String session_affinity_cookie_name) {
        this.session_affinity_cookie_name = session_affinity_cookie_name;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}