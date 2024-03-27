package com.oracle.oci.intellij.common.command;

import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Severity;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;
import com.oracle.oci.intellij.common.command.CompositeCommand.*;

public class CompositeCommand
  extends AbstractBasicCommand<CompositeCommandResult> {

  public static class CompositeCommandResult extends Result {

    private List<Result> results;

    public CompositeCommandResult(List<Result> results) {
      super();
      this.results = results;
    }

    @Override
    public Severity getSeverity() {
      Severity mostSevere = Severity.ERROR;
      for (Result r : this.results) {
        if (r.getSeverity().compareTo(mostSevere) > 0) {
          mostSevere = r.getSeverity();
        }
      }
      return mostSevere;
    }

    @Override
    public Status getStatus() {
      Status mostSevere = Status.OK;
      for (Result r : this.results) {
        if (r.getStatus().compareTo(mostSevere) > 0) {
          mostSevere = r.getStatus();
        }
      }
      return mostSevere;
    }

    @Override
    public String getMessage() {
      StringBuilder builder = new StringBuilder();
      for (Result r : this.results) {
       builder.append(r.getMessage());
       builder.append("\n");
      }
      
      return builder.toString();
    }

    @Override
    public Throwable getException() {
      Throwable compThrowable = new Throwable();
      compThrowable.addSuppressed(compThrowable);
      return compThrowable;
    }
  }

  private List<BasicCommand<?>> commands;
  private List<Result> results;
  
  public CompositeCommand(List<BasicCommand<?>> commands) {
    this.commands = commands;
  }

  public CompositeCommand(BasicCommand<?>...commands) {
    this.commands = Arrays.asList(commands);
  }

  @Override
  protected CompositeCommandResult doExecute() throws Exception {
    this.results = new ArrayList<>(this.commands.size());
    for (BasicCommand<?> command : this.commands) {
     this.results.add(command.execute()); 
    }
    return new CompositeCommandResult(this.results);
  }
  
  
  
}
