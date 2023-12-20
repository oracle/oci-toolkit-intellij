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
    resManagerClientProxy.deleteStack(this.stackId);
    return Result.OK_RESULT;
  }
}
