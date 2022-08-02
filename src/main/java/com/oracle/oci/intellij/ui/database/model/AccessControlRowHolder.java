package com.oracle.oci.intellij.ui.database.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.oracle.oci.intellij.ui.database.model.AccessControlType.Category;

public class AccessControlRowHolder extends EventSource implements PropertyChangeListener {
    private AccessControlType aclType;
    private boolean isFullyLoaded;
    private boolean isNew;

    public AccessControlRowHolder(AccessControlType type, boolean isFullyLoaded) {
        this.aclType = type;
        this.aclType.addPropertyChangeListener(this);
        this.isFullyLoaded = isFullyLoaded;
    }

    public void setAclType(AccessControlType newType) {

        AccessControlType oldAclType = this.aclType;
        oldAclType.removePropertyChangeListener(this);
        this.aclType = newType;
        this.aclType.addPropertyChangeListener(this);
        this.pcs.firePropertyChange("aclType", oldAclType, this.aclType); //$NON-NLS-1$
    }

    public AccessControlType getAclType() {
        return this.aclType;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("value".equals(evt.getPropertyName())) { //$NON-NLS-1$
            this.pcs.firePropertyChange("value", evt.getOldValue(), evt.getNewValue()); //$NON-NLS-1$
        }
    }

    public boolean isFullyLoaded() {
        return this.isFullyLoaded;
    }

    public void setFullyLoaded(boolean isFullyLoaded) {
        boolean oldValue = this.isFullyLoaded;
        this.isFullyLoaded = isFullyLoaded;
        if (oldValue != this.isFullyLoaded) {
            this.pcs.firePropertyChange("fullyLoaded", oldValue, this.isFullyLoaded); //$NON-NLS-1$
        }
    }

    public boolean isNew() {
        return this.isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public static PropertyListeningArrayList<AccessControlRowHolder> parseAclsFromText(List<String> whiteListedIps,
            Category category) {
        PropertyListeningArrayList<AccessControlRowHolder> acls = new PropertyListeningArrayList<>();

        for (final String whitelisted : whiteListedIps) {
            AccessControlType acl = AccessControlType.parseAcl(whitelisted);
            if (acl.getCategory() == category) {
                // IP Based is always loaded; vcn needs to wait for thec actual network
                // metadata.
                acls.add(new AccessControlRowHolder(acl, acl.getCategory() == Category.IP_BASED));
            }
        }

        return acls;
    }
}