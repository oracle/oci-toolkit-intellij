package com.oracle.oci.intellij.account;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.resourcemanager.ResourceManagerClient;
import com.oracle.bmc.resourcemanager.model.ApplyJobOperationDetails;
import com.oracle.bmc.resourcemanager.model.CreateApplyJobOperationDetails;
import com.oracle.bmc.resourcemanager.model.CreateDestroyJobOperationDetails;
import com.oracle.bmc.resourcemanager.model.CreateImportTfStateJobOperationDetails;
import com.oracle.bmc.resourcemanager.model.CreateJobDetails;
import com.oracle.bmc.resourcemanager.model.CreateJobOperationDetails;
import com.oracle.bmc.resourcemanager.model.CreatePlanJobOperationDetails;
import com.oracle.bmc.resourcemanager.model.CreateStackDetails;
import com.oracle.bmc.resourcemanager.model.CreateZipUploadConfigSourceDetails;
import com.oracle.bmc.resourcemanager.model.DestroyJobOperationDetails;
import com.oracle.bmc.resourcemanager.model.StackSummary;
import com.oracle.bmc.resourcemanager.requests.CreateJobRequest;
import com.oracle.bmc.resourcemanager.requests.CreateStackRequest;
import com.oracle.bmc.resourcemanager.requests.DeleteStackRequest;
import com.oracle.bmc.resourcemanager.requests.GetJobRequest;
import com.oracle.bmc.resourcemanager.requests.ListStacksRequest;
import com.oracle.bmc.resourcemanager.responses.CreateJobResponse;
import com.oracle.bmc.resourcemanager.responses.CreateStackResponse;
import com.oracle.bmc.resourcemanager.responses.DeleteStackResponse;
import com.oracle.bmc.resourcemanager.responses.ListStacksResponse;
import com.oracle.oci.intellij.common.Utils;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;

//import net.jodah.failsafe.Failsafe;
//import net.jodah.failsafe.RetryPolicy;

public class ResManagerClient {
//	private static final String CONFIG_LOCATION = "~/.oci/config";
//	private static final String CONFIG_PROFILE = "DEFAULT";

//	static final RetryPolicy JOB_NOT_COMPLETED_RETRY_POLICY = new RetryPolicy()
//			.retryIf(new Predicate<GetJobResponse>() {
//				@Override
//				public boolean test(GetJobResponse response) {
//					return response.getJob().getLifecycleState() != LifecycleState.Failed
//							&& response.getJob().getLifecycleState() != LifecycleState.Succeeded;
//				}
//			}).withDelay(5, TimeUnit.SECONDS).withMaxDuration(24, TimeUnit.HOURS);
//
//	static final RetryPolicy STACK_LOCKED_RETRY_POLICY = new RetryPolicy().retryOn(new Predicate<Throwable>() {
//		@Override
//		public boolean test(Throwable e) {
//			if (!(e instanceof BmcException)) {
//				return false;
//			}
//			BmcException bmcException = (BmcException) e;
//			return bmcException.getStatusCode() == 409 && bmcException.getServiceCode().equals("IncorrectState");
//		}
//	}).withDelay(5, TimeUnit.SECONDS).withMaxDuration(5, TimeUnit.MINUTES);

	public static void main(String[] args) throws Exception {

		if (args.length < 1) {
			System.out.println(args.length);
			throwCmdLineUsage("This example expects at least 1 arguments.");
		}

		String commandArg = args[0];

		if (commandArg.charAt(0) != '-' || commandArg.length() < 2) {
			throwCmdLineUsage("First argument must start with a dash. i.e. '-d'");
		}

		final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parseDefault();
		final AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);
		final ResourceManagerClient resourceManagerClient = ResourceManagerClient.builder().build(provider);

		AbstractBasicCommand command = null;
		switch (commandArg.charAt(1)) {
		case 'c': {
			// create
			// : a compartment OCID and a zip file path
			String compartmentId = args[1];
			String zipFilePath = args[2];
			command = new CreateStackCommand(resourceManagerClient, compartmentId, zipFilePath);
		}
			break;
		case 'l': {
			String compartmentId = args[1];
			command = new ListStackCommand(resourceManagerClient, compartmentId);
		}
			break;
		case 'd': {
			// delete
			String stackId = args[1];
			command = new DeleteStackCommand(resourceManagerClient, stackId);
		}
			break;
		}

		if (command != null) {
			try {
				Result result = command.execute();
				if (result.isOk()) {
					System.out.println(result.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void throwCmdLineUsage(String message) throws Exception {
		throw new Exception(message);
	}

//	private static void waitForJobToComplete(final ResourceManagerClient resourceManagerClient, final String jobId) {
//		final GetJobRequest getJobRequest = GetJobRequest.builder().jobId(jobId).build();
//		Failsafe.with(JOB_NOT_COMPLETED_RETRY_POLICY).get(new Callable<GetJobResponse>() {
//			@Override
//			public GetJobResponse call() throws Exception {
//				return resourceManagerClient.getJob(getJobRequest);
//			}
//		});
//	}

	private static CreateJobResponse createImportStateJob(ResourceManagerClient resourceManagerClient, String stackId) {
		CreateJobOperationDetails operationDetails = CreateImportTfStateJobOperationDetails.builder()
				.tfStateBase64Encoded(new byte[] {}).build();
		CreateJobDetails createImportStateJobDetails = CreateJobDetails.builder().stackId(stackId)
				.jobOperationDetails(operationDetails).build();
		CreateJobRequest createImportStateJobRequest = CreateJobRequest.builder()
				.createJobDetails(createImportStateJobDetails).build();
		return resourceManagerClient.createJob(createImportStateJobRequest);
	}

	private static CreateJobResponse createPlanJob(ResourceManagerClient resourceManagerClient, String stackId) {
		CreateJobOperationDetails operationDetails = CreatePlanJobOperationDetails.builder().build();
		CreateJobDetails planJobDetails = CreateJobDetails.builder().stackId(stackId)
				.jobOperationDetails(operationDetails).build();
		CreateJobRequest jobPlanRequest = CreateJobRequest.builder().createJobDetails(planJobDetails).build();
		return resourceManagerClient.createJob(jobPlanRequest);
	}

	private static CreateJobResponse createApplyJob(ResourceManagerClient resourceManagerClient, String stackId,
			String planJobId) {
		CreateJobOperationDetails operationDetails = CreateApplyJobOperationDetails.builder()
				.executionPlanStrategy(ApplyJobOperationDetails.ExecutionPlanStrategy.FromPlanJobId)
				.executionPlanJobId(planJobId).build();
		CreateJobDetails createApplyJobDetails = CreateJobDetails.builder().stackId(stackId)
				.jobOperationDetails(operationDetails).build();
		CreateJobRequest applyJobRequest = CreateJobRequest.builder().createJobDetails(createApplyJobDetails).build();
		return resourceManagerClient.createJob(applyJobRequest);
	}

	private static CreateJobResponse createDestroyJob(ResourceManagerClient resourceManagerClient, String stackId) {
		CreateJobOperationDetails operationDetails = CreateDestroyJobOperationDetails.builder()
				.executionPlanStrategy(DestroyJobOperationDetails.ExecutionPlanStrategy.AutoApproved).build();
		CreateJobDetails createDestroyJobDetails = CreateJobDetails.builder().stackId(stackId)
				.jobOperationDetails(operationDetails).build();
		CreateJobRequest createPlanJobRequest = CreateJobRequest.builder().createJobDetails(createDestroyJobDetails)
				.build();
		return resourceManagerClient.createJob(createPlanJobRequest);
	}

	@SuppressWarnings("unused")
	private static class CreateStackCommand extends AbstractBasicCommand {
		private ResourceManagerClient resourceManagerClient;
		private String compartmentId;
		private String zipFilePath;

		public static class CreateResult extends Result {

			private boolean classSealed = false;
			private String stackId;

			public CreateResult() {
				super(Severity.NONE, Status.OK);
			}

			public static CreateResult create() {
				return new CreateResult();
			}

			public CreateResult stackId(String stackId) {
				if (!isSealed()) {
					this.stackId = stackId;
				}
				// TODO: throw?
				return this;
			}

			public boolean isSealed() {
				return classSealed;
			}

			public String getStackId() {
				return stackId;
			}

			public CreateResult build() {
				this.classSealed = true;
				return this;
			}
		}

		public CreateStackCommand(ResourceManagerClient resourceManagerClient, String compartmentId,
				String zipFilePath) {
			this.resourceManagerClient = resourceManagerClient;
			this.compartmentId = compartmentId;
			this.zipFilePath = zipFilePath;
		}

		@Override
		protected Result doExecute() throws Exception {
			try {
				CreateZipUploadConfigSourceDetails zipUploadConfigSourceDetails = CreateZipUploadConfigSourceDetails
						.builder().zipFileBase64Encoded(Utils.GetBase64EncodingForAFile(zipFilePath)).build();

				CreateStackDetails stackDetails = CreateStackDetails.builder().compartmentId(compartmentId)
						.configSource(zipUploadConfigSourceDetails).build();
				CreateStackRequest createStackRequest = CreateStackRequest.builder().createStackDetails(stackDetails)
						.build();
				CreateStackResponse createStackResponse = resourceManagerClient.createStack(createStackRequest);
				System.out.println("Created Stack : " + createStackResponse.getStack());
				final String stackId = createStackResponse.getStack().getId();

				System.out.println(stackId);

//				 // Provide initial state file 
//				CreateJobResponse importStateJobResponse = 
//					createImportStateJob(resourceManagerClient, stackId); final String
//				 importStateJobId = importStateJobResponse.getJob().getId();
//				 //waitForJobToComplete(resourceManagerClient, importStateJobId);
//
//				// Create Plan Job 
//				CreateJobResponse createPlanJobResponse =
//						createPlanJob(resourceManagerClient, stackId);
//				final String planJobId = createPlanJobResponse.getJob().getId();
//				//waitForJobToComplete(resourceManagerClient, planJobId);
//
//				// Get Job logs GetJobLogsRequest getJobLogsRequest =
//				GetJobLogsRequest getJobLogsRequest = 
//					GetJobLogsRequest.builder().jobId(planJobId).build();
//				GetJobLogsResponse getJobLogsResponse = resourceManagerClient.getJobLogs(getJobLogsRequest);
//
//				CreateJobResponse createApplyJobResponse = createApplyJob(resourceManagerClient, stackId, planJobId);
//				String applyJobId = createApplyJobResponse.getJob().getId();
//				//waitForJobToComplete(resourceManagerClient, applyJobId);
//
//				// Get Job Terraform state GetJobTfStateRequest getJobTfStateRequest =
//				GetJobTfStateRequest getJobTfStateRequest =
//					GetJobTfStateRequest.builder().jobId(applyJobId).build();
//				GetJobTfStateResponse getJobTfStateResponse = resourceManagerClient.getJobTfState(getJobTfStateRequest);
// 
				return CreateResult.create().stackId(stackId).build();
			} catch (final IOException ioe) {
				return Result.exception(ioe);
			}
		}
	}

	private static class ListStackCommand extends AbstractBasicCommand {
		private ResourceManagerClient resManagerClient;
		private String compartmentId;

		public static class ListStackResult extends Result {

			private List<StackSummary> stacks;

			public ListStackResult(Severity severity, Status status, List<StackSummary> stacks) {
				super(severity, status);
				this.stacks = stacks;
			}

			public String toString() {
				final StringBuilder builder = new StringBuilder();
				Optional.ofNullable(this.stacks).ifPresent(stacks -> {

					stacks.forEach(stack -> builder.append(stack.toString()));
				});
				return builder.toString();
			}
		}

		public ListStackCommand(ResourceManagerClient resManagerClient, String stackId) {
			super();
			this.resManagerClient = resManagerClient;
			this.compartmentId = stackId;
		}

		@Override
		protected Result doExecute() throws Exception {
			ListStacksRequest listStackRequest = ListStacksRequest.builder().compartmentId(compartmentId).build();
			ListStacksResponse listStacks = this.resManagerClient.listStacks(listStackRequest);
			List<StackSummary> items = listStacks.getItems();
			return new ListStackResult(AbstractBasicCommand.Result.Severity.NONE,
					Result.Status.OK, items);
		}

	}

	private static class DeleteStackCommand extends AbstractBasicCommand {

		private ResourceManagerClient resManagerClient;
		private String stackId;

		public DeleteStackCommand(ResourceManagerClient resourceManagerClient, String stackId) {
			super();
			this.resManagerClient = resourceManagerClient;
			this.stackId = stackId;
		}

		@Override
		protected Result doExecute() throws Exception {
			// Create Destroy Job
			final CreateJobResponse destroyJob = createDestroyJob(resManagerClient, stackId);
			final GetJobRequest getJobRequest = GetJobRequest.builder().jobId(destroyJob.getJob().getId()).build();
			resManagerClient.getJob(getJobRequest);

			// Delete Stack
			final DeleteStackRequest deleteStackRequest = DeleteStackRequest.builder().stackId(stackId).build();
			final DeleteStackResponse deleteStackResponse = resManagerClient.deleteStack(deleteStackRequest);
			System.out.println("Deleted Stack : " + deleteStackResponse.toString());
			return Result.OK_RESULT;
		}

	}

}
