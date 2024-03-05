package com.oracle.oci.intellij.ui.devops.wizard.mirrorgh;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import com.intellij.ui.wizard.WizardDialog;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.VaultClientProxy;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory.BeanRowTable;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory.Builder;
import com.oracle.oci.intellij.ui.common.JComponentBuilder.JLabelBuilder;
import com.oracle.oci.intellij.ui.common.SpringUtilities;

public class PickSecretWizardStep
  extends WizardStep<MirrorGitHubWizardModel> {
  private JPanel secretTablePanel;
  private MyWizardContext context;
  private List<SecretSummary> listSecrets;
  private JPanel mainPanel;
  private JPanel createButtonPanel;
  private JButton createNewSecretButton;
  private BeanRowTable<SecretSummary> secretTable;

  public PickSecretWizardStep(MyWizardContext context) {
    this.context = context;
  }

  @Override
  public JComponent prepare(WizardNavigationState state) {
    this.mainPanel = new JPanel();
    this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
    
    this.createButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    this.createNewSecretButton = new JButton("Create...");
    this.createNewSecretButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        CreateSecretWizardModel.SecretWizardContext context = new CreateSecretWizardModel.SecretWizardContext();
        context.setVaultSummary(PickSecretWizardStep.this.context.getVaultSummary());
        CreateSecretWizardModel model = new CreateSecretWizardModel("New Secret Wizard", context);
        WizardDialog<CreateSecretWizardModel> wizardDialog =
          new WizardDialog<CreateSecretWizardModel>(true, model);
        if (wizardDialog.showAndGet()) {
          VaultClientProxy vaultClient =
            OracleCloudAccount.getInstance().getVaultsClient();
          Optional<VaultSummary> vaultSummary = context.getVaultSummary();
          listSecrets =
            vaultClient.listSecrets(vaultSummary.get().getCompartmentId(),
                                    vaultSummary.get().getId());
          secretTable.setRows(listSecrets);
        }
      }
    });
    this.createButtonPanel.add(this.createNewSecretButton);
    this.mainPanel.add(createButtonPanel);
    
    SpringLayout layout = new SpringLayout();
    secretTablePanel = new JPanel(layout);

    {
      JLabel secretsLbl = 
        JLabelBuilder.create().alignTrailing().alignTop().build("Secrets: ");

      secretTablePanel.add(secretsLbl);

      Builder<SecretSummary> secbuilder = BeanRowTableFactory.create();
      secbuilder.beanClass(SecretSummary.class)
                .columns("secretName", "description", "keyId",
                         "lifecycleState")
                .build();
      this.secretTable = secbuilder.build();
      secretTable.setRowSelectionAllowed(true);
      secretTable.setColumnSelectionAllowed(false);
      JScrollPane scrollPane = new JScrollPane(secretTable);
      secretTablePanel.add(scrollPane);
      VaultClientProxy vaultClient =
        OracleCloudAccount.getInstance().getVaultsClient();

      Optional<VaultSummary> vaultSummary = context.getVaultSummary();
      if (vaultSummary.isPresent()) {
        this.listSecrets =
          vaultClient.listSecrets(vaultSummary.get().getCompartmentId(),
                                  vaultSummary.get().getId());
        // only show ACTIVE
        secretTable.setRows(listSecrets); //.stream().
          //filter(s -> (LifecycleState.Active.equals(s.getLifecycleState()))).collect(Collectors.toList()));
        secretTable.setShowGrid(true);

      }
      // Lay out the panel.
      SpringUtilities.makeCompactGrid(secretTablePanel, // parent
                                      1, 2, 3, 3,  // initX, initY
                                      3, 3); // xPad, yPad

      secretTable.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.getButton() == MouseEvent.BUTTON1) {
            if (secretTable.getSelectedRowCount() == 1) {
              int selectedRow = secretTable.getSelectedRow();
              SecretSummary secretSummary = listSecrets.get(selectedRow);
              context.setSecretSummary(Optional.ofNullable(secretSummary));
              state.NEXT.setEnabled(context.getSecretSummary().isPresent());
            }
          }
        }
      });
      this.mainPanel.add(secretTablePanel);
      
      return mainPanel;
    }

  }
  
}