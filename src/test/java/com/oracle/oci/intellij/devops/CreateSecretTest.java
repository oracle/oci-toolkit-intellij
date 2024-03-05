package com.oracle.oci.intellij.devops;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import com.oracle.bmc.keymanagement.model.KeySummary;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.bmc.vault.model.Secret;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.KmsManagementClientProxy;
import com.oracle.oci.intellij.account.OracleCloudAccount.VaultClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;

public class CreateSecretTest {
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
    final String rootCompartmentId = 
      "ocid1.tenancy.oc1..aaaaaaaagojvam7c7hthdm7h2pgshjmiqntcvei4skgysz3galuejn3rioia";
    final String keyId = 
      "ocid1.key.oc1.phx.bvqyw534aadfa.abyhqljrxjzhkald3vzmhngqvuqf62rroyjzzeq4wspgoql23ztq2es3gvba";
    final String vaultId = 
      "ocid1.vault.oc1.phx.bvqyw534aadfa.abyhqljrsmw7ssbfzocpnekb4ykcui443z5jbdzpkequddq6fwl7pomdaiqa";
    Optional<VaultSummary> findFirst = 
      ListKeysTests.getSummary(rootCompartmentId, vaultId);

    try (KmsManagementClientProxy kmsMgmtClient =
      OracleCloudAccount.getInstance().getKmsManagementClient(findFirst.get())) {
      Optional<KeySummary> key = kmsMgmtClient.getKey(rootCompartmentId, keyId);
      System.out.println(key.orElseThrow());
      
      VaultClientProxy vaultsClient = OracleCloudAccount.getInstance().getVaultsClient();
      
      String encodeToString = Base64.getEncoder().encodeToString("ghp_token1234567".getBytes());
      Secret createSecret = vaultsClient.createSecret(findFirst.get(), keyId, 
                                ("secretNameFoo_"+System.currentTimeMillis()), 
                                encodeToString);
      System.out.println(createSecret);
    }

  }
}
