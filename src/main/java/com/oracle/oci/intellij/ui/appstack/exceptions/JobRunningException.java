package com.oracle.oci.intellij.ui.appstack.exceptions;

public class JobRunningException extends RuntimeException{
    static String JOB_RUNNING_EXCEPTION_MESSAGE= "Can't update the stack because a job is currently running. Wait for the job to complete and then try again :" ;

    public JobRunningException(String stackId) {
        super(JOB_RUNNING_EXCEPTION_MESSAGE + stackId);
    }


}
