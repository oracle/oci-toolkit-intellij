package com.oracle.oci.intellij.common.command;

import java.util.Stack;

public class CommandStack {
	Stack<BasicCommand>  stack = new Stack<>();
	
	public void execute(BasicCommand command) {
		this.stack.push(command);
	}
	
	public boolean canUndo() {
		return stack.peek().canUndo();
	}
	
	public BasicCommand undo() {
		BasicCommand command = this.stack.pop();
		command.undo();
		return command;
	}
}
