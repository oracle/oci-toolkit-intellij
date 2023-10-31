package com.oracle.oci.intellij.common.command;

public interface BasicCommand {
	public AbstractBasicCommand.Result execute() throws Exception;
	
	public boolean hasExecuted( );
	public boolean canUndo(); 
	
	public void undo(); 
}
