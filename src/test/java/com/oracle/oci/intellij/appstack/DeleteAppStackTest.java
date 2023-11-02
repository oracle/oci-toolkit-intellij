package com.oracle.oci.intellij.appstack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.appStackGroup.command.DeleteStackCommand;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;

public class DeleteAppStackTest {

  private static final String compartmentId;
  
  static {
    Properties ociConfigProp = new Properties();
    try {
      ociConfigProp.load(new FileReader(new File("/Users/cbateman/.oci/config")));
      compartmentId = ociConfigProp.getProperty("tenancy");
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
    
    DeleteStackCommand deleteCommand = new DeleteStackCommand(resourceManagerClientProxy, 
                "ocid1.ormstack.oc1.iad.amaaaaaadra4vviarcdv4w73m5b6rtk3wwavcsc4w7h4rm3znysazimlcexa");
    Result result = deleteCommand.execute();
    System.out.println(result);
  }
}
