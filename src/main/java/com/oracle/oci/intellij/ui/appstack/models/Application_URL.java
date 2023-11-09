package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Application_URL extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean create_fqdn;

    private java.lang.Object dns_compartment;

    private java.lang.String zone;

    private java.lang.String subdomain;

    private java.lang.String certificate_ocid;

    @PropertyOrder(1)
    @VariableMetaData(title="Create DNS record",description="If you check this checkbox the stack will create a DNS record that will resolve to the load balancer's IP address.",defaultVal="true",type="boolean",required=true)

    public boolean isCreate_fqdn() {
        return create_fqdn;
    }

    public void setCreate_fqdn(boolean create_fqdn) {
        this.create_fqdn = create_fqdn;
    }
    @PropertyOrder(2)
    @VariableMetaData(title="DNS and Certificate compartement",description="Compartment containing the DNS Zone and the Certificate",defaultVal="compartment_ocid",type="oci:identity:compartment:id",required=true,visible="create_fqdn")

    public Object getDns_compartment() {
        return dns_compartment;
    }

    public void setDns_compartment(Object dns_compartment) {
        this.dns_compartment = dns_compartment;
    }
    @PropertyOrder(3)
    @VariableMetaData(title="DNS Zone",description="Domain name in which the host name will be created.",type="string",required=true,visible="create_fqdn")

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
    @PropertyOrder(4)
    @VariableMetaData(title="Host name",description="The host name will be created on the selected Zone and will resolve to the the load balancer's IP address.",type="string",required=true,visible="create_fqdn")
    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }
    @PropertyOrder(5)
    @VariableMetaData(title="Certificate OCID",description="You must have a SSL certificate available in OCI Certificates service. Provide the certificate OCID for the host name.",type="string",required=true,visible="create_fqdn")
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