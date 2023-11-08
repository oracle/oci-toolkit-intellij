package com.oracle.oci.intellij.ui.appstack.models;

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

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}