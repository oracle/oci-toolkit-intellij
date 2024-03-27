package com.oracle.oci.intellij.ui.devops.wizard.mirrorgh;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Base64;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.util.NlsContexts.DialogTitle;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.wizard.WizardModel;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.keymanagement.model.KeySummary;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.KmsManagementClientProxy;
import com.oracle.oci.intellij.common.ObjectUtils;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory.BeanRowTable;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory.Builder;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import com.oracle.oci.intellij.ui.common.JComponentBuilder.JLabelBuilder;
import com.oracle.oci.intellij.ui.common.SpringUtilities;
import com.oracle.oci.intellij.ui.common.UIUtil.GridBagLayoutConstraintBuilder;
import com.oracle.oci.intellij.ui.common.UIUtil.SimpleDialogWrapper;
import com.oracle.oci.intellij.ui.common.WizardContext;
import com.oracle.oci.intellij.util.OptionalUtil.AlternateOptional;

public class CreateSecretWizardModel extends WizardModel {
  private @NotNull WizardContext context;

  public static class SecretWizardContext extends WizardContext {

    private Optional<VaultSummary> vaultSummary;
    private String keyName;
    private String keyId;
    private String secretName;
    private String secretText;
    private AlternateOptional<Compartment, String> secretCompartment;
    private Compartment selectedKeyCompartment;

    public Optional<VaultSummary> getVaultSummary() {
      return this.vaultSummary;
    }

    public void setVaultSummary(Optional<VaultSummary> vaultSummary) {
      this.vaultSummary = vaultSummary;
    }

    public String getKeyName() {
      return this.keyName;
    }

    public void setKeyName(String keyName) {
      String oldKeyName = this.keyName;
      this.keyName = keyName;
      pcs.firePropertyChange("keyName", oldKeyName, this.keyName);
    }

    public boolean validate() {
      return !ObjectUtils.isEmpty(this.secretName, this.secretText);
    }

    public void setSecretText(String text) {
      String oldSecretText = this.secretText;
      this.secretText = text;
      pcs.firePropertyChange("secretText", oldSecretText, this.secretText);
    }

    public Compartment getKeyCompartment() {
      return this.selectedKeyCompartment;
    }

    public String getSecretName() {
      return secretName;
    }

    public void setSecretName(String secretName) {
      String oldValue = this.secretName;
      this.secretName = secretName;
      pcs.firePropertyChange("secretName", oldValue, this.secretName);
    }

    public void setKeyCompartment(Compartment selectedCompartment) {
      Compartment oldSelectedKeyCompartment = this.selectedKeyCompartment;
      this.selectedKeyCompartment = selectedCompartment;
      pcs.firePropertyChange("keyCompartment", oldSelectedKeyCompartment,
                             this.selectedKeyCompartment);
    }

    public String getKeyId() {
      return this.keyId;
    }
    
    public void setKeyId(String keyId) {
      String oldValue = this.keyId;
      this.keyId = keyId;
      pcs.firePropertyChange("keyId", oldValue, this.keyId);
    }

    public AlternateOptional<Compartment, String> getSecretCompartment() {
      return secretCompartment;
    }

    public void setSecretCompartment(AlternateOptional<Compartment, String> secretCompartment) {
      AlternateOptional<Compartment, String> oldValue = this.secretCompartment;
      this.secretCompartment = secretCompartment;
      pcs.firePropertyChange("secretCompartment", oldValue, this.secretCompartment);
    }

    public String getSecretContentBase64() {
      if (this.secretText != null) {
        return Base64.getEncoder().encodeToString(this.secretText.getBytes());
      }
      return null;
    }
  }

  public CreateSecretWizardModel(@DialogTitle String title,
                                 @NotNull SecretWizardContext context) {
    super(title);
    this.context = context;

    add(new CreateSecretWizardStep(context));
    add(new SelectOrCreateKeyStep(context));
  }

  public static class SelectOrCreateKeyStep
    extends WizardStep<CreateSecretWizardModel> {

    private @NotNull SecretWizardContext context;
    private JPanel mainPanel;
    private JPanel keyTablePanel;
    private List<KeySummary> keys;
    private JPanel newKeyPanel;
    private JButton createNewSecretButton;
    private BeanRowTable<KeySummary> keysTable;
    private JButton selectCompartmentBtn;
    private JTextField compartmentText;

    public SelectOrCreateKeyStep(@NotNull SecretWizardContext context) {
      super("Select or Create A Key to Encrypt Your Secret", 
            "The key is symmetric 256 bit AES encryption key.");
      this.context = context;
    }

    @Override
    public JComponent prepare(WizardNavigationState state) {
      this.mainPanel = new JPanel();
      this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));

      this.newKeyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

      newKeyPanel.setLayout(new BoxLayout(newKeyPanel, BoxLayout.LINE_AXIS));
      this.selectCompartmentBtn = new JButton("Compartment...");
      this.selectCompartmentBtn.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          CompartmentSelection compSel = CompartmentSelection.newInstance();
          if (compSel.showAndGet()) {
            context.setKeyCompartment(compSel.getSelectedCompartment());
          }
        }
      });
      newKeyPanel.add(this.selectCompartmentBtn);

      this.compartmentText = new JTextField(16);
      newKeyPanel.add(this.compartmentText);
      context.addPropertyChangeListener("keyCompartment",
                                  new PropertyChangeListener() {
                                    @Override
                                    public void propertyChange(PropertyChangeEvent evt) {
                                      compartmentText.setText(((Compartment) evt.getNewValue()).getName());
                                      KmsManagementClientProxy client =
                                        OracleCloudAccount.getInstance()
                                                          .getKmsManagementClient(context.getVaultSummary()
                                                                                         .orElseThrow());
                                      List<KeySummary> listKeys =
                                        client.listKeys(context.getKeyCompartment()
                                                               .getId());
                                      keysTable.setRows(listKeys);
                                    }
                                  });
      Optional<Compartment> optionalCompartmentText = Optional.ofNullable(context.getKeyCompartment());
      optionalCompartmentText.ifPresent(comp -> compartmentText.setText(comp.getName()));

      this.createNewSecretButton = new JButton("Create...");
      this.createNewSecretButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          NewKeyDialog dialog = new NewKeyDialog(mainPanel, true, context);
          if (dialog.showAndGet()) {
            KmsManagementClientProxy client =
              OracleCloudAccount.getInstance()
                                .getKmsManagementClient(context.getVaultSummary()
                                                               .orElseThrow());

            client.createKey(context.getKeyCompartment().getId(),
                             context.getKeyName());
          }
        }
      });
      this.newKeyPanel.add(createNewSecretButton);
      this.mainPanel.add(newKeyPanel);

      this.keyTablePanel = new JPanel();
      this.keyTablePanel.setLayout(new SpringLayout());
      this.mainPanel.add(this.keyTablePanel);
      {
        JLabel secretsLbl =
          JLabelBuilder.create().alignTrailing().alignTop().build("Key: ");

        keyTablePanel.add(secretsLbl);

        Builder<KeySummary> secbuilder = BeanRowTableFactory.create();
        secbuilder.beanClass(KeySummary.class)
                  .columns("displayName", "algorithm", "timeCreated")
                  .build();
        this.keysTable = secbuilder.build();
        keysTable.setRowSelectionAllowed(true);
        keysTable.setColumnSelectionAllowed(false);
        JScrollPane scrollPane = new JScrollPane(keysTable);
        keyTablePanel.add(scrollPane);
        Optional<VaultSummary> vaultSummary = context.getVaultSummary();
        if (vaultSummary.isPresent()) {
          KmsManagementClientProxy kmsMgmt =
            OracleCloudAccount.getInstance()
                              .getKmsManagementClient(vaultSummary.get());
          this.keys = kmsMgmt.listKeys(vaultSummary.get().getCompartmentId());
          // only show ACTIVE
          keysTable.setRows(keys); // .stream().
          // filter(s ->
          // (LifecycleState.Active.equals(s.getLifecycleState()))).collect(Collectors.toList()));
          keysTable.setShowGrid(true);

        }
        // Lay out the panel.
        SpringUtilities.makeCompactGrid(keyTablePanel, // parent
                                        1, 2, 3, 3,  // initX, initY
                                        3, 3); // xPad, yPad

        keysTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
          @Override
          public void valueChanged(ListSelectionEvent e) {
            int selectedRow = e.getFirstIndex();
            assert selectedRow == e.getLastIndex();
            KeySummary keySummary = keys.get(selectedRow);
            context.setKeyId(keySummary.getId());
          }
        });

        return mainPanel;
      }
    }
  }

  private static class NewKeyDialog extends SimpleDialogWrapper {

    private JButton selectCompartmentBtn;
    private JTextField compartmentText;
    private SecretWizardContext context;

    protected NewKeyDialog(@NotNull Component parent, boolean canBeParent,
                           @NotNull SecretWizardContext context) {
      super(parent, canBeParent);
      this.context = context;
      init();
    }

    @Override
    protected void basicInit() {
      // override to do nothing because is called from super.constructor
      // do nothing
    }

    @Override
    protected void init() {
      setOKButtonText("OK");
      super.init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
      JPanel centerPanel = new JPanel();
      centerPanel.setLayout(new BorderLayout());

      JPanel keyCreatePanel = new JPanel();
      keyCreatePanel.setLayout(new GridBagLayout());
      JLabel keyDisplayNameLbl = new JLabel("Name: ");
      keyCreatePanel.add(keyDisplayNameLbl);
      JTextField keyDisplayNameText = new JTextField(32);
      keyCreatePanel.add(keyDisplayNameText);
      keyDisplayNameText.getDocument()
                        .addDocumentListener(new DocumentAdapter() {

                          @Override
                          protected void textChanged(@NotNull DocumentEvent e) {
                            context.setKeyName(keyDisplayNameText.getText());
                          }
                        });

      // centerPanel.add(compartmentPanel, BorderLayout.NORTH);
      centerPanel.add(keyCreatePanel, BorderLayout.CENTER);

      return centerPanel;
    }

  }

  public static class CreateSecretWizardStep
    extends WizardStep<CreateSecretWizardModel> {

    private @NotNull SecretWizardContext context;
    private JPanel mainPanel;

    public CreateSecretWizardStep(@NotNull SecretWizardContext context) {
      super("Set secret name and secret");
      this.context = context;
    }

    @Override
    public JComponent prepare(WizardNavigationState state) {
      this.mainPanel = new JPanel();
      GridBagLayout layout = new GridBagLayout();
      this.mainPanel.setLayout(layout);

      JLabel compartmentLbl = new JLabel("Compartment:");
      layout.setConstraints(compartmentLbl,
                            GridBagLayoutConstraintBuilder.defaults()
                                                          .gridx(0)
                                                          .gridy(0)
                                                          .build());
      this.mainPanel.add(compartmentLbl);

      JTextField compartmentText = new JTextField(16);
      context.getSecretCompartment().ifPresent(c -> {
                                                      compartmentText.setText(c.getName());
                                                      context.setKeyCompartment(c);
                                               },
                                               c -> compartmentText.setText(c));
      layout.setConstraints(compartmentText,
                            GridBagLayoutConstraintBuilder.defaults()
                                                          .gridx(1)
                                                          .gridy(0)
                                                          .fillHorizontal()
                                                          .build());
      compartmentText.setEditable(false);
      this.mainPanel.add(compartmentText);
      
      
      JLabel secretNameLabel = new JLabel("Name: ");

      // layout.setConstraints(secretName, GridBagConstraints.);
      layout.setConstraints(secretNameLabel,
                            GridBagLayoutConstraintBuilder.defaults()
                                                          .gridx(0)
                                                          .gridy(1)
                                                          .build());
      JTextField secretNameText = new JTextField(16);
      secretNameText.getDocument().addDocumentListener(new DocumentAdapter() {
        
        @Override
        protected void textChanged(@NotNull DocumentEvent e) {
          String text = secretNameText.getText();
          if (text != null && !"".equals(text.trim())) {
            context.setSecretName(text);
          } else {
            context.setSecretName(null);
          }
          validate(state);
        }
      });
      
      layout.setConstraints(secretNameText,
                            GridBagLayoutConstraintBuilder.defaults()
                                                          .gridx(1)
                                                          .gridy(1)
                                                          .fillHorizontal()
                                                          .build());

      this.mainPanel.add(secretNameLabel);
      this.mainPanel.add(secretNameText);

      JLabel secretTextLbl = new JLabel("Secret Text: ");
      layout.setConstraints(secretTextLbl,
                            GridBagLayoutConstraintBuilder.defaults()
                                                          .gridx(0)
                                                          .gridy(2)
                                                          .build());
      JTextField secretText = new JTextField(24);
      secretText.getDocument().addDocumentListener(new DocumentAdapter() {
        
        @Override
        protected void textChanged(@NotNull DocumentEvent e) {
          String text = secretText.getText();
          if (text != null && !"".equals(text.trim())) {
            context.setSecretText(text);
          } else {
            context.setSecretText(null);
          }
          validate(state);
        }
      });
      layout.setConstraints(secretText,
                            GridBagLayoutConstraintBuilder.defaults()
                                                          .gridx(1)
                                                          .gridy(2)
                                                          .fillHorizontal()
                                                          .build());

      this.mainPanel.add(secretTextLbl);
      this.mainPanel.add(secretText);

      return this.mainPanel;
    }

    private void validate(WizardNavigationState state) {
      boolean isValid = context.validate();
      state.NEXT.setEnabled(isValid);
    }

  }
}
