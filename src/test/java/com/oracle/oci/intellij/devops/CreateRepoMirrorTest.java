package com.oracle.oci.intellij.devops;

import java.io.IOException;

import com.oracle.bmc.devops.responses.CreateConnectionResponse;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.DevOpsClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;

public class CreateRepoMirrorTest {
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
    final DevOpsClientProxy devOpsClientProxy = OracleCloudAccount.getInstance().getDevOpsClient();
    final String projectId = "ocid1.devopsproject.oc1.phx.amaaaaaadxiv6saadyecvba5jkqkbb2tvsxnc2i3g4xagtlqxqefbaxrgmxa";
    final String accessToken = "ocid1.vaultsecret.oc1.phx.amaaaaaadxiv6saaxngaorxmh4vekouby5cjx3iry4xv6i4rp6m7n24rzjca";//args[0];
    CreateConnectionResponse conn = devOpsClientProxy.createGithubRepositoryConnection(projectId, accessToken);
    System.out.println(conn.getConnection());
//    MirrorRepositoryResponse mirrorRepository = devOpsClientProxy.mirrorRepository(projectId);
//    System.out.println(mirrorRepository);
//    List<ProjectSummary> listDevOpsProjects = devOpsClientProxy.listDevOpsProjects(compartmentId);
//    System.out.println(listDevOpsProjects);
//    ListStackCommand listCommand = new ListStackCommand(resourceManagerClientProxy, compartmentId);
//    ListStackResult result = listCommand.execute();
//    List<StackSummary> stacks = result.getStacks();
//    AppStackContentProvider provider = new AppStackContentProvider();
//    List<AppStackContent> elements = provider.getElements(stacks);
//    if (!elements.isEmpty()) {
//      elements.forEach(appstack -> 
//        { System.out.printf("%s\t%s\t%s\n", appstack.getDisplayName(), appstack.getId(), appstack.getLifecycleState());
//          ListJobsResponse listJobs = resourceManagerClientProxy.listJobs(appstack.getCompartmentId(), appstack.getId());
//          listJobs.getItems().forEach(job -> 
//              System.out.printf("\t\t%s\t%s\t%s\n", job.getId(), job.getLifecycleState(), job.getJobOperationDetails().toString()));
//        });
//    }
//    else {
//      System.out.println("No stacks found.");
//    }
  }
}
