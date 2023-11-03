package com.oracle.oci.intellij.common.command;

public abstract class AbstractBasicCommand<RESULT extends AbstractBasicCommand.Result> implements BasicCommand<RESULT> {
	public static class CommandFailedException extends Exception {

		private static final long serialVersionUID = -827559900110505792L;

		public CommandFailedException(String message, Throwable cause) {
			super(message, cause);
		}

		public CommandFailedException(Throwable cause) {
			super(cause);
		}

    public CommandFailedException(String message) {
      super(message);
    }
	}
	
	public static class Result {
		public enum Severity {
			NONE, INFO, WARNING, ERROR;
		}

		private final Result.Severity severity;

		public enum Status {
			OK, CANCELLED, FAILED;
		}

		private final Result.Status status;
		private final String message;
		private Throwable exception;

		public static final AbstractBasicCommand.Result OK_RESULT = new Result(Severity.NONE, Status.OK);

		public Result(Result.Severity severity, Result.Status status) {
			this(severity, status, (String) null);
		}

		public boolean isOk() {
			return status == Status.OK;
		}

		public static AbstractBasicCommand.Result exception(Throwable t) {
			return new Result(Severity.ERROR, Status.FAILED, t);
		}

		public Result(Result.Severity severity, Result.Status status, String message) {
			this.severity = severity;
			this.status = status;
			this.message = message;
		}

		public Result(Result.Severity error, Result.Status failed, Throwable t) {
			this(error, failed, t.getMessage());
			this.exception = t;
		}

		public Result.Severity getSeverity() {
			return severity;
		}

		public Result.Status getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}

		@SuppressWarnings("unused")
		public Throwable getException() {
			return exception;
		}

		public String toString() {
			return String.format("Result: %s[%s] %s", getStatus(), getSeverity(),
					getMessage() == null ? "" : getMessage());
		}
	}

	public final RESULT execute() throws Exception
	{
		try {
		  if (!canExecute()) {
		    throw new CommandFailedException("");
		  }
			RESULT r =  doExecute();
			hasExecuted = true;
			return r;
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	private boolean hasExecuted;
	
	protected abstract RESULT doExecute() throws Exception;

	public boolean hasExecuted( ) {
		return this.hasExecuted;
	}

  public boolean canUndo() {
    return hasExecuted && hasUndo();
  }

  public boolean hasUndo() {
    return true;
  }
	
	@Override
  public boolean canExecute() {
    return hasUndo() &&!hasExecuted();
  }

  public void undo() {
		// do nothing
		if (!canUndo()) {
			throw new IllegalStateException();
		}
	}
}