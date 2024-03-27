package com.oracle.oci.intellij.ui.devops.wizard.mirrorgh;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jetbrains.annotations.NotNull;

import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.KmsVaultClientProxy;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory.BeanRowTable;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory.Builder;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import com.oracle.oci.intellij.ui.common.JComponentBuilder.JLabelBuilder;
import com.oracle.oci.intellij.ui.common.JComponentBuilder.JTableBuilder;
import com.oracle.oci.intellij.ui.common.SpringUtilities;
import com.oracle.oci.intellij.util.OptionalUtil.AlternateOptional;

public class PickVaultWizardStep
  extends WizardStep<MirrorGitHubWizardModel> {
  private JPanel mainPanel;
  private MyWizardContext context;
  private JButton selectionButton;
  private List<VaultSummary> vaults;
  private BeanRowTable<VaultSummary> vaultsTable;

  public PickVaultWizardStep(MyWizardContext context) {
    super("Pick the Vault where Your Github Token Is", 
          "Pick the compartment and vault where you will store the GitHub developer token that gives access to your repo.");
    this.context = context;
  }

  @SuppressWarnings("serial")
  @Override
  public JComponent prepare(WizardNavigationState state) {
    SpringLayout layout = new SpringLayout();
    mainPanel = new JPanel(layout);

    this.selectionButton = new JButton();
    this.selectionButton.setText("Select Compartment");
    mainPanel.add(selectionButton);

    JTextField compartmentName = new JTextField(20);
    compartmentName.setEditable(false);
    mainPanel.add(compartmentName);

    JLabel vaultsLbl = 
      JLabelBuilder.create().alignTrailing().alignTop().build("Vaults: ");
    mainPanel.add(vaultsLbl);

    JScrollPane scrollPane = createScrollableVaultTable();

    mainPanel.add(scrollPane);
    vaultsLbl.setLabelFor(scrollPane);

    //table.getSelectionModel().addListSelectionListener
    vaultsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        int selectedRow = e.getFirstIndex();
        assert selectedRow == e.getLastIndex();
        VaultSummary vaultSummary = vaults.get(selectedRow);
        context.setVaultSummary(Optional.ofNullable(vaultSummary));
        String vaultCompartmentId = vaultSummary.getCompartmentId();
        Compartment compartment = null;
        try {
          compartment = 
            OracleCloudAccount.getInstance().getIdentityClient().getCompartment(vaultCompartmentId);
        }
        catch (Exception excp) {
          // ignore
        }
        // default the compartment where the secret is.
        PickVaultWizardStep.this.context
          .setSecretCompartmentId(AlternateOptional.ofNullable(compartment, vaultCompartmentId));
        state.NEXT.setEnabled(context.getVaultSummary().isPresent());
      }
    });
//    vaultsTable.addMouseListener(new MouseAdapter() {
//      @Override
//      public void mouseClicked(MouseEvent e) {
//        if (e.getButton() == MouseEvent.BUTTON1) {
//          if (vaultsTable.getSelectedRowCount() == 1) {
//            int selectedRow = vaultsTable.getSelectedRow();
//            VaultSummary vaultSummary = vaults.get(selectedRow);
//            context.setVaultSummary(Optional.ofNullable(vaultSummary));
//            PickVaultWizardStep.this.context.setSecretCompartmentId(vaultSummary.getCompartmentId());
//            state.NEXT.setEnabled(context.getVaultSummary().isPresent());
//          }
//        }
//      }
//    });

    selectionButton.setAction(new AbstractAction("Select Compartment") {
      @Override
      public void actionPerformed(ActionEvent e) {
        CompartmentSelection compartmentSelection =
          CompartmentSelection.newInstance();
        if (compartmentSelection.showAndGet()) {
          Compartment selectedCompartment =
            compartmentSelection.getSelectedCompartment();
          if (selectedCompartment != null) {
            loadVaults(selectedCompartment, vaultsTable);
            compartmentName.setText(selectedCompartment.getName());
          }
        }
      }
    });

    // Lay out the panel.
    SpringUtilities.makeCompactGrid(mainPanel, // parent
                                    2, 2, 3, 3,  // initX, initY
                                    3, 3); // xPad, yPad

    state.NEXT.setEnabled(context.getVaultSummary().isPresent());

    return mainPanel;
  }

  private JScrollPane createScrollableVaultTable() {
    Builder<VaultSummary> builder = BeanRowTableFactory.create();
    JTableBuilder tableFactory = new JTableBuilder().rowSelectionAllowed(true);
    builder.beanClass(VaultSummary.class)
           .columns("displayName", "lifecycleState", "timeCreated", "vaultType")
           .tableFactory(tableFactory);
    this.vaultsTable = builder.build();
    vaultsTable.setShowGrid(true);
    Dimension minimumSize = vaultsTable.getMinimumSize();
    minimumSize.height = vaultsTable.getRowHeight()*10;
    vaultsTable.setMinimumSize(minimumSize);
    JScrollPane scrollPane = new JScrollPane(vaultsTable);
    scrollPane.setMinimumSize(minimumSize);
    scrollPane.setPreferredSize(minimumSize);
    return scrollPane;
  }
  
  private void loadVaults(Compartment compartment, BeanRowTable<VaultSummary> vaultsTable) {
    final KmsVaultClientProxy kmsClient =
      OracleCloudAccount.getInstance().getKmsVaultClient();

    String compartmentId = compartment.getId();
    System.out.println(compartmentId);
    this.vaults = kmsClient.listVaults(compartmentId);
    vaultsTable.setRows(vaults);
  }

  public @NotNull Optional<VaultSummary> getSelection() {
    return this.context.getVaultSummary();
  }
}