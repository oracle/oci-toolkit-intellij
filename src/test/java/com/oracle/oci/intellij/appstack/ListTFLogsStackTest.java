package com.oracle.oci.intellij.appstack;

import java.io.IOException;
import java.util.List;

import com.oracle.bmc.resourcemanager.model.LogEntry;
import com.oracle.bmc.resourcemanager.model.StackSummary;
import com.oracle.bmc.resourcemanager.responses.GetJobLogsResponse;
import com.oracle.bmc.resourcemanager.responses.ListJobsResponse;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.appStackGroup.provider.AppStackContentProvider;
import com.oracle.oci.intellij.appStackGroup.provider.AppStackContentProvider.AppStackContent;
import com.oracle.oci.intellij.ui.appstack.command.ListStackCommand;
import com.oracle.oci.intellij.ui.appstack.command.ListStackCommand.ListStackResult;
import com.oracle.oci.intellij.ui.appstack.command.ListTFLogsCommand;
import com.oracle.oci.intellij.ui.appstack.command.ListTFLogsCommand.ListTFLogsResult;

public class ListTFLogsStackTest {

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
    final ResourceManagerClientProxy resourceManagerClientProxy = 
      OracleCloudAccount.getInstance().getResourceManagerClientProxy();
    String jobId = args[0];
    ListTFLogsCommand tfLogsCommand = new ListTFLogsCommand(resourceManagerClientProxy, jobId, 100);
    ListTFLogsResult result = tfLogsCommand.execute();
    dumpLogs(result);
    
    while (result.getLastResponse().getOpcNextPage()!=null) {
      tfLogsCommand = new ListTFLogsCommand(resourceManagerClientProxy, jobId, result, 1000);
      result = tfLogsCommand.execute();
      dumpLogs(result);
    }
  }

  private static void dumpLogs(ListTFLogsResult result) {
    List<LogEntry> items = result.getLastResponse().getItems();
    for (LogEntry logEntry : items) {
      System.out.println(logEntry.getMessage());
    }
  }
}
