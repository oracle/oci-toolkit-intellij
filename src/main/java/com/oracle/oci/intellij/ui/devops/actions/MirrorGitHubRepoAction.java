package com.oracle.oci.intellij.ui.devops.actions;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.wizard.WizardDialog;
import com.oracle.bmc.devops.responses.CreateConnectionResponse;
import com.oracle.bmc.devops.responses.MirrorRepositoryResponse;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.DevOpsClientProxy;
import com.oracle.oci.intellij.settings.OCIProjectSettings;
import com.oracle.oci.intellij.settings.OCIProjectSettings.State;
import com.oracle.oci.intellij.ui.devops.wizard.mirrorgh.MirrorGitHubWizardModel;
import com.oracle.oci.intellij.ui.devops.wizard.mirrorgh.MyWizardContext;
import com.oracle.oci.intellij.ui.git.config.GitConfig;
import com.oracle.oci.intellij.ui.git.config.GitConfigManager;

public class MirrorGitHubRepoAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    System.out.println("actionPerformed");
    @Nullable
    Project project = e.getProject();
    GitConfigManager.getInstance().getConfig(project, (k) -> {
      final GitConfig gitConfig = k;
      assert gitConfig != null;
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          Set<String> githubUrls = findGitHubUrls(gitConfig);
          @Nullable
          State state = OCIProjectSettings.getInstance(project).getState();
          if (githubUrls.isEmpty()) {
            if (state != null) {

            }
            // JOptionPane.showMessageDialog(null, "Must have a github remote",
            // "Must have github remote",
            // JOptionPane.INFORMATION_MESSAGE);
          } else {
            MyWizardContext context = new MyWizardContext(project);
            MirrorGitHubWizardModel model =
              new MirrorGitHubWizardModel("Mirror Repository Wizard", context);
            WizardDialog<MirrorGitHubWizardModel> wizardDialog =
              new WizardDialog<MirrorGitHubWizardModel>(true,
                                                                               model);
            wizardDialog.show();
            if (wizardDialog.isOK()) {
              createConnection(state.getDevOpsProjectId(), context);
            }

            // MirrorSelectionDialog dialog = new
            // MirrorSelectionDialog(project);
            // dialog.show();
          }
        }

        private void createConnection(String devOpsProjectId,
                                      MyWizardContext context) {
          final DevOpsClientProxy devOpsClientProxy =
            OracleCloudAccount.getInstance().getDevOpsClient();
          CreateConnectionResponse conn =
            devOpsClientProxy.createGithubRepositoryConnection(devOpsProjectId,
                                                               context.getSecretSummary()
                                                                      .get()
                                                                      .getId());
          System.out.println(conn.getConnection().getDisplayName());
          MirrorRepositoryResponse mirrorRepository =
            devOpsClientProxy.mirrorRepository(devOpsProjectId,
                                               context.getGithub(),
                                               conn.getConnection().getId(),
                                               context.getRepoName(),
                                               context.getRepoDescription());
          System.out.println(mirrorRepository.toString());
        }
      });
      return k;
    });
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
  }

  public static Set<String> findGitHubUrls(final GitConfig gitConfig) {
    Set<String> githubUrls = gitConfig.getUrls()
                                      .stream()
                                      .filter(s -> s.contains("github.com"))
                                      .map((s) -> {
                                        if (s.startsWith("http")) {
                                          return s;
                                        } else {
                                          return convertGithubSshToHttps(s);
                                        }
                                      })
                                      .filter(s -> s.startsWith("https"))
                                      .collect(Collectors.toSet());
    return githubUrls;
  }

  private static Pattern pattern =
    Pattern.compile("git@github.com:(.*)/(.*).git");

  public static String convertGithubSshToHttps(String url) {
    Matcher matcher = pattern.matcher(url);
    if (matcher.matches()) {
      return String.format("https://github.com/%s/%s.git\n", matcher.group(1),
                           matcher.group(2));
    }
    return url;
  }

}
