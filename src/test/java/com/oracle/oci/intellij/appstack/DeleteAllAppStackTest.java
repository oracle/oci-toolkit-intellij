package com.oracle.oci.intellij.appstack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.oracle.bmc.resourcemanager.model.StackSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.appStackGroup.command.DeleteStackCommand;
import com.oracle.oci.intellij.appStackGroup.command.ListStackCommand;
import com.oracle.oci.intellij.appStackGroup.command.ListStackCommand.ListStackResult;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;

public class DeleteAllAppStackTest {

  private static final String compartmentId = "ocid1.compartment.oc1..aaaaaaaaj25pmwr67ktc4ykojn7rs6n2iawyapbgcu4pvutxhvusgqnrlypa";
  
  static {
    Properties ociConfigProp = new Properties();
    try {
      ociConfigProp.load(new FileReader(new File("/Users/cbateman/.oci/config")));
      //compartmentId = ociConfigProp.getProperty("tenancy");
      SystemPreferences.changeEventEnabled.set(false);
      OracleCloudAccount.getInstance().configure("/Users/cbateman/.oci/config", "DEFAULT");
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    
  }
  
  public static void main(String args[]) throws Exception {
//    final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parseDefault();
//    final AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);
//    final ResourceManagerClient resourceManagerClient = ResourceManagerClient.builder().build(provider);
    ResourceManagerClientProxy resourceManagerClientProxy = 
      OracleCloudAccount.getInstance().getResourceManagerClientProxy();
    
    ListStackCommand allStacks = new ListStackCommand(resourceManagerClientProxy, compartmentId);
    ListStackResult listResult = allStacks.execute();
    for (StackSummary summary : listResult.getStacks()) {
      String id = summary.getId();
      DeleteStackCommand deleteCommand = new DeleteStackCommand(resourceManagerClientProxy, id);
      Result execute = deleteCommand.execute();
      System.out.printf("Removed stack %s, %s\n", id, execute.toString());
    }
    
  }
}
