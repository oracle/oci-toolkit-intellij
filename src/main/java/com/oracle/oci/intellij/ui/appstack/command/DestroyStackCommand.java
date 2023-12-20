package com.oracle.oci.intellij.ui.appstack.command;

import com.oracle.bmc.resourcemanager.responses.CreateJobResponse;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Severity;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Status;

public class DestroyStackCommand extends AbstractBasicCommand<Result> {

  private ResourceManagerClientProxy resManagerClientProxy;
  private String stackId;
  public DestroyStackCommand(ResourceManagerClientProxy resourceManagerClientProxy, String stackId) {
    super();
    this.resManagerClientProxy = resourceManagerClientProxy;
    this.stackId = stackId;
  }
  @Override
  protected Result doExecute() throws Exception {
    CreateJobResponse response = this.resManagerClientProxy.destroyStack(stackId);
    return new Result(Severity.NONE,Status.OK);
  }

}
