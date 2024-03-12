package com.oracle.oci.intellij.ui.appstack.command;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.oracle.bmc.resourcemanager.model.LogEntry;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.ui.appstack.command.ListTFLogsCommand.ListTFLogsResult;

public class TerraformLogger {

  public enum STAT {
    NOT_OPENED, OPEN, CLOSED
};
  private Writer writer;
  private Thread thread;
  private AtomicBoolean running = new AtomicBoolean();
  private AtomicBoolean closed = new AtomicBoolean();
  
  private ResourceManagerClientProxy rmc;
  private String jobId;
  private int limit = 350;
  private LinkedList<LogEntry> logEntries = new LinkedList<LogEntry>();
  private STAT stat = STAT.NOT_OPENED;
  private ListTFLogsResult result;
  
  public TerraformLogger(Writer writer, ResourceManagerClientProxy rmc, String jobId) {
    this.writer = writer;
    this.rmc = rmc;
    this.jobId = jobId;
  }

  public void start() {
    if (this.thread == null) {
      this.thread = new Thread(() -> runPollerLoop());
      this.running.set(true);
      this.thread.start();
    }
  }
  
  public void close() {
    if (closed.compareAndSet(false, true)) {
      this.running.set(false);
      this.thread.interrupt();
      this.thread = null;
    }
  }

  private void runPollerLoop() {
    while (this.running.get() == true) {
      int numFetched;
      try {
        numFetched = fetchAndQueue();
        if (numFetched > 0) {
          // drain logEntries
          while (!this.logEntries.isEmpty()) {
            LogEntry removeFirst = this.logEntries.removeFirst();
            this.writer.write(removeFirst.getMessage());
            this.writer.write("\n");
          }
          this.writer.flush();
        }
      } catch (IOException e) {
        e.printStackTrace();
        this.running.set(false);
        return; // terminate thread, don't try to recover at all.
      }
    }
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
      }
      case CLOSED: 
        // TODO:
      }
    }
    catch (IOException e) {
      throw e;
    }
    catch (Exception e) {
      throw new IOException(e);
    }
    return -1;
  }
}
