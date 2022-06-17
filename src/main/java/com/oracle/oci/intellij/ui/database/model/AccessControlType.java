package com.oracle.oci.intellij.ui.database.model;

import static com.oracle.oci.intellij.ui.database.model.CIDRBlockType.CIDR_BLOCK_PATTERN;

import java.util.regex.Matcher;

public abstract class AccessControlType extends EventSource {
    public enum Category {
        IP_BASED, VCN_BASED;
    }
    public enum Types {
        IP("IP Address"), CIDR("CIDR Block"),  VCN_BY_OCID("VCN By OCID"), Unknown("Unknown ACL Type"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        private String label;

        private Types(String label) {
            this.label = label;
        }
        public String getLabel()
        {
            return this.label;
        }
    }
    
    private final Category category;
    private final Types type;

    public static AccessControlType parseAcl(String aclStr) {
        if (aclStr.startsWith("ocid")) //$NON-NLS-1$
        {
            return OcidBasedAccessControlType.parseOcidAcl(aclStr);
        }
        Matcher matcher = IPAddressType.IP_ADDR_PATTERN.matcher(aclStr);
        if (matcher.matches()) {
            return new IPAddressType(aclStr);
        }
        matcher = CIDR_BLOCK_PATTERN.matcher(aclStr);
        if (matcher.matches()) {
            return new CIDRBlockType(aclStr);
        }
        return new UnknownAccessControlType(aclStr);
    }

    protected AccessControlType(Category category, Types type)
    {
        this.category = category;
        this.type = type;
    }

    public Category getCategory() {
        return this.category;
    }
    
    public Types getType() {
        return this.type;
    }

    public final void setValue(String value) {
        String oldValue = this.getValue();
        doSetValue(value);
        String newValue = getValue();
        if (!oldValue.equals(newValue)) {
            this.pcs.firePropertyChange("value", oldValue, newValue); //$NON-NLS-1$
        }
    }

    public abstract String isValueValid();

    public abstract void doSetValue(String value);

    public abstract String getValue();

    public abstract String getTypeLabel();
}
