package com.oracle.oci.intellij.ui.appstack.command;

import com.intellij.notification.NotificationType;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;
import com.oracle.oci.intellij.ui.common.UIUtil;

public class DeleteStackCommand extends AbstractBasicCommand<Result> {

  private ResourceManagerClientProxy resManagerClientProxy;
  private String stackId;
  private String stackName ;
  private final String SUCCESSFUL_MESSAGE = "Stack has been successfully deleted" ;

  public DeleteStackCommand(ResourceManagerClientProxy resourceManagerClientProxy, String stackId,String stackName) {
    super();
    this.stackName = stackName;
    this.resManagerClientProxy = resourceManagerClientProxy;
    this.stackId = stackId;
  }

  @Override
  protected Result doExecute() throws Exception {
    resManagerClientProxy.deleteStack(this.stackId);
    UIUtil.fireNotification(NotificationType.INFORMATION, SUCCESSFUL_MESSAGE+" : \""+stackName+"\" (stack)", null);
    return Result.OK_RESULT;
  }
}
