package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Application_configuration_SSL_communication_between_backends_and_load_balancer extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @VariableMetaData(title="Use default SSL properties for Spring",description="The stack creates a self-signed certificate that will be used for the communication between the load balancer and the backends. This self-signed certificate is stored in a JKS keystore. The following properties can be used to configure the web server to use this JKS keystore. By default Spring boot properties will be used by the stack. Click on this checkbox to specify your own property names.",defaultVal="true",type="boolean",visible="and(eq(application_type,\"JAR\"),not(eq(application_source,\"IMAGE\")))")
    private boolean use_default_ssl_configuration;

    @VariableMetaData(title="Server port number property name",description="Assuming that your application can consume a property to configure the server port, this field can be used to specify the name of the property.",defaultVal="server.port",type="string",required=true,visible="not(use_default_ssl_configuration)")
    private java.lang.String port_property;

    @VariableMetaData(title="SSL keystore filename property name",description="Assuming that your application can consume a property to configure the SSL keystore filename, this field can be used to specify the name of the property.",defaultVal="server.ssl.key-store",type="string",required=true,visible="not(use_default_ssl_configuration)")
    private java.lang.String keystore_property;

    @VariableMetaData(title="SSL key alias property name",description="Assuming that your application can consume a property to configure the SSL key alias property name, this field can be used to specify the name of the property.",defaultVal="server.ssl.key-alias",type="string",required=true,visible="not(use_default_ssl_configuration)")
    private java.lang.String key_alias_property;

    @VariableMetaData(title="SSL keystore password property name",description="Assuming that your application can consume a property to configure the SSL keystore password property name, this field can be used to specify the name of the property.",defaultVal="server.ssl.key-store-password",type="string",required=true,visible="not(use_default_ssl_configuration)")
    private java.lang.String keystore_password_property;

    @VariableMetaData(title="SSL keystore type property name",description="Assuming that your application can consume a property to configure the SSL keystore type property name, this field can be used to specify the name of the property.",defaultVal="server.ssl.key-store-type",type="string",required=true,visible="not(use_default_ssl_configuration)")
    private java.lang.String keystore_type_property;

    @VariableMetaData(title="SSL certificate",type="text",required=true,visible="eq(application_source,'IMAGE')")
    private java.lang.Object cert_pem;

    @VariableMetaData(title="Private key",type="text",required=true,visible="eq(application_source,'IMAGE')")
    private java.lang.Object private_key_pem;

    @VariableMetaData(title="CA certificate",type="text",required=true,visible="eq(application_source,'IMAGE')")
    private java.lang.Object ca_pem;

    @PropertyOrder(1)
    public boolean isUse_default_ssl_configuration() {
        return use_default_ssl_configuration;
    }

    public void setUse_default_ssl_configuration(boolean use_default_ssl_configuration) {
        this.use_default_ssl_configuration = use_default_ssl_configuration;
    }
    @PropertyOrder(2)
    public String getPort_property() {
        return port_property;
    }

    public void setPort_property(String port_property) {
        this.port_property = port_property;
    }
    @PropertyOrder(3)
    public String getKeystore_property() {
        return keystore_property;
    }

    public void setKeystore_property(String keystore_property) {
        this.keystore_property = keystore_property;
    }
    @PropertyOrder(4)

    public String getKey_alias_property() {
        return key_alias_property;
    }

    public void setKey_alias_property(String key_alias_property) {
        this.key_alias_property = key_alias_property;
    }
    @PropertyOrder(5)

    public String getKeystore_password_property() {
        return keystore_password_property;
    }

    public void setKeystore_password_property(String keystore_password_property) {
        this.keystore_password_property = keystore_password_property;
    }
    @PropertyOrder(6)
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