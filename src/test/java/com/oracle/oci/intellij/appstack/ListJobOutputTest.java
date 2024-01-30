package com.oracle.oci.intellij.appstack;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.oracle.bmc.resourcemanager.model.JobOutputSummary;
import com.oracle.bmc.resourcemanager.model.LogEntry;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.appstack.command.ListJobOutputCommand;
import com.oracle.oci.intellij.ui.appstack.command.ListJobOutputCommand.ListJobOutputResult;
import com.oracle.oci.intellij.ui.appstack.command.ListTFLogsCommand.ListTFLogsResult;

public class ListJobOutputTest {

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
    if (args.length < 1) {
      throw new RuntimeException("must pass the jobId");
    }
    final ResourceManagerClientProxy resourceManagerClientProxy = 
      OracleCloudAccount.getInstance().getResourceManagerClientProxy();
    String jobId = args[0];
    ListJobOutputCommand cmd = new ListJobOutputCommand(resourceManagerClientProxy, null, jobId);
    ListJobOutputResult result = cmd.execute();
    List<JobOutputSummary> outputSummaries = result.getOutputSummaries();
    Optional<JobOutputSummary> jos = outputSummaries.stream().filter(p -> "app_url".equals(p.getOutputName())).findFirst();
    jos.ifPresent(j -> System.out.println(j.getOutputValue()));
  }
}
