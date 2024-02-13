package com.oracle.oci.intellij.ui.devops.actions;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.Icons;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.account.OracleCloudAccount.ResourceManagerClientProxy;
import com.oracle.oci.intellij.ui.appstack.AppStackDashboard;
import com.oracle.oci.intellij.ui.appstack.YamlLoader;
import com.oracle.oci.intellij.ui.appstack.command.CreateStackCommand;

public class LaunchProjectToAppStack extends AnAction {

  public LaunchProjectToAppStack() {
      super("Launch As Java App Stack...");
  }

  @Override
  public void actionPerformed(com.intellij.openapi.actionSystem.@NotNull AnActionEvent e) {
    AtomicReference<Map<String, String>> variables = new AtomicReference<>(new LinkedHashMap<>());

    Runnable runnable = () -> {
      YamlLoader loader = new YamlLoader();

      try {
        variables.set(loader.load());
      } catch (IntrospectionException | IllegalAccessException | InvocationTargetException ex) {
        throw new RuntimeException(ex);
      }
      if (variables.get() == null)
        return;
      try {
        ResourceManagerClientProxy proxy = OracleCloudAccount.getInstance().getResourceManagerClientProxy();
        String compartmentId = SystemPreferences.getCompartmentId();
        ClassLoader cl = AppStackDashboard.class.getClassLoader();
        CreateStackCommand command =
                new CreateStackCommand(proxy, compartmentId, cl, "appstackforjava.zip",loader.isApply());
//        Map<String,String> variables = new ModelLoader().loadTestVariables();
//        variables.put("shape","CI.Standard.E3.Flex");
        command.setVariables(variables.get());
//        command.setVariables(variables.get());
        //TODO:
        command.execute();
        //this.dashboard.commandStack.execute(command);
      } catch (Exception e1) {
        throw new RuntimeException(e1);
      }
    };
    ApplicationManager.getApplication().invokeLater(runnable);

  }


  
//  @Override
//  protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
//      VirtualFile virtualFile = Lookups.getVirtualFile(e);
//      if (isAvailableFor(virtualFile)) {
//          ScriptExecutionManager scriptExecutionManager = ScriptExecutionManager.getInstance(project);
//          scriptExecutionManager.executeScript(virtualFile);
//      }
//  }
//
//  private boolean isAvailableFor(VirtualFile virtualFile) {
//      return virtualFile != null && (
//              virtualFile.getFileType() == SQLFileType.INSTANCE ||
//              virtualFile.getFileType() == PSQLFileType.INSTANCE);
//  }
//
//  @Override
//  protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
//      Presentation presentation = e.getPresentation();
//      VirtualFile virtualFile = Lookups.getVirtualFile(e);
//      presentation.setVisible(isAvailableFor(virtualFile));
//      presentation.setIcon(Icons.EXECUTE_SQL_SCRIPT);
//      presentation.setText("Execute SQL Script");
//  }{

}
