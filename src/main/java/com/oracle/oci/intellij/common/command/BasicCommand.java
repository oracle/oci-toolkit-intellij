package com.oracle.oci.intellij.common.command;

public interface BasicCommand<RESULT extends AbstractBasicCommand.Result> {
	public RESULT execute() throws Exception;
	
	public boolean canExecute();
	public boolean hasExecuted( );
	public boolean canUndo(); 
	public boolean hasUndo();
	
	public void undo(); 
}
