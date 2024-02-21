package com.oracle.oci.intellij.ui.devops.actions;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.JTableHeader;

import org.bouncycastle.jcajce.provider.util.SecretKeyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.bmc.devops.responses.CreateConnectionResponse;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.bmc.resourcemanager.model.JobSummary;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.DevOpsClientProxy;
import com.oracle.oci.intellij.account.OracleCloudAccount.KmsVaultClientProxy;
import com.oracle.oci.intellij.account.OracleCloudAccount.VaultClientProxy;
import com.oracle.oci.intellij.settings.OCIProjectSettings;
import com.oracle.oci.intellij.settings.OCIProjectSettings.State;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory.BeanRowTable;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory.Builder;
import com.oracle.oci.intellij.ui.common.UIUtil.GridBagLayoutConstraintBuilder;
import com.oracle.oci.intellij.ui.common.UIUtil.SimpleDialogWrapper;
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
          if (githubUrls.isEmpty()) {
            @Nullable
            State state = OCIProjectSettings.getInstance(project).getState();
            if (state != null) {
              // String projectCompartmentId = state.getCompartmentId();
              String projectId = state.getDevOpsProjectId();
              // OracleCloudAccount.getInstance().getKmsVaultClient().listVaults();
              // ProjectSummary devOpsProject =
              // OracleCloudAccount.getInstance().getDevOpsClient().getDevOpsProject(projectCompartmentId,
              // projectId);
              // if (devOpsProject)

            }
            // JOptionPane.showMessageDialog(null, "Must have a github remote",
            // "Must have github remote",
            // JOptionPane.INFORMATION_MESSAGE);
          } else {
            MirrorSelectionDialog dialog = new MirrorSelectionDialog(project);
            dialog.show();
          }
        }

        private void createConnection(String projectId) {
          final DevOpsClientProxy devOpsClientProxy =
            OracleCloudAccount.getInstance().getDevOpsClient();
          // final String projectId =
          // "ocid1.devopsproject.oc1.phx.amaaaaaadxiv6saadyecvba5jkqkbb2tvsxnc2i3g4xagtlqxqefbaxrgmxa";
          final String accessToken =
            "ocid1.vaultsecret.oc1.phx.amaaaaaadxiv6saaxngaorxmh4vekouby5cjx3iry4xv6i4rp6m7n24rzjca";// args[0];
          CreateConnectionResponse conn =
            devOpsClientProxy.createGithubRepositoryConnection(projectId,
                                                               accessToken);
          System.out.println(conn.getConnection().getDisplayName());
        }
      });
      return k;
    });
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
  }

  private Set<String> findGitHubUrls(final GitConfig gitConfig) {
    Set<String> githubUrls = gitConfig.getUrls()
                                      .stream()
                                      .filter(s -> s.contains("github.com"))
                                      .collect(Collectors.toSet());
    return githubUrls;
  }

  public static class MirrorSelectionDialog extends DialogWrapper {

    public MirrorSelectionDialog(@Nullable Project project) {
      super(project);
      setTitle("DevOps Mirror Selection Dialog");
      setOKButtonText("OK");
      init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
      JPanel centerPanel = new JPanel();
      centerPanel.setLayout(new GridLayout(0, 2));

      centerPanel.add(new JLabel("DevOps Project"));
      JTextField projectName = new JTextField();
      projectName.setEnabled(false);
      centerPanel.add(projectName);

      centerPanel.add(new JLabel("Token Key for Git"));

      JPanel textButtonBox = new JPanel();
      textButtonBox.setLayout(new BoxLayout(textButtonBox,
                                            BoxLayout.LINE_AXIS));
      centerPanel.add(textButtonBox);

      JTextField secretNameTextField = new JTextField();
      secretNameTextField.setEnabled(false);
      textButtonBox.add(secretNameTextField);
      JButton secretNameSelectAction = new JButton("Token Secret...");
      secretNameSelectAction.setAction(new OpenTokenSecretAction(centerPanel));
      textButtonBox.add(secretNameSelectAction);

      return centerPanel;
    }

  }

  public static class OpenTokenSecretAction extends AbstractAction {

    private JComponent parent;

    public OpenTokenSecretAction(JComponent parent) {
      super();
      this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

      TokenSecretSelectionDialog dialog =
        new TokenSecretSelectionDialog(parent, true);
      dialog.show();
      dialog.disposeIfNeeded();
      // VaultClientProxy vaultClient =
      // OracleCloudAccount.getInstance().getVaultsClient();
      // vaults.forEach(v -> {
      // List<SecretSummary> listSecrets =
      // vaultClient.listSecrets(v.getCompartmentId(), v.getId());
      // listSecrets.forEach(s -> System.out.println(s));
      // System.out.println();
      // listSecrets.stream().filter((s) ->
      // "camgithubtoken3".equals(s.getSecretName())).forEach(s ->
      // System.out.println(s.getId()));
      // });
      // }

    }

    static class TokenSecretSelectionDialog extends SimpleDialogWrapper {

      protected TokenSecretSelectionDialog(@NotNull Component parent,
                                           boolean canBeParent) {
        super(parent, canBeParent);
        setTitle("Select Repository Secret");
      }

      @Override
      protected @Nullable JComponent createCenterPanel() {
        JPanel centerPanel = new JPanel();
        GridBagLayout mgr = new GridBagLayout();
        centerPanel.setLayout(mgr);
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.anchor = GridBagConstraints.FIRST_LINE_START;

        
        JLabel label = new JLabel("Vaults: ");
        centerPanel.add(label, gbConstraints);

        final KmsVaultClientProxy kmsClient =
          OracleCloudAccount.getInstance().getKmsVaultClient();
        // final String projectId =
        // "ocid1.devopsproject.oc1.phx.amaaaaaadxiv6saadyecvba5jkqkbb2tvsxnc2i3g4xagtlqxqefbaxrgmxa";
        final String rootCompartmentId =
          "ocid1.tenancy.oc1..aaaaaaaagojvam7c7hthdm7h2pgshjmiqntcvei4skgysz3galuejn3rioia";

        List<VaultSummary> vaults = kmsClient.listVaults(rootCompartmentId);
        System.out.println(vaults);

        Builder<VaultSummary> builder = BeanRowTableFactory.create();
        builder.beanClass(VaultSummary.class)
               .columns("displayName", "timeCreated", "managementEndpoint", "vaultType",
                        "cryptoEndpoing", "lifecycleState", "vaultType");
        BeanRowTable<VaultSummary> vaultsTable = builder.build();
        vaultsTable.setShowGrid(true);
        // table.setTableHeader(new JTableHeader());
        centerPanel.add(vaultsTable,
                        GridBagLayoutConstraintBuilder.defaults()
                                                      .anchor(GridBagConstraints.FIRST_LINE_END)
                                                      .build());
        centerPanel.add(new JLabel("Secrets:"), GridBagLayoutConstraintBuilder.defaults()
                        .gridy(1).anchor(GridBagConstraints.LINE_START).build());
        
        Builder<SecretSummary> secbuilder = BeanRowTableFactory.create();
        secbuilder.beanClass(SecretSummary.class)
                  .columns("secretName", "description", "keyId",
                           "lifecycleDetails", "lifecycleState", "timeCreated",
                           "timeOfCurrentVersionExpiry", "timeOfDeletion")
                  .build();
        BeanRowTable<SecretSummary> secretTable = secbuilder.build();
        secretTable.setShowGrid(true);
        centerPanel.add(secretTable,
                        GridBagLayoutConstraintBuilder.defaults()
                        .anchor(GridBagConstraints.LINE_END)
                        .gridy(1)
                        .build());

        vaultsTable.setRows(vaults);
        
        
        vaultsTable.addMouseListener(new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
              if (vaultsTable.getSelectedRowCount() == 1) {
                int selectedRow = vaultsTable.getSelectedRow();
                VaultSummary vaultSummary = vaults.get(selectedRow);
                String id = vaultSummary.getId();
                if (id != null) {
                  VaultClientProxy vaultClient = OracleCloudAccount.getInstance().getVaultsClient();
                  List<SecretSummary> listSecrets = 
                    vaultClient.listSecrets(vaultSummary.getCompartmentId(), id); 
                    secretTable.setRows(listSecrets);
                }
              }
            }
          }
          });
        return centerPanel;
      }

    }
    }
}
