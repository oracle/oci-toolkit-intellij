package com.oracle.oci.intellij.ui.appstack.command;

import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;

public class DeleteStackCommand extends AbstractBasicCommand<Result> {

  private ResourceManagerClientProxy resManagerClientProxy;
  private String stackId;

  public DeleteStackCommand(ResourceManagerClientProxy resourceManagerClientProxy, String stackId) {
    super();
    this.resManagerClientProxy = resourceManagerClientProxy;
    this.stackId = stackId;
  }

  @Override
  protected Result doExecute() throws Exception {
    // Create Destroy Job
//    final CreateJobResponse destroyJob = createDestroyJob(resManagerClientProxy, stackId);
//    final GetJobRequest getJobRequest = GetJobRequest.builder().jobId(destroyJob.getJob().getId()).build();
//    resManagerClientProxy.submitJob(getJobRequest);
    resManagerClientProxy.deleteStack(this.stackId);
    return Result.OK_RESULT;
  }

//  private static CreateJobResponse createDestroyJob(ResourceManagerClientProxy resourceManagerClient, String stackId) {
//    CreateJobOperationDetails operationDetails = CreateDestroyJobOperationDetails.builder()
//        .executionPlanStrategy(DestroyJobOperationDetails.ExecutionPlanStrategy.AutoApproved).build();
//    CreateJobDetails createDestroyJobDetails = CreateJobDetails.builder().stackId(stackId)
//        .jobOperationDetails(operationDetails).build();
//    CreateJobRequest createPlanJobRequest = CreateJobRequest.builder().createJobDetails(createDestroyJobDetails)
//        .build();
//    resourceManagerClient.submitJob(createPlanJobRequest); 
//  }
}
