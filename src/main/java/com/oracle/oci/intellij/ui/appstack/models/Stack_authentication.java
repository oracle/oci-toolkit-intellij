package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Stack_authentication extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @VariableMetaData(title="Use existing authentication token",description="This token will be used by the stack to authenticate the user when connecting to the code repository or container registry.",defaultVal="true",type="boolean",required=true)
    private boolean use_existing_token;

    @VariableMetaData(title="User's authentication token",type="password",required=true,visible="use_existing_token")
    private java.lang.Object current_user_token;

    @VariableMetaData(title="Use an existing key vault",description="This vault will be used to store the authentication token needed by the build and deploy pipelines to publish the container image to the container registry.",defaultVal="true",type="boolean",required=true)
    private boolean use_existing_vault;

    @VariableMetaData(title="Key vault display name",description="A user-friendly name to help you easily identify the resource.",type="string",required=true,visible="not(use_existing_vault)")
    private java.lang.String new_vault_display_name;

    @VariableMetaData(title="Compartment",description="The compartment containing the existing vault.",defaultVal="compartment_ocid",type="oci:identity:compartment:id",required=true,visible="and(use_existing_vault)")
    private java.lang.Object vault_compartment_id;

    @VariableMetaData(title="Vault",description="Choose an existing vault used to store the authentication token.",type="oci:kms:vault:id",dependsOn="{compartmentId=${vault_compartment_id}}",required=true,visible="and(use_existing_vault)")
    private java.lang.Object vault_id;

    @VariableMetaData(title="Encryption key",description="This key will be used to encrypt the sensitive information stored as vault secrets.",type="oci:kms:key:id",dependsOn="{compartmentId=${vault_compartment_id}, vaultId=${vault_id}}",required=true,visible="and(use_existing_vault)")
    private java.lang.Object key_id;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}