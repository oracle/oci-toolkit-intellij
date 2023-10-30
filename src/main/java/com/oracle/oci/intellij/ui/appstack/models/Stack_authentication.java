package com.oracle.oci.intellij.ui.appstack.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Stack_authentication extends VariableGroup {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean use_existing_token;

    private Object current_user_token;

    private boolean use_existing_vault;

    private String new_vault_display_name;

    private Object vault_compartment_id;

    private Object vault_id;

    private Object key_id;

    public boolean isUse_existing_token() {
        return use_existing_token;
    }

    public void setUse_existing_token(boolean use_existing_token) {
        this.use_existing_token = use_existing_token;
    }

    public Object getCurrent_user_token() {
        return current_user_token;
    }

    public void setCurrent_user_token(Object current_user_token) {
        this.current_user_token = current_user_token;
    }

    public boolean isUse_existing_vault() {
        return use_existing_vault;
    }

    public void setUse_existing_vault(boolean use_existing_vault) {
        this.use_existing_vault = use_existing_vault;
    }

    public String getNew_vault_display_name() {
        return new_vault_display_name;
    }

    public void setNew_vault_display_name(String new_vault_display_name) {
        this.new_vault_display_name = new_vault_display_name;
    }

    public Object getVault_compartment_id() {
        return vault_compartment_id;
    }

    public void setVault_compartment_id(Object vault_compartment_id) {
        this.vault_compartment_id = vault_compartment_id;
    }

    public Object getVault_id() {
        return vault_id;
    }

    public void setVault_id(Object vault_id) {
        this.vault_id = vault_id;
    }

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