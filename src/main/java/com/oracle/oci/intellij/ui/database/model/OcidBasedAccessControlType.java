package com.oracle.oci.intellij.ui.database.model;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.oracle.bmc.OCID;
import com.oracle.bmc.core.model.Vcn;

public class OcidBasedAccessControlType extends VCNBasedAccessControlType {

    @SuppressWarnings("unused")
    private String value;
    private String ocid;
    private List<String> ipList;
    private Vcn vcn;

    public OcidBasedAccessControlType(String ocid, List<String> ipList) {
        super(AccessControlType.Types.VCN_BY_OCID);
        this.ocid = ocid;
        this.ipList = ipList;
    }

    @Override
    public String isValueValid() {
        if (OCID.isValid(getOcid()))
        {
            return null;
        }
        return "OCID must be of the form: : ocid1.<RESOURCE TYPE>.<REALM>.[REGION].<UNIQUE ID>"; 
    }

    public String isOptionalIPPartValid() {
        if (this.ipList != null && !this.ipList.isEmpty()) {
            for (String val : this.ipList) {
                AccessControlType parseAcl = AccessControlType.parseAcl(val);
                if (parseAcl.getType() != Types.Unknown) {
                    continue;
                }
                return "Must be semi-colon delimited list of IP or CIDR block notations";
            }
        }
        return null;
    }

    @Override
    public void doSetValue(String value) {
        this.value = value;
        OcidBasedAccessControlType newType = parseOcidAcl(value);
        if (newType != null)
        {
            this.ocid = newType.ocid;
            this.ipList = newType.ipList;
        }
    }

    @Override
    public String getValue() {
        StringBuilder builder = new StringBuilder(this.ocid);
        if (!this.ipList.isEmpty())
        {
            builder.append(";"); //$NON-NLS-1$
            buildIPListIfPresent(builder);
        }
        return builder.toString();
    }

    public void buildIPListIfPresent(StringBuilder builder) {
        if (!this.ipList.isEmpty())
        {
            for (String ip : this.ipList)
            {
                builder.append(ip);
                builder.append(";"); //$NON-NLS-1$
            }
            // remove the last trailing comma
            builder.deleteCharAt(builder.length()-1);
        }
    }

    public String getOcid() {
        return this.ocid;
    }

    public Vcn getVcn() {
        return this.vcn;
    }

    public void setVcn(Vcn vcn) {
        Vcn oldVcn = this.vcn;
        String oldOid = this.ocid;
        this.vcn = vcn;
        if (vcn != null)
        {
            this.ocid = vcn.getId();
        }
        else
        {
            this.ocid = null;
        }
        //this.ipList = Collections.emptyList();
        if (oldVcn != this.vcn)
        {
            this.pcs.firePropertyChange(new PropertyChangeEvent(this, "vcn", oldVcn, this.vcn)); //$NON-NLS-1$
        }
        this.pcs.firePropertyChange(new PropertyChangeEvent(this, "ocid", oldOid, this.ocid)); //$NON-NLS-1$
    }

    public List<String> getIPList() {
        if (this.ipList != null && !this.ipList.isEmpty())
        {
            return Collections.unmodifiableList(this.ipList);
        }
        return Collections.emptyList();
    }

    public void setIPList(List<String> newIPList) {
        if (this.ipList == null || this.ipList.isEmpty())
        {
            this.ipList = new ArrayList<>();
        }
        else
        {
            this.ipList.clear();
        }
        this.ipList.addAll(newIPList);
    }
    
    public void setIPList(String asString) {
        String[] splitBySemi = asString.split(";"); //$NON-NLS-1$
        List<String> list = new ArrayList<>();
        for (String str : splitBySemi) {
            if (str != null && !str.trim().isEmpty()) {
                list.add(str);
            }
        }
        this.ipList = list;
    }

    public String getIPListAsString()
    {
        if (this.ipList != null && !this.ipList.isEmpty())
        {
            return StringUtils.join(this.ipList.toArray(new String[0]), ';');
        }
        return ""; //$NON-NLS-1$
    }
    public static OcidBasedAccessControlType parseOcidAcl(String aclStr) {
        if (aclStr.startsWith("ocid")) //$NON-NLS-1$
        {
            String ocid = aclStr;
            List<String> ipList = new ArrayList<>();
            int firstSemi = aclStr.indexOf(';');
            if (firstSemi > -1)
            {
                ocid = aclStr.substring(0, firstSemi);
                String ipListStr = aclStr.substring(firstSemi+1);
                String[] splitOnSemi = ipListStr.split(";"); //$NON-NLS-1$
                for (String ip : splitOnSemi)
                {
                    ipList.add(ip);
                }
            }
            return new OcidBasedAccessControlType(ocid, ipList);
        }
        return null;
    }

    public void setOcid(String newOcid) {
        String oldOcid = newOcid;
        this.ocid = newOcid;
        if ((this.ocid != null && !this.ocid.equals(oldOcid)
              || oldOcid != null && !oldOcid.equals(this.ocid)))
        {
            this.pcs.firePropertyChange(new PropertyChangeEvent(this, "ocid", oldOcid, this.ocid)); //$NON-NLS-1$
        }
    }

}
