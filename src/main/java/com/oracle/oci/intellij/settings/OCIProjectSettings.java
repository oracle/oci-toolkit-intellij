package com.oracle.oci.intellij.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;

@Service(Service.Level.PROJECT)
@State(name = "OCIProjectSettings",
       storages = @Storage(value = "oracleocitoolkit.xml"))
public final class OCIProjectSettings
  implements PersistentStateComponent<OCIProjectSettings.State> {

  private State state = new State();
  private @NotNull Project project;

  public static class State {

    private String tenancyId;
    private String compartmentId;
    private String compartmentName;
    
    private String devOpsProjectId;
    private String devOpsProjectName;

    public State() {

    }

    public String getTenancyId() {
      return tenancyId;
    }

    public void setTenancyId(String tenancyId) {
      this.tenancyId = tenancyId;
    }

    public String getCompartmentId() {
      return compartmentId;
    }

    public void setCompartmentId(String compartmentId) {
      this.compartmentId = compartmentId;
    }

    public String getDevOpsProjectId() {
      return devOpsProjectId;
    }

    public void setDevOpsProjectId(String devOpsProjectId) {
      this.devOpsProjectId = devOpsProjectId;
    }

    public String getCompartmentName() {
      return compartmentName;
    }

    public void setCompartmentName(String compartmentName) {
      this.compartmentName = compartmentName;
    }

    public String getDevOpsProjectName() {
      return devOpsProjectName;
    }

    public void setDevOpsProjectName(String devOpsProjectName) {
      this.devOpsProjectName = devOpsProjectName;
    }
  }

  public OCIProjectSettings(@NotNull Project project) {
    this.project = project;
  }

  public static OCIProjectSettings getInstance(@NotNull Project project) {
    return project.getService(OCIProjectSettings.class);
  }

  @Override
  public @Nullable State getState() {
    return state;
  }


  @Override
  public void loadState(@NotNull State state) {
    this.state = state;
  }
}
