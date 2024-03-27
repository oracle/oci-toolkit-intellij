package com.oracle.oci.intellij.ui.devops.wizard.mirrorgh;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.oracle.oci.intellij.util.OptionalUtil.ExtendedOptional;

public class SummaryStep extends WizardStep<MirrorGitHubWizardModel> {

  private JPanel mainPanel;
  private JTextArea summaryTextArea;
  private MyWizardContext context;

  public SummaryStep(MyWizardContext context) {
    super("Summary of Mirror Creation", "Here's the information we will use to create your Github mirror in OCI");
    this.context = context;
  }

  @Override
  public JComponent prepare(WizardNavigationState state) {
    this.mainPanel = new JPanel();
    BoxLayout boxLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
    mainPanel.setLayout(boxLayout);

    this.summaryTextArea = new JTextArea();
    this.summaryTextArea.setEditable(false);
    this.context.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        summaryTextArea.setText(createSummaryString());
      }
    });
    this.mainPanel.add(summaryTextArea);
    this.summaryTextArea.setText(createSummaryString());

    return mainPanel;
  }

  private String createSummaryString() {
    return String.format("DevOps Project: %s\nGithub Repo: %s\nSecret Name: %s\nRepo Name: %s\nRepo Description: %s\n",
                          context.getProject().getName(),
                          context.getGithub(),
                          ExtendedOptional.of(
                            context.getSecretSummary()).ifPresentElseDefault((s) -> s.getSecretName(), "**MISSING**"),
                          context.getRepoName(),
                          context.getRepoDescription());
  }

}