package com.oracle.oci.intellij.ui.appstack.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.oracle.bmc.resourcemanager.model.JobSummary;
import com.oracle.bmc.resourcemanager.model.StackSummary;
import com.oracle.bmc.resourcemanager.responses.ListJobsResponse;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.appStackGroup.provider.AppStackContentProvider;
import com.oracle.oci.intellij.appStackGroup.provider.AppStackContentProvider.AppStackContent;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Severity;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Status;
import com.oracle.oci.intellij.ui.appstack.command.GetStackJobsCommand.GetStackJobsResult;
import com.oracle.oci.intellij.ui.appstack.command.ListStackCommand.ListStackResult;

public class GetStackJobsCommand extends AbstractBasicCommand<GetStackJobsResult> {
  public static class GetStackJobsResult extends Result {

    private final List<JobSummary> jobs;

    public GetStackJobsResult(Severity severity, Status status, List<JobSummary> jobs) {
      super(severity, status);
      this.jobs = jobs != null ? new ArrayList<>(jobs) : Collections.emptyList();
    }

    public List<JobSummary> getJobs() {
      return jobs;
    }
    
  }
  private ResourceManagerClientProxy resManagerClientProxy;
  private String stackId;
  private String compartmentId;

  public GetStackJobsCommand(ResourceManagerClientProxy resManagerClient, String compartmentId, String stackId) {
    this.resManagerClientProxy = resManagerClient;
    this.compartmentId = compartmentId;
    this.stackId = stackId;
  }

  @Override
  protected GetStackJobsResult doExecute() throws Exception {
    ListJobsResponse listJobs = resManagerClientProxy.listJobs(compartmentId, stackId);
    List<JobSummary> items = listJobs.getItems();
    return new GetStackJobsResult(Severity.NONE, Status.OK, items);
  }
}
