package com.oracle.oci.intellij.ui.appstack.actions;

import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.OracleCloudAccount;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompartmentCache {
    Map<String, List<Compartment>> compChildren = new LinkedHashMap<>();
    private volatile boolean isCaching = true;

    public synchronized List<Compartment> getCompartmentList(Compartment parent) {
        List<Compartment> list = null;
        if (isCaching) {
            list = compChildren.get(parent.getId());
            if (list != null) return list;
        }
        list = OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(parent);
        if (isCaching) {
            compChildren.put(parent.getId(), list);
        }
        return list;
    }
}
