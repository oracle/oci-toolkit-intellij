package com.oracle.oci.intellij.ui.appstack.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Application_URL extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean create_fqdn;

    private Object dns_compartment;

    private String zone;

    private String subdomain;

    private String certificate_ocid;

    public boolean isCreate_fqdn() {
        return create_fqdn;
    }

    public void setCreate_fqdn(boolean create_fqdn) {
        this.create_fqdn = create_fqdn;
    }

    public Object getDns_compartment() {
        return dns_compartment;
    }

    public void setDns_compartment(Object dns_compartment) {
        this.dns_compartment = dns_compartment;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public String getCertificate_ocid() {
        return certificate_ocid;
    }

    public void setCertificate_ocid(String certificate_ocid) {
        this.certificate_ocid = certificate_ocid;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}