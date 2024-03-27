package com.oracle.oci.intellij.devops;

import java.io.IOException;
import java.util.List;

import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.KmsVaultClientProxy;
import com.oracle.oci.intellij.account.OracleCloudAccount.VaultClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;

public class ListKmsVaultsTests {
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
    final KmsVaultClientProxy kmsClient = 
      OracleCloudAccount.getInstance().getKmsVaultClient();
    //final String projectId = "ocid1.devopsproject.oc1.phx.amaaaaaadxiv6saadyecvba5jkqkbb2tvsxnc2i3g4xagtlqxqefbaxrgmxa";
    final String rootCompartmentId = "ocid1.tenancy.oc1..aaaaaaaagojvam7c7hthdm7h2pgshjmiqntcvei4skgysz3galuejn3rioia";
    
    List<VaultSummary> listRepos = kmsClient.listVaults(rootCompartmentId);
    System.out.println(listRepos);

    VaultClientProxy vaultClient = OracleCloudAccount.getInstance().getVaultsClient();
    listRepos.forEach(v -> { 
      List<SecretSummary> listSecrets = vaultClient.listSecrets(v.getCompartmentId(), v.getId()); 
      listSecrets.forEach(s -> System.out.println(s));
      System.out.println();
      listSecrets.stream().filter((s) -> "camgithubtoken3".equals(s.getSecretName())).forEach(s -> System.out.println(s.getId()));
    });
    
  }
}
