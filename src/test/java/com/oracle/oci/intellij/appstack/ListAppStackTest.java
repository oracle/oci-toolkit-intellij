package com.oracle.oci.intellij.appstack;

import java.io.IOException;
import java.util.List;

import com.oracle.bmc.resourcemanager.model.StackSummary;
import com.oracle.bmc.resourcemanager.responses.ListJobsResponse;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.appStackGroup.provider.AppStackContentProvider;
import com.oracle.oci.intellij.appStackGroup.provider.AppStackContentProvider.AppStackContent;
import com.oracle.oci.intellij.ui.appstack.command.ListStackCommand;
import com.oracle.oci.intellij.ui.appstack.command.ListStackCommand.ListStackResult;

public class ListAppStackTest {

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

    ListStackCommand listCommand = new ListStackCommand(resourceManagerClientProxy, compartmentId);
    ListStackResult result = listCommand.execute();
    List<StackSummary> stacks = result.getStacks();
    AppStackContentProvider provider = new AppStackContentProvider();
    List<AppStackContent> elements = provider.getElements(stacks);
    if (!elements.isEmpty()) {
      elements.forEach(appstack -> 
        { System.out.printf("%s\t%s\t%s\n", appstack.getDisplayName(), appstack.getId(), appstack.getLifecycleState());
          ListJobsResponse listJobs = resourceManagerClientProxy.listJobs(appstack.getCompartmentId(), appstack.getId());
          listJobs.getItems().forEach(job -> 
              System.out.printf("\t\t%s\t%s\t%s\n", job.getId(), job.getLifecycleState(), job.getJobOperationDetails().toString()));
        });
    }
    else {
      System.out.println("No stacks found.");
    }
  }
}
