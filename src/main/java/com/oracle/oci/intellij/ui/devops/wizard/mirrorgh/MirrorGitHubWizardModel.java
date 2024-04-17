package com.oracle.oci.intellij.ui.devops.wizard.mirrorgh;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.util.NlsContexts.DialogTitle;
import com.intellij.ui.wizard.WizardModel;

public class MirrorGitHubWizardModel extends WizardModel {
  private MyWizardContext context;

  public MirrorGitHubWizardModel(@DialogTitle String title,
                                 @NotNull MyWizardContext context) {
    super(title);
    this.context = context;

    add(new SelectRemoteGithubWizardStep(this.context));
    add(new PickVaultWizardStep(this.context));
    add(new PickSecretWizardStep(this.context));
    add(new ConfigureRepositoryStep(this.context));
    add(new SummaryStep(this.context));
  }
  
  
}