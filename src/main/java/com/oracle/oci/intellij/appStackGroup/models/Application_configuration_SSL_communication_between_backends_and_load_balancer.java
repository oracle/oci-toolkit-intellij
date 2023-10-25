package com.oracle.oci.intellij.appStackGroup.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Application_configuration_SSL_communication_between_backends_and_load_balancer extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean use_default_ssl_configuration;

    private String port_property;

    private String keystore_property;

    private String key_alias_property;

    private String keystore_password_property;

    private String keystore_type_property;

    private Object cert_pem;

    private Object private_key_pem;

    private Object ca_pem;

    public boolean isUse_default_ssl_configuration() {
        return use_default_ssl_configuration;
    }

    public void setUse_default_ssl_configuration(boolean use_default_ssl_configuration) {
        this.use_default_ssl_configuration = use_default_ssl_configuration;
    }

    public String getPort_property() {
        return port_property;
    }

    public void setPort_property(String port_property) {
        this.port_property = port_property;
    }

    public String getKeystore_property() {
        return keystore_property;
    }

    public void setKeystore_property(String keystore_property) {
        this.keystore_property = keystore_property;
    }

    public String getKey_alias_property() {
        return key_alias_property;
    }

    public void setKey_alias_property(String key_alias_property) {
        this.key_alias_property = key_alias_property;
    }

    public String getKeystore_password_property() {
        return keystore_password_property;
    }

    public void setKeystore_password_property(String keystore_password_property) {
        this.keystore_password_property = keystore_password_property;
    }

    public String getKeystore_type_property() {
        return keystore_type_property;
    }

    public void setKeystore_type_property(String keystore_type_property) {
        this.keystore_type_property = keystore_type_property;
    }

    public Object getCert_pem() {
        return cert_pem;
    }

    public void setCert_pem(Object cert_pem) {
        this.cert_pem = cert_pem;
    }

    public Object getPrivate_key_pem() {
        return private_key_pem;
    }

    public void setPrivate_key_pem(Object private_key_pem) {
        this.private_key_pem = private_key_pem;
    }

    public Object getCa_pem() {
        return ca_pem;
    }

    public void setCa_pem(Object ca_pem) {
        this.ca_pem = ca_pem;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}