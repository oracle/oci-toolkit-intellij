package com.oracle.oci.intellij.appStackGroup.models;

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

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}