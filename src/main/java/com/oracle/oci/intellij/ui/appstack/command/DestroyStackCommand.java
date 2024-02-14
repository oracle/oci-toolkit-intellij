package com.oracle.oci.intellij.ui.appstack.command;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.ProjectManager;
import com.oracle.bmc.resourcemanager.responses.CreateJobResponse;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Severity;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Status;
import com.oracle.oci.intellij.ui.common.MyBackgroundTask;
import com.oracle.oci.intellij.ui.common.UIUtil;

public class DestroyStackCommand extends AbstractBasicCommand<Result> {

  private ResourceManagerClientProxy resManagerClientProxy;
  private final String SUCCESSFUL_MESSAGE = "Stack has been successfully destroyed" ;
  private String stackName ;
  private String stackId;
  public DestroyStackCommand(ResourceManagerClientProxy resourceManagerClientProxy, String stackId,String stackName) {
    super();
    this.resManagerClientProxy = resourceManagerClientProxy;
    this.stackId = stackId;
    this.stackName = stackName;
  }
  @Override
  protected Result doExecute() throws Exception {
    CreateJobResponse response = this.resManagerClientProxy.destroyStack(stackId);
    String applyJobId = response.getJob().getId();
    MyBackgroundTask.startBackgroundTask(ProjectManager.getInstance().getDefaultProject(),"Destroying Resources of \""+stackName+"\" (stack)","Destroying resources...","Destroy Job Failed please check logs of \""+stackName+"\" (stack)","Destroy job successfully applied on \""+stackName+"\" (stack)",applyJobId);

    UIUtil.fireNotification(NotificationType.INFORMATION, "Destroy Job was submitted of \""+stackName+"\" (stack)", null);
    return new Result(Severity.NONE,Status.OK);
  }

}
