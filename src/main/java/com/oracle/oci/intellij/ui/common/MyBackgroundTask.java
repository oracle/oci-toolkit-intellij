package com.oracle.oci.intellij.ui.common;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.oracle.bmc.model.BmcException;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.common.command.BasicCommand;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class MyBackgroundTask {
    private final Project project;
    private static final Function<String,Boolean> isJobFinished=(jobId)->{
        try {
            while (true) {
                String status = getJobStatus(jobId);
                if ("SUCCEEDED".equals(status)) {
                    return true;
                } else if ("FAILED".equals(status)) {
                    return false;
                }

                // Wait a bit before checking again
                Thread.sleep(5000); // Sleep for 5 seconds
            }
        } catch (BmcException e){
            UIUtil.fireNotification(NotificationType.ERROR,e.getMessage(),null);
        }catch (InterruptedException e) {
            // Handle exceptions
            throw new RuntimeException();
        }

        return null;
    };



    private static String getJobStatus(String jobId) {
         OracleCloudAccount.ResourceManagerClientProxy resourceManagerClient = OracleCloudAccount.getInstance().getResourceManagerClientProxy();
        return resourceManagerClient.getJobDetails(jobId).getLifecycleState().getValue();
    }

    public MyBackgroundTask(Project project) {
        this.project = project;
    }

    public static void startBackgroundTask(Project project, String title, String processingMessage, String failedMessage , String succeededMessage ,String jobId, BasicCommand<?> runLater) {
        Task.Backgroundable task = new Task.Backgroundable(project, title, false) {

            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                // Set the progress indicator's initial text
                progressIndicator.setText(processingMessage+"...");
                progressIndicator.setIndeterminate(true);


                if (progressIndicator.isCanceled()) {
                    return;
                }

                if (isJobFinished.apply(jobId)){
                    progressIndicator.setText(succeededMessage);
                    UIUtil.fireNotification(NotificationType.INFORMATION, succeededMessage, null);
                }else {
                    progressIndicator.setText(failedMessage);
                    UIUtil.fireNotification(NotificationType.ERROR, failedMessage, null);
                }
            }

            @Override
            public void onFinished() {
                if (runLater != null){
                    try {
                        runLater.execute();
                    } catch (Exception e) {
                        String errorMessage = e.getMessage()==null?"Something went wrong ":e.getMessage();
                        UIUtil.fireNotification(NotificationType.ERROR, errorMessage, null);
                    }
                }

            }
        };

        // Run the task with a progress indicator
        ProgressManager.getInstance().run(task);
    }
    public static void startBackgroundTask(Project project, String title, String processingMessage, String failedMessage , String succeededMessage , String jobId) {
        startBackgroundTask(project,title,processingMessage,failedMessage,succeededMessage,jobId,null);
    }



    }