package com.oracle.oci.intellij.ui.appstack.command;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.oracle.bmc.resourcemanager.model.JobOutputSummary;
import com.oracle.bmc.resourcemanager.model.JobOutputsCollection;
import com.oracle.bmc.resourcemanager.responses.ListJobOutputsResponse;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.common.Utils;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.*;

public class ListJobOutputCommand extends AbstractBasicCommand<com.oracle.oci.intellij.ui.appstack.command.ListJobOutputCommand.ListJobOutputResult> {
  public static class ListJobOutputResult extends Result {
    List<JobOutputSummary>  outputSummaries;

    public List<JobOutputSummary> getOutputSummaries() {
      return Collections.unmodifiableList(outputSummaries);
    }
    
  }
  private ResourceManagerClientProxy resourceManagerClient;
  private String compartmentId;
  private String jobId;

  public ListJobOutputCommand(ResourceManagerClientProxy resourceManagerClient, String compartmentId,
                            String jobId) throws IOException {
    this.resourceManagerClient = resourceManagerClient;
    this.compartmentId = compartmentId;
    this.jobId = jobId;
   }

  @Override
  protected ListJobOutputResult doExecute() throws Exception {
    ListJobOutputsResponse jobOutputsResponse = resourceManagerClient.listJobOutputs(this.jobId);
    JobOutputsCollection jobOutputsCollection = jobOutputsResponse.getJobOutputsCollection();
    ListJobOutputResult listJobOutputResult = new ListJobOutputResult();
    if (jobOutputsCollection != null) {
      List<JobOutputSummary> items = jobOutputsCollection.getItems();
      listJobOutputResult.outputSummaries = items == null ? Collections.emptyList() : items;
    }
    return listJobOutputResult;
  }
}
