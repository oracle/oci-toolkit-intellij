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

import org.jetbrains.annotations.NotNull;

import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.KmsVaultClientProxy;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import com.oracle.oci.intellij.ui.common.SpringUtilities;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory.BeanRowTable;
import com.oracle.oci.intellij.ui.common.BeanRowTableFactory.Builder;
import com.oracle.oci.intellij.ui.common.JComponentBuilder.JLabelBuilder;

public class PickVaultWizardStep
  extends WizardStep<MirrorGitHubWizardModel> {
  private JPanel mainPanel;
  private MyWizardContext context;
  private JButton selectionButton;
  private List<VaultSummary> vaults;

  public PickVaultWizardStep(MyWizardContext context) {
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
    compartmentName.setEnabled(false);
    mainPanel.add(compartmentName);

    JLabel vaultsLbl = 
      JLabelBuilder.create().alignTrailing().alignTop().build("Vaults: ");
    mainPanel.add(vaultsLbl);

    Builder<VaultSummary> builder = BeanRowTableFactory.create();
    builder.beanClass(VaultSummary.class)
           .columns("displayName", "lifecycleState", "timeCreated", "vaultType");
    BeanRowTable<VaultSummary> vaultsTable = builder.build();
    Dimension minimumSize = vaultsTable.getMinimumSize();
    minimumSize.height = vaultsTable.getRowHeight()*10;
    vaultsTable.setMinimumSize(minimumSize);
    JScrollPane scrollPane = new JScrollPane(vaultsTable);
    scrollPane.setMinimumSize(minimumSize);
    scrollPane.setPreferredSize(minimumSize);
    vaultsTable.setShowGrid(true);
    mainPanel.add(scrollPane);
    vaultsLbl.setLabelFor(vaultsLbl);

    vaultsTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          if (vaultsTable.getSelectedRowCount() == 1) {
            int selectedRow = vaultsTable.getSelectedRow();
            VaultSummary vaultSummary = vaults.get(selectedRow);
            context.setVaultSummary(Optional.ofNullable(vaultSummary));
            state.NEXT.setEnabled(context.getVaultSummary().isPresent());
          }
        }
      }
    });

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