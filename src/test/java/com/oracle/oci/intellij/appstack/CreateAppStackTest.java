package com.oracle.oci.intellij.appstack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.appstack.command.CreateStackCommand;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;

public class CreateAppStackTest {

  private static String compartmentId;

  static {
    try {
      SystemPreferences.changeEventEnabled.set(false);
     
      String CONFIG_PROFILE = "ONPREMJAVA";
      String COMPARTMENT_ID_ALWAYS_FREE = "ocid1.compartment.oc1..aaaaaaaaj25pmwr67ktc4ykojn7rs6n2iawyapbgcu4pvutxhvusgqnrlypa";
      String COMPARTMENT_ID_JAVAONPREM = "ocid1.compartment.oc1..aaaaaaaa6dtnmzrd3utboa4oli67kme2b52llhkexkuoawk3zkr7345wyisq";
      
      OracleCloudAccount.getInstance().configure("/Users/cbateman/.oci/config", CONFIG_PROFILE);
      compartmentId = COMPARTMENT_ID_JAVAONPREM;

    } catch (IOException e) {
      throw new AssertionError(e);
    }
    
  }

  public static void main(String args[]) throws Exception {
    // final ConfigFileReader.ConfigFile configFile =
    // ConfigFileReader.parseDefault();
    // final AuthenticationDetailsProvider provider = new
    // ConfigFileAuthenticationDetailsProvider(configFile);
    // final ResourceManagerClient resourceManagerClient =
    // ResourceManagerClient.builder().build(provider);
    ResourceManagerClientProxy resourceManagerClientProxy =
      OracleCloudAccount.getInstance().getResourceManagerClientProxy();

    CreateStackCommand createCommand =
      new CreateStackCommand(resourceManagerClientProxy, compartmentId,
        CreateAppStackTest.class.getClassLoader(), "com/oracle/oci/intellij/appstack/appstackforjava.zip");
    Map<String,String> variables = new ModelLoader().loadTestVariables();
    createCommand.setVariables(variables);
    Result result = createCommand.execute();
    System.out.println(result);
  }
}
