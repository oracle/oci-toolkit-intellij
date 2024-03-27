package com.oracle.oci.intellij.appstack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.oracle.bmc.database.model.AutonomousDatabase;
import com.oracle.bmc.database.model.AutonomousDatabase.LifecycleState;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
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
        CreateAppStackTest.class.getClassLoader(), "com/oracle/oci/intellij/appstack/appstackforjava.zip",true);
    Map<String,String> variables = new ModelLoader().loadTestVariables();
    System.out.println("Validating (could take a while....");
    validate(variables, compartmentId, OracleCloudAccount.getInstance());
    createCommand.setVariables(variables);
    Result result = createCommand.execute();
    System.out.println(result);
  }

  private static Result validate(Map<String, String> variables,
                                 String compartmentId2,
                                 OracleCloudAccount instance) {
    String dbId = variables.get("autonomous_database");
    System.out.printf("Checking db exists and is running %s\n", dbId);
    AutonomousDatabase dbSummary = 
      instance.getDatabaseClient().getAutonomousDatabaseSummary(compartmentId2, dbId);
    if (dbSummary == null) {
      throw new RuntimeException(String.format("No database for ocid: %s", dbId));
    }
    if (dbSummary.getLifecycleState() != LifecycleState.Available) {
      throw new RuntimeException(
        String.format("Database %s is in state %s not Available", dbId, dbSummary.getLifecycleState()));
    }
    return Result.OK_RESULT;
  }
}
