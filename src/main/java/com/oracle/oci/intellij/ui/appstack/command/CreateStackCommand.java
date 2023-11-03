package com.oracle.oci.intellij.ui.appstack.command;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.oracle.bmc.resourcemanager.model.ApplyJobOperationDetails.ExecutionPlanStrategy;
import com.oracle.bmc.resourcemanager.model.CreateApplyJobOperationDetails;
import com.oracle.bmc.resourcemanager.model.CreateImportTfStateJobOperationDetails;
import com.oracle.bmc.resourcemanager.model.CreateJobDetails;
import com.oracle.bmc.resourcemanager.model.CreateJobOperationDetails;
import com.oracle.bmc.resourcemanager.model.CreatePlanJobOperationDetails;
import com.oracle.bmc.resourcemanager.model.Job.Operation;
import com.oracle.bmc.resourcemanager.requests.CreateJobRequest;
import com.oracle.bmc.resourcemanager.responses.CreateJobResponse;
import com.oracle.bmc.resourcemanager.responses.CreateStackResponse;
import com.oracle.bmc.resourcemanager.responses.GetJobTfStateResponse;
import com.oracle.bmc.resourcemanager.responses.ListJobsResponse;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.common.Utils;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand;

public class CreateStackCommand extends AbstractBasicCommand<CreateResult> {
		private ResourceManagerClientProxy resourceManagerClient;
		private String compartmentId;
		@SuppressWarnings("unused")
    private String zipFileAsString;
    private HashMap<String, String> variables;

		public CreateStackCommand(ResourceManagerClientProxy resourceManagerClient, String compartmentId,
				String zipFilePath) throws IOException {
			this.resourceManagerClient = resourceManagerClient;
			this.compartmentId = compartmentId;
			this.zipFileAsString = Utils.GetBase64EncodingForAFile(zipFilePath);
		}

    public CreateStackCommand(ResourceManagerClientProxy resourceManagerClient,
                              String compartmentId, ClassLoader classLoader,
                              String zipFilePath) throws IOException {
      this.resourceManagerClient = resourceManagerClient;
      this.compartmentId = compartmentId;
      this.zipFileAsString = Utils.GetBased64EncodingForAFile(classLoader, zipFilePath);
    }

    public void setVariables(Map<String, String> variables) {
      this.variables = new HashMap<String, String>(variables);
    }
    public Map<String, String> getVariables() {
      return Collections.unmodifiableMap(this.variables);
    }
		@Override
		protected CreateResult doExecute() throws Exception {
//			CreateZipUploadConfigSourceDetails zipUploadConfigSourceDetails = CreateZipUploadConfigSourceDetails
//      		.builder().zipFileBase64Encoded(zipFileAsString).build();

			CreateStackResponse createStackResponse = resourceManagerClient.createStack(this.variables);
      System.out.println("Created Stack : " + createStackResponse.getStack());
      final String stackId = createStackResponse.getStack().getId();

      System.out.println(stackId);

		 // Provide initial state file 
			CreateJobResponse importStateJobResponse = 
			  createImportStateJob(resourceManagerClient, stackId); 
			final String importStateJobId = importStateJobResponse.getJob().getId();
			System.out.println("Import State Job Id: "+importStateJobId);

			ListJobsResponse listJobs = resourceManagerClient.listJobs(compartmentId, stackId);
			System.out.println(listJobs.getItems());
 		 //waitForJobToComplete(resourceManagerClient, importStateJobId);

			// Create Plan Job 
//			CreateJobResponse createPlanJobResponse =
//			  createPlanJob(resourceManagerClient, stackId);
//			final String planJobId = createPlanJobResponse.getJob().getId();
//			System.out.println(planJobId);
			//waitForJobToComplete(resourceManagerClient, planJobId);

			// Get Job logs GetJobLogsRequest getJobLogsRequest =
//			GetJobLogsResponse getJobLogsResponse = resourceManagerClient.getJobLogs(planJobId);
//			System.out.println(getJobLogsResponse.getItems());
			
			CreateJobResponse createApplyJobResponse = createApplyJob(resourceManagerClient, stackId);
			String applyJobId = createApplyJobResponse.getJob().getId();
//				//waitForJobToComplete(resourceManagerClient, applyJobId);
			System.out.println(applyJobId);

			// Get Job Terraform state GetJobTfStateRequest getJobTfStateRequest =
			GetJobTfStateResponse jobTfState = resourceManagerClient.getJobTfState(applyJobId);
			System.out.println(jobTfState.toString());

      return CreateResult.create().stackId(stackId).build();
		}
		
	  private static CreateJobResponse createImportStateJob(ResourceManagerClientProxy resourceManagerClient, String stackId) {
	    CreateJobOperationDetails operationDetails = CreateImportTfStateJobOperationDetails.builder()
	        .tfStateBase64Encoded(new byte[] {}).build();
	    CreateJobDetails createImportStateJobDetails = CreateJobDetails.builder().stackId(stackId)
	        .jobOperationDetails(operationDetails).build();
	    CreateJobRequest createImportStateJobRequest = CreateJobRequest.builder()
	        .createJobDetails(createImportStateJobDetails).build();
	    return resourceManagerClient.submitJob(createImportStateJobRequest);
	  }
	  
	  private static CreateJobResponse createPlanJob(ResourceManagerClientProxy resourceManagerClient, String stackId) {
	    CreateJobOperationDetails operationDetails = CreatePlanJobOperationDetails.builder().build();
	    CreateJobDetails planJobDetails = CreateJobDetails.builder().stackId(stackId)
	        .jobOperationDetails(operationDetails).build();
	    CreateJobRequest jobPlanRequest = CreateJobRequest.builder().createJobDetails(planJobDetails).build();
	    return resourceManagerClient.submitJob(jobPlanRequest);
	  }

    private static CreateJobResponse createApplyJob(ResourceManagerClientProxy resourceManagerClient,
                                                    String stackId) {
      CreateJobDetails createJobDetails = CreateJobDetails.builder()
        .stackId(stackId)
        .displayName("app-stack-test-apply-job-" + UUID.randomUUID().toString())
        .operation(Operation.Apply)
        .jobOperationDetails(CreateApplyJobOperationDetails.builder()
            .executionPlanStrategy(ExecutionPlanStrategy.AutoApproved)
            .build())
        .build();

    CreateJobRequest createJobRequest = CreateJobRequest.builder()
        .createJobDetails(createJobDetails)
        .opcRequestId("app-stack-test-apply-job-request-" + UUID.randomUUID()
            .toString())
        .opcRetryToken("app-stack-test-apply-job-retry-" + UUID.randomUUID().toString())
        .build();

    /* Send request to the Client */
    return resourceManagerClient.submitJob(createJobRequest);
//      CreateJobOperationDetails operationDetails =
//        CreateApplyJobOperationDetails.builder()
//                                      .executionPlanStrategy(ApplyJobOperationDetails.ExecutionPlanStrategy.FromPlanJobId)
//                                      .executionPlanJobId(planJobId)
//                                      .build();
//      CreateJobDetails createApplyJobDetails =
//        CreateJobDetails.builder()
//                        .applyJobPlanResolution(ApplyJobPlanResolution.builder()
//                                                  .isAutoApproved(Boolean.TRUE).isUseLatestJobId(Boolean.TRUE).planJobId(planJobId)
//                                                  .build())
//                        .stackId(stackId)
//                        .jobOperationDetails(operationDetails)
//                        .build();
//      CreateJobRequest applyJobRequest =
//        CreateJobRequest.builder()
//                        .createJobDetails(createApplyJobDetails)
//                        .build();
//      return resourceManagerClient.submitJob(applyJobRequest);
    }

    @Override
    public boolean canExecute() {
      return true;
    }

	}