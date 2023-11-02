package com.oracle.oci.intellij.ui.appstack.command;

import java.beans.PropertyDescriptor;

import com.oracle.oci.intellij.ui.appstack.command.SetCommand.SetCommandResult;
import com.oracle.oci.intellij.common.Utils;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Severity;
import com.oracle.oci.intellij.common.command.AbstractBasicCommand.Result.Status;

public class SetCommand<ModelType, ObjType> extends AbstractBasicCommand<SetCommandResult<ModelType, ObjType>> {
	public static class SetCommandResult<ModelType, ObjType> extends Result {
		private ModelType modelType;
		private ObjType oldValue;
		private ObjType newValue;

		public SetCommandResult(Severity severity, Status status, String message) {
			super(severity, status, message);
		}

		public SetCommandResult(Severity error, Status failed, Throwable t) {
			super(error, failed, t);
		}

		public SetCommandResult(Severity severity, Status status) {
			super(severity, status);
		}

		public ModelType getModelType() {
			return modelType;
		}

		public void setModelType(ModelType modelType) {
			this.modelType = modelType;
		}

		public ObjType getObjType() {
			return oldValue;
		}

		public void setOldValue(ObjType objType) {
			this.oldValue = objType;
		}

		public ObjType getOldValue() {
			return oldValue;
		}

		public void setNewValue(ObjType newValue) {
			this.newValue = newValue;
		}

		public ObjType getNewValue() {
			return newValue;
		}
	}

	private ModelType modelObj;
	private PropertyDescriptor pd;
	private ObjType newValue;
	private ObjType oldValue;

	public SetCommand(ModelType modelObj, PropertyDescriptor pd, ObjType newValue) {
		this.modelObj = modelObj;
		this.pd = pd;
		this.newValue = newValue;
	}

	@Override
	protected SetCommand.SetCommandResult doExecute() throws Exception {
		Object propValue =  Utils.setPropertyValue(modelObj, pd, newValue);
		this.oldValue = (ObjType) propValue;
		SetCommand.SetCommandResult<ModelType, ObjType> result = new SetCommandResult(Severity.NONE, Status.OK);
		result.setModelType(modelObj);
		result.setOldValue(oldValue);
		result.setNewValue(newValue);
		return result;
	}
}