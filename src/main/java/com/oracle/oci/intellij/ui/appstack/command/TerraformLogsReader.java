package com.oracle.oci.intellij.ui.appstack.command;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import com.oracle.bmc.resourcemanager.model.LogEntry;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.ui.appstack.command.ListTFLogsCommand.ListTFLogsResult;

public class TerraformLogsReader extends Reader {

  public enum STAT {
      NOT_OPENED, OPEN, CLOSED
  };

  private ResourceManagerClientProxy rmc;
  private String jobId;
  private int limit = 100;
  private LinkedList<LogEntry> logEntries;
  private STAT stat = STAT.NOT_OPENED;
  private ListTFLogsResult result;
  private char[] remainderChars;
  
  public TerraformLogsReader(ResourceManagerClientProxy rmc, String jobId) {
    super();
    this.rmc = rmc;
    this.jobId = jobId;
  }

  private int fetchAndQueue() throws IOException {
    try {
      switch(stat) {
      case NOT_OPENED:
      {
        ListTFLogsCommand command = new ListTFLogsCommand(rmc, jobId, limit);
        this.result = command.execute();
        List<LogEntry> items = this.result.getLastResponse().getItems();
        for (LogEntry logEntry : items)
        {
          this.logEntries.addLast(logEntry);
        }
        if (this.result.getLastResponse().getOpcNextPage() == null) {
          stat = STAT.CLOSED;
        }
        else {
          stat = STAT.OPEN;
        }
        return items.size();
      }
      case OPEN:
      {
        ListTFLogsCommand command = new ListTFLogsCommand(rmc, jobId, result, this.limit);
        result = command.execute();
        List<LogEntry> items = result.getLastResponse().getItems();
        for (LogEntry item : items) {
          logEntries.addLast(item);
        }
        if (result.getLastResponse().getOpcNextPage() == null) {
          stat = STAT.CLOSED;
        }
        return items.size();
      }}
    }
    catch (IOException e) {
      throw e;
    }
    catch (Exception e) {
      throw new IOException(e);
    }
    return -1;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if (stat == STAT.CLOSED) {
      return -1;
    }
    int remainingBytes = len;
    char[] buffer = new char[len];
    int curOffset = 0;

    // check if remainderChars here
    if (remainderChars != null && remainderChars.length > 0) {
      System.arraycopy(remainderChars, 0, buffer, 0, Math.min(len, remainderChars.length));
    }
    if (logEntries.isEmpty()) {
      int fetchAndQueue = fetchAndQueue();
      if (fetchAndQueue < 0) {
        return -1;
      }
    }

    while (remainingBytes > 0) {
      LogEntry nextEntry = logEntries.removeFirst();
      String message = nextEntry.getMessage();
      int subStrOffset = Math.min(remainingBytes, message.length());
      char[] transBuff = message.substring(0, subStrOffset).toCharArray();
      System.arraycopy(transBuff, 0, buffer, curOffset, transBuff.length);
      curOffset+=transBuff.length;
      remainingBytes -= transBuff.length;
    }
    return curOffset;
  }
  
  @Override
  public void close() throws IOException {
    //
  }


}
