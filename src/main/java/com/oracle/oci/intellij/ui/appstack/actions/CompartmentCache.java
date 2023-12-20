package com.oracle.oci.intellij.ui.appstack.actions;

import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.OracleCloudAccount;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompartmentCache {
//    Map<String, List<Compartment>> compChildren = new LinkedHashMap<>();
   volatile Map<String, List<Compartment>> compChildren = new ConcurrentHashMap<>();
    private  boolean isCaching ;
    private static CompartmentCache INSTANCE;

    public static CompartmentCache getInstance() {
        if (INSTANCE == null){
            INSTANCE = new CompartmentCache();
        }
        return INSTANCE;
    }

    public void setCaching(boolean caching) {
        isCaching = caching;
    }

    public     List<Compartment> getCompartmentList(Compartment parent) {
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

    public    void clearCache() {
        compChildren.clear();
    }

}
