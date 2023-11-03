package com.oracle.oci.intellij.common.command;

import java.util.Stack;

import com.oracle.oci.intellij.common.command.AbstractBasicCommand.CommandFailedException;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;

public class CommandStack {
	Stack<BasicCommand<?>>  stack = new Stack<>();
	Stack<BasicCommand<?>> undoStack = new Stack<>();
	public Result execute(BasicCommand<?> command) throws CommandFailedException {
	  try {
	    Result r = command.execute();
	    this.stack.push(command);
	    return r;
	  } catch (CommandFailedException e) {
	    throw e;
	  }  catch (Exception e) {
	    throw new CommandFailedException(e);
    }
	}
	
	public boolean canUndo() {
		return !stack.isEmpty() & stack.peek().canUndo();
	}
	
	public BasicCommand<?> undo() {
		BasicCommand<?> command = null;
		while (!this.undoStack.isEmpty() && !(command = this.undoStack.pop()).hasUndo()) {
		  continue;
		}
		if (command != null && command.canUndo()) {
		  command.undo();
		}
		return command;
	}
}
