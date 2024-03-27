package com.oracle.oci.intellij.ui.devops.actions;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.bmc.devops.model.ProjectSummary;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.DevOpsClientProxy;
import com.oracle.oci.intellij.settings.OCIProjectSettings;
import com.oracle.oci.intellij.settings.OCIProjectSettings.State;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.common.UIUtil.ModelHolder;
import com.oracle.oci.intellij.util.LogHandler;

public class AssociateDevOpsProjectAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    @Nullable
    Project project = event.getProject();
    @Nullable
    State state = OCIProjectSettings.getInstance(project).getState();
    try {
      SelectProjectDialog dialog = new SelectProjectDialog(true, state);
      boolean wasOk = dialog.showAndGet();
      if (wasOk) {
        state.setDevOpsCompartmentId(dialog.getCompartmentId());
        state.setDevOpsProjectId(dialog.getDevOpsProjectId());

        state.setDevOpsTenancyId(OracleCloudAccount.getInstance().getCurrentTenancy());
        project.save();
      }
      dialog.disposeIfNeeded();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage());
      e.printStackTrace();
    }
  }


  public static class SelectProjectDialog extends DialogWrapper {
    private State state;
    private JComboBox<ModelHolder<ProjectSummary>> projectCombo;
    private List<ProjectSummary> listDevOpsProjects;
    private JTextField compartmentText;
    private JButton compartmentButton;
    protected Optional<Compartment> selectedCompartment = Optional.empty();
    protected ProjectSummary selectedProjectSummary;

    protected SelectProjectDialog(boolean canBeParent, State state) {
      super(canBeParent);
      this.state = state;
      setTitle("DevOps Project Association");
      setOKButtonText("OK");
      init();
    }


    @Override
    public void show() {
      selectedCompartment.ifPresent(s -> populateComboList(s.getId(), ModalityState.any()));
      super.show();
    }


    public String getCompartmentName() {
      return selectedCompartment.orElseThrow().getName();
    }

    public String getCompartmentId() {
      return selectedCompartment.orElseThrow().getId();
    }

    public String getDevOpsProjectName() {
      Optional<ProjectSummary> comboItem =
        ModelHolder.getComboItem(projectCombo);
      return comboItem.orElseThrow().getName();
    }

    public String getDevOpsProjectId() {
      Optional<ProjectSummary> comboItem =
        ModelHolder.getComboItem(projectCombo);
      return comboItem.orElseThrow().getId();
    }

    public void setTenancyId(String currentTenancy) {
      
    }

    private void populateComboList(final String compartmentId, ModalityState modalityState) {
      UIUtil.showInfoInStatusBar("Refreshing DevOps Projects.");

      final Runnable fetchData = () -> {
        try {
          DevOpsClientProxy devOpsClient =
            OracleCloudAccount.getInstance().getDevOpsClient();
          this.listDevOpsProjects =
            devOpsClient.listDevOpsProjects(compartmentId);
        } catch (Exception exception) {
          listDevOpsProjects = null;
          UIUtil.fireNotification(NotificationType.ERROR, exception.getMessage(),
                                  null);
          LogHandler.error(exception.getMessage(), exception);
        }
      };

    final Runnable updateUI = () -> {
      if (this.listDevOpsProjects != null) {
        UIUtil.showInfoInStatusBar((listDevOpsProjects.size())
                                   + " DevOps Projects found.");
        projectCombo.removeAllItems();

        for (ProjectSummary s : this.listDevOpsProjects) {
          projectCombo.addItem(UIUtil.holdModel(s).setTextProvider(summary -> {
            return summary.getName();
          }));
        }
      }
      };

      UIUtil.executeAndUpdateUIAsync(fetchData, updateUI, modalityState);
    }

    @SuppressWarnings("serial")
    @Override
    protected @Nullable JComponent createCenterPanel() {
      JPanel centerPanel = new JPanel();
      centerPanel.setLayout(new GridLayout(0, 2));

      JLabel compartmentLabel = new JLabel("Compartment");
      centerPanel.add(compartmentLabel);

      JPanel compartmentPanel = new JPanel();
      compartmentPanel.setLayout(new BoxLayout(compartmentPanel,
                                               BoxLayout.LINE_AXIS));
      centerPanel.add(compartmentPanel);

      this.compartmentText = new JTextField(16);
      compartmentText.setEnabled(false);
      compartmentPanel.add(compartmentText);
      this.compartmentButton = new JButton(new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          CompartmentSelection cselect = CompartmentSelection.newInstance();
          boolean showAndGet = cselect.showAndGet();
          if (showAndGet) {
            selectedCompartment = Optional.ofNullable(cselect.getSelectedCompartment());
            selectedCompartment.ifPresent(s -> {
              compartmentText.setText(s.getName());
              populateComboList(s.getId(), ModalityState.defaultModalityState());
            });
          }
        }
      });
      compartmentButton.setText("...");
      compartmentPanel.add(compartmentButton);

      JLabel projectLabel = new JLabel("DevOps Project");
      projectCombo = new JComboBox<>();

      centerPanel.add(projectLabel);
      centerPanel.add(projectCombo);

      initFromState(this.state);

      return centerPanel;
    }

    private void initFromState(State state) {
      if (state.getDevOpsCompartmentId() != null) {
        Compartment compartment = 
          OracleCloudAccount.getInstance().getIdentityClient().getCompartment(state.getDevOpsCompartmentId());
          this.selectedCompartment = Optional.ofNullable(compartment);
          selectedCompartment.ifPresent(s -> compartmentText.setText(s.getName()));
      }
    }
  }
}
