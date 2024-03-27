package com.oracle.oci.intellij.ui.appstack.command;

import com.oracle.bmc.resourcemanager.responses.GetJobLogsResponse;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Severity;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Status;
import com.oracle.oci.intellij.ui.appstack.command.ListTFLogsCommand.ListTFLogsResult;

public class ListTFLogsCommand extends AbstractBasicCommand<ListTFLogsResult> {

  public static class ListTFLogsResult extends Result {

    private GetJobLogsResponse lastResponse;

    public ListTFLogsResult(Severity severity, Status status,
                            GetJobLogsResponse lastResponse) {
      super(severity, status);
      this.lastResponse = lastResponse;
    }

    public GetJobLogsResponse getLastResponse() {
      return lastResponse;
    }

  }

  private final int limit;
  private ListTFLogsResult previousResult;
  private final ResourceManagerClientProxy rmc;
  private final String jobId;

  public ListTFLogsCommand(ResourceManagerClientProxy rmc, String jobId,
                           int limit) {
    this.rmc = rmc;
    this.jobId = jobId;
    this.limit = limit;
  }

  public ListTFLogsCommand(ResourceManagerClientProxy rmc, String jobId) {
    this(rmc, jobId, 100);
  }

  public ListTFLogsCommand(ResourceManagerClientProxy rmc, String jobId,
                           ListTFLogsResult previousResult, int nextLimit) {
    this(rmc, jobId, nextLimit);
    this.previousResult = previousResult;
  }

  @Override
  protected ListTFLogsResult doExecute() throws Exception {
    // first time on this list pagination
    if (this.previousResult == null) {
      GetJobLogsResponse jobLogs = this.rmc.getJobLogs(jobId, limit);
      return new ListTFLogsResult(Severity.NONE, Status.OK, jobLogs);
    }
    // not the first pagination, so request paging based on prev
    else {
      String opcNextPage = this.previousResult.lastResponse.getOpcNextPage();
      GetJobLogsResponse jobLogs = this.rmc.getJobLogs(jobId, limit, opcNextPage);
      return new ListTFLogsResult(Severity.NONE, Status.OK, jobLogs);
    }
  }
}
