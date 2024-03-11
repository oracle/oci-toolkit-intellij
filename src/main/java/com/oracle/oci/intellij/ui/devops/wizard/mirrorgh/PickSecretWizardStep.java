package com.oracle.oci.intellij.ui.devops.wizard.mirrorgh;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.intellij.ui.wizard.WizardDialog;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.VaultClientProxy;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory.BeanRowTable;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory.Builder;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import com.oracle.oci.intellij.ui.common.JComponentBuilder.JLabelBuilder;
import com.oracle.oci.intellij.ui.common.SpringUtilities;
import com.oracle.oci.intellij.util.OptionalUtil.AlternateOptional;

public class PickSecretWizardStep
  extends WizardStep<MirrorGitHubWizardModel> {
  private JPanel secretTablePanel;
  private MyWizardContext context;
  private List<SecretSummary> listSecrets;
  private JPanel mainPanel;
  private JPanel setCompartmentAndCreatePanel;
  private JButton createNewSecretButton;
  private BeanRowTable<SecretSummary> secretTable;
  private JButton selectCompartmentBtn;
  private JTextField compartmentText;

  public PickSecretWizardStep(MyWizardContext context) {
    super("Select Secret for Github Token", 
          "Pick the OCI Secret that contains the Github developer token used to mirror your repository.");
    this.context = context;
  }

  @Override
  public JComponent prepare(WizardNavigationState state) {
    this.mainPanel = new JPanel();
    this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
    
    this.setCompartmentAndCreatePanel = createCompartmentAndCreatePanel();
    this.mainPanel.add(setCompartmentAndCreatePanel);
    
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

      secretTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
          int selectedRow = e.getFirstIndex();
          assert selectedRow == e.getLastIndex();
          SecretSummary secretSummary = listSecrets.get(selectedRow);
          context.setSecretSummary(Optional.ofNullable(secretSummary));
        }
      });
      
      this.mainPanel.add(secretTablePanel);
      
      return mainPanel;
    }

  }

  private JPanel createCompartmentAndCreatePanel() {
    JPanel createButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    
    createButtonPanel.setLayout(new BoxLayout(createButtonPanel, BoxLayout.LINE_AXIS));
    this.selectCompartmentBtn = new JButton("Compartment...");
    this.selectCompartmentBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        CompartmentSelection compSel = CompartmentSelection.newInstance();
        if (compSel.showAndGet()) {
          context.setSecretCompartmentId(AlternateOptional.ofNullable(compSel.getSelectedCompartment(), null));
        }
      }
    });
    createButtonPanel.add(this.selectCompartmentBtn);

    this.compartmentText = new JTextField(16);
    createButtonPanel.add(this.compartmentText);

    context.addPropertyChangeListener("secretCompartmentId",
      new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          AlternateOptional<Compartment, String> newValue = 
            (AlternateOptional<Compartment, String>) evt.getNewValue();
          Optional<String> name = newValue.map(c -> c.getName(), c -> c);
          name.ifPresent(n -> compartmentText.setText(n));
          VaultClientProxy client =
            OracleCloudAccount.getInstance()
                              .getVaultsClient();
          Optional<String> comp = newValue.map(c -> c.getCompartmentId(), c->c);
          comp.ifPresent(cId -> {
            PickSecretWizardStep.this.listSecrets = 
              client.listSecrets(cId, context.getVaultSummary().get().getId());
            secretTable.setRows(listSecrets);
          });
          
        }
      });
      Optional<String> optSecretName = this.context.getSecretCompartmentId().map(c -> c.getName(), c -> c);
      optSecretName.ifPresent(c -> {
        // TODO: if the ocid for root it will contain "tenancy".
        if (c.contains("tenancy")) {
          c = "[Root Compartment]";
        }
        compartmentText.setText(c);
      });

    this.createNewSecretButton = new JButton("Create...");
    this.createNewSecretButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        CreateSecretWizardModel.SecretWizardContext context = new CreateSecretWizardModel.SecretWizardContext();
        context.setVaultSummary(PickSecretWizardStep.this.context.getVaultSummary());
        context.setSecretCompartment(PickSecretWizardStep.this.context.getSecretCompartmentId());
        CreateSecretWizardModel model = new CreateSecretWizardModel("New Secret Wizard", context);
        WizardDialog<CreateSecretWizardModel> wizardDialog =
          new WizardDialog<CreateSecretWizardModel>(true, model);
        if (wizardDialog.showAndGet()) {
          VaultClientProxy vaultClient =
            OracleCloudAccount.getInstance().getVaultsClient();
          Optional<VaultSummary> vaultSummary = context.getVaultSummary();
          vaultSummary.ifPresent(v ->
              vaultClient.createSecret(PickSecretWizardStep.this.context.getSecretCompartmentId().getMain().get().getId(), 
                                       v,
                                       context.getKeyId(), context.getSecretName(),
                                       context.getSecretContentBase64()));
          AlternateOptional<Compartment, String> comp =
            PickSecretWizardStep.this.context.getSecretCompartmentId();
          Optional<String >compartmentId = comp.map((c) -> c.getId(), c -> c);
          compartmentId.ifPresent(cId -> {
            listSecrets =
              vaultClient.listSecrets(
                cId,
                  vaultSummary.get().getId());
            secretTable.setRows(listSecrets);
          });
        }}});
    createButtonPanel.add(this.createNewSecretButton);
    return createButtonPanel;
  }
  
}