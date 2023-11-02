package com.oracle.oci.intellij.account;

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
import com.oracle.bmc.resourcemanager.model.DestroyJobOperationDetails;
import com.oracle.bmc.resourcemanager.requests.CreateJobRequest;
import com.oracle.bmc.resourcemanager.requests.DeleteStackRequest;
import com.oracle.bmc.resourcemanager.requests.GetJobRequest;
import com.oracle.bmc.resourcemanager.responses.CreateJobResponse;
import com.oracle.bmc.resourcemanager.responses.DeleteStackResponse;
import com.oracle.oci.intellij.ui.appstack.command.CreateStackCommand;
import com.oracle.oci.intellij.ui.appstack.command.ListStackCommand;
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
//			String compartmentId = args[1];
//			String zipFilePath = args[2];
//			command = new CreateStackCommand(resourceManagerClient, compartmentId, zipFilePath);
		}
			break;
		case 'l': {
			String compartmentId = args[1];
//			command = new ListStackCommand(resourceManagerClient, compartmentId);
		}
			break;
		case 'd': {
			// delete
//			String stackId = args[1];
//			command = new DeleteStackCommand(resourceManagerClient, stackId);
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



}
