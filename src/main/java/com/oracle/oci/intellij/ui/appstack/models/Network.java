package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Network extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean create_new_vcn;

    private Object vcn_compartment_id;

    private Object existing_vcn_id;

    private String vcn_cidr;

    private boolean use_existing_app_subnet;

    private Object existing_app_subnet_id;

    private String app_subnet_cidr;

    private boolean use_existing_db_subnet;

    private Object existing_db_subnet_id;

    private String db_subnet_cidr;

    private boolean use_existing_lb_subnet;

    private Object existing_lb_subnet_id;

    private String lb_subnet_cidr;

    private boolean open_https_port;

    private boolean use_default_lb_configuration;

    private int maximum_bandwidth_in_mbps;

    private int minimum_bandwidth_in_mbps;

    private String health_checker_url_path;

    private int health_checker_return_code;

    private boolean enable_session_affinity;

    private enum Session_affinity{
        Enable_application_cookie_persistence,
        Enable_load_balancer_cookie_persistence,
    }

    private Session_affinity session_affinity;

;

    private String session_affinity_cookie_name;
    @PropertyOrder(1)
    public boolean isCreate_new_vcn() {
        return create_new_vcn;
    }
    @PropertyOrder(2)
    public Object getVcn_compartment_id() {
        return vcn_compartment_id;
    }
    @PropertyOrder(3)
    public Object getExisting_vcn_id() {
        return existing_vcn_id;
    }
    @PropertyOrder(4)
    public String getVcn_cidr() {
        return vcn_cidr;
    }
    @PropertyOrder(5)
    public boolean isUse_existing_app_subnet() {
        return use_existing_app_subnet;
    }
    @PropertyOrder(6)
    public Object getExisting_app_subnet_id() {
        return existing_app_subnet_id;
    }
    @PropertyOrder(7)
    public String getApp_subnet_cidr() {
        return app_subnet_cidr;
    }
    @PropertyOrder(8)
    public boolean isUse_existing_db_subnet() {
        return use_existing_db_subnet;
    }
    @PropertyOrder(9)
    public Object getExisting_db_subnet_id() {
        return existing_db_subnet_id;
    }
    @PropertyOrder(10)
    public String getDb_subnet_cidr() {
        return db_subnet_cidr;
    }
    @PropertyOrder(11)
    public boolean isUse_existing_lb_subnet() {
        return use_existing_lb_subnet;
    }
    @PropertyOrder(12)
    public Object getExisting_lb_subnet_id() {
        return existing_lb_subnet_id;
    }
    @PropertyOrder(13)
    public String getLb_subnet_cidr() {
        return lb_subnet_cidr;
    }
    @PropertyOrder(14)
    public boolean isOpen_https_port() {
        return open_https_port;
    }
    @PropertyOrder(15)
    public boolean isUse_default_lb_configuration() {
        return use_default_lb_configuration;
    }
    @PropertyOrder(16)
    public int getMaximum_bandwidth_in_mbps() {
        return maximum_bandwidth_in_mbps;
    }
    @PropertyOrder(17)
    public int getMinimum_bandwidth_in_mbps() {
        return minimum_bandwidth_in_mbps;
    }
    @PropertyOrder(18)
    public String getHealth_checker_url_path() {
        return health_checker_url_path;
    }
    @PropertyOrder(19)
    public int getHealth_checker_return_code() {
        return health_checker_return_code;
    }
    @PropertyOrder(20)
    public boolean isEnable_session_affinity() {
        return enable_session_affinity;
    }
    @PropertyOrder(21)
    public Session_affinity getSession_affinity() {
        return session_affinity;
    }
    @PropertyOrder(22)
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