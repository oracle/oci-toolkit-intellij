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

    private String devOpsTenancyId;
    private String devOpsCompartmentId;
    private String devOpsCompartmentName;
    
    private String devOpsProjectId;
    private String devOpsProjectName;

    public State() {

    }


    public String getDevOpsTenancyId() {
      return devOpsTenancyId;
    }


    public void setDevOpsTenancyId(String devOpsTenancyId) {
      this.devOpsTenancyId = devOpsTenancyId;
    }


    public String getDevOpsCompartmentId() {
      return devOpsCompartmentId;
    }


    public void setDevOpsCompartmentId(String devOpsCompartmentId) {
      this.devOpsCompartmentId = devOpsCompartmentId;
    }


    public String getDevOpsCompartmentName() {
      return devOpsCompartmentName;
    }


    public void setDevOpsCompartmentName(String devOpsCompartmentName) {
      this.devOpsCompartmentName = devOpsCompartmentName;
    }


    public String getDevOpsProjectId() {
      return devOpsProjectId;
    }


    public void setDevOpsProjectId(String devOpsProjectId) {
      this.devOpsProjectId = devOpsProjectId;
    }


    public String getDevOpsProjectName() {
      return devOpsProjectName;
    }


    public void setDevOpsProjectName(String devOpsProjectName) {
      this.devOpsProjectName = devOpsProjectName;
    }


    public boolean isDevOpsAssociated() {
      return this.devOpsProjectId != null;
    }
    public void clearDevOpsAssociation() {
      this.devOpsProjectId = null;
      this.devOpsProjectName = null;
      this.devOpsCompartmentId = null;
      this.devOpsCompartmentName = null;
      this.devOpsTenancyId = null;
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
