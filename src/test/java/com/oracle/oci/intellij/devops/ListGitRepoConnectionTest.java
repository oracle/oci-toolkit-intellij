package com.oracle.oci.intellij.devops;

import java.io.IOException;
import java.util.List;

import com.oracle.bmc.devops.model.ConnectionSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.DevOpsClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;

public class ListGitRepoConnectionTest {
  private static final String compartmentId;
  
  static {
    try {
      SystemPreferences.changeEventEnabled.set(false);
     
      String CONFIG_PROFILE = "ONPREMJAVA";
      String COMPARTMENT_ID_ALWAYS_FREE = "ocid1.compartment.oc1..aaaaaaaaj25pmwr67ktc4ykojn7rs6n2iawyapbgcu4pvutxhvusgqnrlypa";
      String COMPARTMENT_ID_JAVAONPREM = "ocid1.compartment.oc1..aaaaaaaa6dtnmzrd3utboa4oli67kme2b52llhkexkuoawk3zkr7345wyisq";
      
      String compartment = null;
      switch(CONFIG_PROFILE) {
      case "ONPREMJAVA":
        compartment = COMPARTMENT_ID_JAVAONPREM;
        break;
      case "ALWAYSFREE":
      case "DEFAULT":
        compartment = COMPARTMENT_ID_ALWAYS_FREE;
        break;
      }
      
      compartmentId = compartment;
      OracleCloudAccount.getInstance().configure("/Users/cbateman/.oci/config", CONFIG_PROFILE);

    } catch (IOException e) {
      throw new AssertionError(e);
    }
    
  }
  
  public static void main(String args[]) throws Exception {
    final DevOpsClientProxy devOpsClientProxy = 
      OracleCloudAccount.getInstance().getDevOpsClient();
    final String projectId = "ocid1.devopsproject.oc1.phx.amaaaaaadxiv6saadyecvba5jkqkbb2tvsxnc2i3g4xagtlqxqefbaxrgmxa";

    List<ConnectionSummary> listRepos = devOpsClientProxy.listGithubRepositoryConnection(projectId);
    listRepos.forEach(l -> System.out.printf("%s\n", l.getDisplayName()));

  }
}
