package com.oracle.oci.intellij.appstack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.oracle.bmc.resourcemanager.model.StackSummary;
import com.oracle.bmc.resourcemanager.requests.ListJobsRequest;
import com.oracle.bmc.resourcemanager.responses.ListJobsResponse;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.appstack.command.ListStackCommand;
import com.oracle.oci.intellij.ui.appstack.command.ListStackCommand.ListStackResult;
import com.oracle.oci.intellij.appStackGroup.provider.AppStackContentProvider;
import com.oracle.oci.intellij.appStackGroup.provider.AppStackContentProvider.AppStackContent;

public class ListAppStackTest {

  private static final String compartmentId;
  
  static {
    Properties ociConfigProp = new Properties();
    try {
      ociConfigProp.load(new FileReader(new File("/Users/cbateman/.oci/config")));
      compartmentId = "ocid1.compartment.oc1..aaaaaaaaj25pmwr67ktc4ykojn7rs6n2iawyapbgcu4pvutxhvusgqnrlypa";
      //ociConfigProp.getProperty("tenancy");
      SystemPreferences.changeEventEnabled.set(false);
      OracleCloudAccount.getInstance().configure("/Users/cbateman/.oci/config", "DEFAULT");
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
    elements.forEach(appstack -> 
      { System.out.printf("%s\t%s\t%s\n", appstack.getDisplayName(), appstack.getId(), appstack.getLifecycleState());
        ListJobsResponse listJobs = resourceManagerClientProxy.listJobs(appstack.getCompartmentId(), appstack.getId());
        listJobs.getItems().forEach(job -> 
            System.out.printf("\t\t%s\t%s\t%s\n", job.getId(), job.getLifecycleState(), job.getJobOperationDetails().toString()));
      });
  }
}
