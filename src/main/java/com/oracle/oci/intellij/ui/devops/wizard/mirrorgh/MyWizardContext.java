package com.oracle.oci.intellij.ui.devops.wizard.mirrorgh;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.project.Project;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.oci.intellij.ui.common.WizardContext;

public class MyWizardContext extends WizardContext {
  private final Project project;
  private Optional<VaultSummary> vaultSummary = Optional.empty();
  private Optional<SecretSummary> secretSummary = Optional.empty();
  private String github;
  private String repoName;
  private String repoDescription;

  public MyWizardContext(@Nullable Project project) {
    this.project = project;
  }

  public String getRepoDescription() {
    return this.repoDescription;
  }

  public void setRepoDescription(String repoDescription) {
    this.repoDescription = repoDescription;
  }

  public String getRepoName() {
    return this.repoName;
  }

  public void setRepoName(String repoName) {
    this.repoName = repoName;
  }

  public Project getProject() {
    return project;
  }

  public Optional<VaultSummary> getVaultSummary() {
    return vaultSummary;
  }

  public void setVaultSummary(Optional<VaultSummary> vaultSummary) {
    Optional<VaultSummary> oldValue = this.vaultSummary;
    this.vaultSummary = vaultSummary;
    pcs.firePropertyChange("vaultSummary", oldValue, this.vaultSummary);
  }

  public Optional<SecretSummary> getSecretSummary() {
    return secretSummary;
  }

  public void setSecretSummary(Optional<SecretSummary> secretSummary) {
    Optional<SecretSummary> oldValue = this.secretSummary;
    this.secretSummary = secretSummary;
    pcs.firePropertyChange("secretSummary", oldValue, this.secretSummary);
  }

  public String getGithub() {
    return this.github;
  }

  public void setGithub(String github) {
    String oldValue = this.github;
    this.github = github;
    pcs.firePropertyChange("github", oldValue, this.github);
  }

}