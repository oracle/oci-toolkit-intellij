package com.oracle.oci.intellij.ui.appstack.command;

import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;

public class CreateResult extends Result {

	private boolean classSealed = false;
	private String stackId;

	public CreateResult() {
		super(com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Severity.NONE, 
		      com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Status.OK);
	}

	public static CreateResult create() {
		return new CreateResult();
	}

	public CreateResult stackId(String stackId) {
		if (!isSealed()) {
			this.stackId = stackId;
		}
		// TODO: throw?
		return this;
	}

	public boolean isSealed() {
		return classSealed;
	}

	public String getStackId() {
		return stackId;
	}

	public CreateResult build() {
		this.classSealed = true;
		return this;
	}
}