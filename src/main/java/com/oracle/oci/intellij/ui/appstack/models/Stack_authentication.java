package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.PropertyOrder;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Stack_authentication extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean use_existing_token;

    private java.lang.Object current_user_token;

    private boolean use_existing_vault;

    private java.lang.String new_vault_display_name;

    private java.lang.Object vault_compartment_id;

    private java.lang.Object vault_id;

    private java.lang.Object key_id;

    @PropertyOrder(1)
    @VariableMetaData(title="Use existing authentication token",description="This token will be used by the stack to authenticate the user when connecting to the code repository or container registry.",defaultVal="true",type="boolean",required=true)

    public boolean isUse_existing_token() {
        return use_existing_token;
    }

    public void setUse_existing_token(boolean use_existing_token) {
        this.use_existing_token = use_existing_token;
    }
    @PropertyOrder(2)
    @VariableMetaData(title="User's authentication token",type="password",required=true,visible="use_existing_token")

    public Object getCurrent_user_token() {
        return current_user_token;
    }

    public void setCurrent_user_token(Object current_user_token) {
        this.current_user_token = current_user_token;
    }
    @PropertyOrder(3)
    @VariableMetaData(title="Use an existing key vault",description="This vault will be used to store the authentication token needed by the build and deploy pipelines to publish the container image to the container registry.",defaultVal="true",type="boolean",required=true)

    public boolean isUse_existing_vault() {
        return use_existing_vault;
    }

    public void setUse_existing_vault(boolean use_existing_vault) {
        this.use_existing_vault = use_existing_vault;
    }
    @PropertyOrder(4)
    @VariableMetaData(title="Key vault display name",description="A user-friendly name to help you easily identify the resource.",type="string",required=true,visible="not(use_existing_vault)")

    public String getNew_vault_display_name() {
        return new_vault_display_name;
    }

    public void setNew_vault_display_name(String new_vault_display_name) {
        this.new_vault_display_name = new_vault_display_name;
    }
    @PropertyOrder(5)
    @VariableMetaData(title="Compartment",description="The compartment containing the existing vault.",defaultVal="compartment_ocid",type="oci:identity:compartment:id",required=true,visible="and(use_existing_vault)")

    public Object getVault_compartment_id() {
        return vault_compartment_id;
    }

    public void setVault_compartment_id(Object vault_compartment_id) {
        this.vault_compartment_id = vault_compartment_id;
    }
    @PropertyOrder(6)
    @VariableMetaData(title="Vault",description="Choose an existing vault used to store the authentication token.",type="oci:kms:vault:id",dependsOn="{compartmentId=${vault_compartment_id}}",required=true,visible="and(use_existing_vault)")

    public Object getVault_id() {
        return vault_id;
    }

    public void setVault_id(Object vault_id) {
        this.vault_id = vault_id;
    }
    @PropertyOrder(7)
    @VariableMetaData(title="Encryption key",description="This key will be used to encrypt the sensitive information stored as vault secrets.",type="oci:kms:key:id",dependsOn="{compartmentId=${vault_compartment_id}, vaultId=${vault_id}}",required=true,visible="and(use_existing_vault)")

    public Object getKey_id() {
        return key_id;
    }

    public void setKey_id(Object key_id) {
        this.key_id = key_id;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
}