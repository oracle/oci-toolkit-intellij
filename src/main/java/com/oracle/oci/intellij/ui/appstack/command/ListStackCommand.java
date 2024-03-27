package com.oracle.oci.intellij.ui.appstack.command;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.oracle.bmc.resourcemanager.model.StackSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.ui.appstack.command.ListStackCommand.ListStackResult;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand;

public class ListStackCommand extends AbstractBasicCommand<ListStackResult> {
	private ResourceManagerClientProxy resManagerClient;
	private String compartmentId;

	public static class ListStackResult extends AbstractBasicCommand.Result {

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

    public List<StackSummary> getStacks() {
      return Collections.unmodifiableList(stacks);
    }
		
	}

	public ListStackCommand(ResourceManagerClientProxy resManagerClient, String compartmentId) {
		super();
		this.resManagerClient = resManagerClient;
		this.compartmentId = compartmentId;
	}

	@SuppressWarnings("unchecked")
  @Override
	protected ListStackResult doExecute() throws Exception {
	  try {
  	  List<StackSummary> listOfAppStacks = resManagerClient.listStacks(compartmentId);
  		return new ListStackResult(AbstractBasicCommand.Result.Severity.NONE,
  				Result.Status.OK, listOfAppStacks);
	  }
	  catch (com.oracle.bmc.model.BmcException bmcExcep) {
	    if (bmcExcep.getStatusCode() == 404) {
	      // not found or empty; pretend emtpy
	      return new ListStackResult(AbstractBasicCommand.Result.Severity.WARNING,
	           Result.Status.FAILED, Collections.EMPTY_LIST);
	    }
	    throw bmcExcep;
	  }
	}

}