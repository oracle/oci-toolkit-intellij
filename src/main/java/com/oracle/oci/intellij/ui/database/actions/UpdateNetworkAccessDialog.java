package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.oracle.bmc.core.model.NetworkSecurityGroup;
import com.oracle.bmc.core.model.Subnet;
import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.VirtualNetworkClientProxy;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UpdateNetworkAccessDialog extends DialogWrapper {
  private JPanel mainPanel;
  private JRadioButton secureAccessFromEverywhereRadioButton;
  private JRadioButton privateEndpointAccessOnlyRadioButton;
  private JPanel configureAccessControlRulesPanel;
  private JPanel privateEndPointAccessPanel;
  private JButton addAccessControlRuleButton;
  private JPanel accessControlDynamicPanel;

  private final List<AccessControlRules> accessControlRulesList = new ArrayList<>();
  private PrivateEndpointAccessContent privateEndpointAccessContent = null;

  private final AutonomousDatabaseSummary autonomousDatabaseSummary;
  private final VirtualNetworkClientProxy virtualNetworkClientProxy =
          OracleCloudAccount.getInstance().getVirtualNetworkClientProxy();

  private final Function<String, List<String>> VcnListFunction = compartmentId -> {
    final List<Vcn> vcnList = virtualNetworkClientProxy.listVcns(compartmentId);
    final List<String> vcnNames = new ArrayList<>();
    vcnList.forEach(vcn -> vcnNames.add(vcn.getDisplayName()));
    return vcnNames;
  };

  private final Function<String, List<String>> listSubnetsFunction = compartmentId -> {
    final List<Subnet> subnetList = virtualNetworkClientProxy.listSubnets(compartmentId);
    final List<String> subnetNames = new ArrayList<>();
    subnetList.forEach(subnet -> subnetNames.add(subnet.getDisplayName()));
    return subnetNames;
  };

  private final Function<String, List<String>> nsgListFunction = compartmentId -> {
    final List<NetworkSecurityGroup> networkSecurityGroupList =
            virtualNetworkClientProxy.listNetworkSecurityGroups(compartmentId);
    final List<String> nsgNames = new ArrayList<>();
    networkSecurityGroupList.forEach(nsg -> nsgNames.add(nsg.getDisplayName()));
    return nsgNames;
  };

  protected UpdateNetworkAccessDialog(AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    init();
    setTitle("Update Network Access");
    setOKButtonText("Update");

    final ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(secureAccessFromEverywhereRadioButton);
    buttonGroup.add(privateEndpointAccessOnlyRadioButton);

    // Defaults.
    secureAccessFromEverywhereRadioButton.setSelected(true);
    privateEndPointAccessPanel.setVisible(false);

    // Toggle the sub panels based on the radio button selected.
    secureAccessFromEverywhereRadioButton.addActionListener(event -> {
      configureAccessControlRulesPanel.setVisible(true);
      privateEndPointAccessPanel.setVisible(false);
    });

    // Toggle the sub panels based on the radio button selected.
    privateEndpointAccessOnlyRadioButton.addActionListener(event -> {
      configureAccessControlRulesPanel.setVisible(false);
      privateEndPointAccessPanel.setVisible(true);
    });

    initSecureAccessFromEverywhereDynamicPanel();
    initPrivateEndpointAccessDynamicPanel();
  }

  private void initSecureAccessFromEverywhereDynamicPanel() {
    // This is the dynamically growing panel.
    accessControlDynamicPanel.setLayout(new BoxLayout(accessControlDynamicPanel, BoxLayout.PAGE_AXIS));
    addIPNotationTypeValues();

    // The action handler of this button should add a new IP notation type and values pair to dynamic panel.
    addAccessControlRuleButton.addActionListener(event -> addIPNotationTypeValues());
  }

  private class PrivateEndpointAccessContent {
    private final ComboBox<String> virtualCloudNetworkComboBox;
    private final ComboBox<String> subnetComboBox;
    private final JTextField hostNamePrefixTextField = new JTextField();
    private final JPanel outerPanel = new JPanel();
    private final List<ComboBox<String>> networkSecurityGroupsComboBoxList = new ArrayList<>();

    private PrivateEndpointAccessContent() {
      outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.PAGE_AXIS));
      {
        final var virtualCloudNetworkAndChangeCompartmentComponent
                = new ComboBoxAndChangeCompartmentDuo("Virtual cloud network in ", VcnListFunction);
        virtualCloudNetworkComboBox = virtualCloudNetworkAndChangeCompartmentComponent.getComboBox();
        outerPanel.add(virtualCloudNetworkAndChangeCompartmentComponent.getPanel());
      }

      {
        final var subnetAndChangeCompartmentComponent
                = new ComboBoxAndChangeCompartmentDuo("Subnet in ", listSubnetsFunction);
        subnetComboBox = subnetAndChangeCompartmentComponent.getComboBox();
        outerPanel.add(subnetAndChangeCompartmentComponent.getPanel());
      }

      {
        final JPanel hostNamePrefixPanel = new JPanel();
        hostNamePrefixPanel.setLayout(new BoxLayout(hostNamePrefixPanel, BoxLayout.PAGE_AXIS));

        final TitledBorder hostNamePanelTitleBorder = BorderFactory.createTitledBorder("Host name prefix          Optional");
        hostNamePrefixPanel.setBorder(hostNamePanelTitleBorder);
        hostNamePrefixPanel.add(hostNamePrefixTextField);
        hostNamePrefixTextField.setToolTipText("The name can contain only letters and numbers and a maximum of 63 characters.");
        outerPanel.add(hostNamePrefixPanel);
      }

      {
        final JPanel networkSecurityGroupsPanel = new JPanel();
        networkSecurityGroupsPanel.setLayout(new BoxLayout(networkSecurityGroupsPanel, BoxLayout.PAGE_AXIS));

        final TitledBorder nsgTitleBorder = BorderFactory.createTitledBorder("Network security groups (NSGs)");
        networkSecurityGroupsPanel.setBorder(nsgTitleBorder);
        networkSecurityGroupsPanel.setToolTipText("An NSG has a set of security rules that control allowed types of inbound and outbound traffic.");
        outerPanel.add(networkSecurityGroupsPanel);
        initDynamicPanel(networkSecurityGroupsPanel);
      }
    }

    private void initDynamicPanel(JPanel networkSecurityGroupsPanel) {
      final JPanel dynamicPanel = new JPanel();
      dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.PAGE_AXIS));
      // Add one panel initially.
      addNsgAndRemoveButtonPanel(dynamicPanel);
      networkSecurityGroupsPanel.add(dynamicPanel);

      // The button "+ Another Network Security Group".
      {
        final JPanel anotherNetworkSecurityGroupButtonPanel = new JPanel();
        anotherNetworkSecurityGroupButtonPanel.setLayout(new BoxLayout(anotherNetworkSecurityGroupButtonPanel, BoxLayout.LINE_AXIS));
        anotherNetworkSecurityGroupButtonPanel.add(new JSeparator());
        final JButton anotherNetworkSecurityGroupButton = new JButton("+ Another Network Security Group");
        anotherNetworkSecurityGroupButton.addActionListener(e -> {
          addNsgAndRemoveButtonPanel(dynamicPanel);
          dynamicPanel.revalidate();
        });
        anotherNetworkSecurityGroupButtonPanel.add(anotherNetworkSecurityGroupButton);
        networkSecurityGroupsPanel.add(anotherNetworkSecurityGroupButtonPanel);
      }
    }

    private void addNsgAndRemoveButtonPanel(JPanel dynamicPanel) {
      final JPanel nsgAndRemoveButtonPanel = new JPanel();
      nsgAndRemoveButtonPanel.setLayout(new BoxLayout(nsgAndRemoveButtonPanel, BoxLayout.LINE_AXIS));

      final ComboBoxAndChangeCompartmentDuo networkSecurityGroupsComboBoxAndChangeCompartment
              = new ComboBoxAndChangeCompartmentDuo("Network Security Groups in ", nsgListFunction);
      nsgAndRemoveButtonPanel.add(networkSecurityGroupsComboBoxAndChangeCompartment.getPanel());
      networkSecurityGroupsComboBoxList.add(networkSecurityGroupsComboBoxAndChangeCompartment.getComboBox());

      final JPanel removeButtonPanel = new JPanel();
      removeButtonPanel.setLayout(new BoxLayout(removeButtonPanel, BoxLayout.PAGE_AXIS));
      removeButtonPanel.setBorder(BorderFactory.createTitledBorder(" "));
      final JButton removeButton = new JButton("X");
      removeButton.setToolTipText("Remove Network Security Group");
      removeButton.addActionListener(e -> {
        networkSecurityGroupsComboBoxList.remove(networkSecurityGroupsComboBoxAndChangeCompartment.getComboBox());
        dynamicPanel.remove(nsgAndRemoveButtonPanel);
        dynamicPanel.revalidate();
      });
      removeButtonPanel.add(removeButton);
      nsgAndRemoveButtonPanel.add(removeButtonPanel);
      dynamicPanel.add(nsgAndRemoveButtonPanel);
    }

    public String getVirtualCloudNetwork() {
      return (String) virtualCloudNetworkComboBox.getSelectedItem();
    }

    public String getSubnet() {
      return (String) subnetComboBox.getSelectedItem();
    }

    public String getHostNamePrefix() {
      return hostNamePrefixTextField.getText();
    }

    public List<String> getNetworkSecurityGroupsComboBoxList() {
      final List<String> networkSecurityGroupsList = new ArrayList<>();
      networkSecurityGroupsComboBoxList.forEach(comboBox -> networkSecurityGroupsList.add((String) comboBox.getSelectedItem()));
      return networkSecurityGroupsList;
    }

    public JPanel getPanel() {
      return outerPanel;
    }
  }

  private void initPrivateEndpointAccessDynamicPanel() {
    privateEndPointAccessPanel.setLayout(new BoxLayout(privateEndPointAccessPanel, BoxLayout.PAGE_AXIS));
    privateEndpointAccessContent = new PrivateEndpointAccessContent();
    privateEndPointAccessPanel.add((privateEndpointAccessContent).getPanel());
  }

  private void addIPNotationTypeValues() {
    // Get a new instance of IP notation type and values pair.
    final AccessControlRules accessControlRules = new AccessControlRules();
    // Keep the newly created instances in a list to read the user selected values.
    accessControlRulesList.add(accessControlRules);

    // Add IP notation type and values pair panel to wrapper panel.
    final JPanel outerWrapperPanel = new JPanel();
    outerWrapperPanel.setLayout(new BoxLayout(outerWrapperPanel, BoxLayout.LINE_AXIS));
    outerWrapperPanel.add(accessControlRules.getPanel());

    // Anonymous block to create a namespace.
    {
      final JButton removeButton = new JButton("x");
      removeButton.setToolTipText("Remove Access Control Rule");
      removeButton.addActionListener(e -> {
        accessControlRulesList.remove(accessControlRules);
        accessControlDynamicPanel.remove(outerWrapperPanel);
        accessControlDynamicPanel.revalidate();
      });
      // Wrap the remove button in a panel.
      final JPanel removePanel = new JPanel();
      removePanel.setLayout(new BoxLayout(removePanel, BoxLayout.PAGE_AXIS));
      removePanel.add(new JSeparator());
      removePanel.setBorder(BorderFactory.createTitledBorder(" "));
      removePanel.add(removeButton);
      removePanel.add(new JSeparator());
      // Add this remove panel the wrapper panel.
      outerWrapperPanel.add(removePanel);
    }

    // Now add this wrapper panel to the dynamic panel.
    accessControlDynamicPanel.add(outerWrapperPanel);
    accessControlDynamicPanel.revalidate();
    System.out.println("Size = " + accessControlRulesList.size());
  }

  @Override
  protected void doOKAction() {
    if (secureAccessFromEverywhereRadioButton.isSelected()) {
      accessControlRulesList.forEach(accessControlRules -> {
        System.out.println();
        System.out.print("Type = " + accessControlRules.getIpNotationType());
        switch (accessControlRules.getIpNotationType()) {
          case "IP Address":
          case "CIDR Block":
            System.out.print("\tValues = " + accessControlRules.getValues());
            break;
          case "Virtual Cloud Network":
            System.out.print("\tVirtual Cloud Network = "
                    + accessControlRules.getVirtualCloudNetworkValues().getVirtualCloudNetworkComboBox().getSelectedItem());
            System.out.print("\tIP addresses or CIDRs = "
                    + accessControlRules.getVirtualCloudNetworkValues().getIpAddressesOrCIDRsOptionalValue());
            break;
            case "Virtual Cloud Network (OCID)":
              System.out.print("\tValues = " + accessControlRules.getVirtualCloudNetworkOcidValues().getValues());
              System.out.print("\tIP addresses or CIDRs = "
                      + accessControlRules.getVirtualCloudNetworkOcidValues().getIpAddressesOrCIDRsOptionalValues());
              break;
          default:
            System.out.println("Warning: Unknown Type!!");
            break;
        }
      });
    } else if (privateEndpointAccessOnlyRadioButton.isSelected()) {
      System.out.print("Virtual Cloud Network = "
              + privateEndpointAccessContent.getVirtualCloudNetwork());
      System.out.print("\tSubnet = "
              + privateEndpointAccessContent.getSubnet());
      System.out.print("\tHost name = "
              + privateEndpointAccessContent.getHostNamePrefix());
      privateEndpointAccessContent.getNetworkSecurityGroupsComboBoxList().forEach(nsgItem -> System.out.println("Network Security Group = " + nsgItem));
    }

    close(OK_EXIT_CODE);
  }

  private class ComboBoxAndChangeCompartmentDuo {
    final JPanel comboBoxAndChangeCompartmentPanel = new JPanel();
    private final ComboBox<String> comboBox = new ComboBox<>();

    public ComboBoxAndChangeCompartmentDuo(String panelTextPrefix,
                                            Function<String, List<String>> function) {
      final JPanel comboBoxPanel = new JPanel();
      comboBoxPanel.setLayout(new BoxLayout(comboBoxPanel, BoxLayout.LINE_AXIS));
      comboBoxPanel.add(comboBox);

      ApplicationManager.getApplication().executeOnPooledThread(() -> {
        final String compartmentName = OracleCloudAccount.getInstance().getIdentityClient()
                .getCompartment(autonomousDatabaseSummary.getCompartmentId()).getName();
        final TitledBorder comboBoxPanelTitleBorder = BorderFactory.createTitledBorder(panelTextPrefix + compartmentName);
        comboBoxPanel.setBorder(comboBoxPanelTitleBorder);
        comboBox.removeAllItems();

        final List<String> comboBoxItems = function.apply(autonomousDatabaseSummary.getCompartmentId());
        comboBoxItems.forEach(comboBox::addItem);
      });

      // Now create a button for compartment selection.
      final JButton changeCompartmentButton = new JButton("Change Compartment");
      changeCompartmentButton.addActionListener(e -> {
        final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

        if(compartmentSelection.showAndGet()) {
          final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
          comboBox.removeAllItems();

          final List<String> items = function.apply(selectedCompartment.getCompartmentId());
          items.forEach(comboBox::addItem);

          final Border newTitleBorder = BorderFactory
                  .createTitledBorder(panelTextPrefix + selectedCompartment.getName());
          comboBoxPanel.setBorder(newTitleBorder);
        }
      });
      final JPanel changeCompartmentPanel = new JPanel();
      changeCompartmentPanel.setLayout(new BoxLayout(changeCompartmentPanel, BoxLayout.LINE_AXIS));
      changeCompartmentPanel.setBorder(BorderFactory.createTitledBorder(" "));
      changeCompartmentPanel.add(changeCompartmentButton);

      // Now wrap the panels "Virtual cloud network in " and "Change Compartment" in a new panel.
      comboBoxAndChangeCompartmentPanel.setLayout(new BoxLayout(comboBoxAndChangeCompartmentPanel, BoxLayout.LINE_AXIS));
      comboBoxAndChangeCompartmentPanel.add(comboBoxPanel);
      comboBoxAndChangeCompartmentPanel.add(changeCompartmentPanel);
    }

    public ComboBox<String> getComboBox() {
      return comboBox;
    }

    public JPanel getPanel() {
      return comboBoxAndChangeCompartmentPanel;
    }
  }

  /**
   * The UI components of IP notation type and values pair.
   */
  private class AccessControlRules {
    private final ComboBox<String> ipNotationTypeComboBox = new ComboBox<>();
    private final JTextField ipOrCidrValuesTextField = new JTextField();
    private final JPanel valuesPanel = new JPanel();

    // Value panels for each type.
    final JPanel ipOrCidrValuesPanel = new JPanel();
    final VirtualCloudNetworkValues virtualCloudNetworkValues = new VirtualCloudNetworkValues();
    final VirtualCloudNetworkOcidValues virtualCloudNetworkOcidValues = new VirtualCloudNetworkOcidValues();

    public AccessControlRules() {
      buildIPNotationTypeValuesPanel();
    }

    public JPanel getPanel() {
      return valuesPanel;
    }

    public String getIpNotationType() {
      return ipNotationTypeComboBox.getItem();
    }

    public String getValues() {
      return ipOrCidrValuesTextField.getText();
    }

    public VirtualCloudNetworkValues getVirtualCloudNetworkValues() {
      return virtualCloudNetworkValues;
    }

    public VirtualCloudNetworkOcidValues getVirtualCloudNetworkOcidValues() {
      return virtualCloudNetworkOcidValues;
    }

    private void buildIPNotationTypeValuesPanel() {
      // Left side component.
      ipNotationTypeComboBox.addItem("IP Address");
      ipNotationTypeComboBox.addItem("CIDR Block");
      ipNotationTypeComboBox.addItem("Virtual Cloud Network");
      ipNotationTypeComboBox.addItem("Virtual Cloud Network (OCID)");

      // Wrap the left side component in a panel.
      final JPanel ipNotationTypePanel = new JPanel();
      ipNotationTypePanel.setLayout(new BoxLayout(ipNotationTypePanel, BoxLayout.PAGE_AXIS));
      ipNotationTypePanel.setBorder(BorderFactory.createTitledBorder("IP notation type"));
      ipNotationTypePanel.add(ipNotationTypeComboBox);
      ipNotationTypePanel.add(new JSeparator());

      // Right side component. Wrap this one too in its own panel.
      ipOrCidrValuesPanel.setLayout(new BoxLayout(ipOrCidrValuesPanel, BoxLayout.LINE_AXIS));
      ipOrCidrValuesPanel.setBorder(BorderFactory.createTitledBorder("Values"));
      ipOrCidrValuesPanel.add(ipOrCidrValuesTextField);

      // Now create a wrapper panel and add the left and right component panels in it.
      valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.LINE_AXIS));
      valuesPanel.add(ipNotationTypePanel);
      valuesPanel.add(ipOrCidrValuesPanel);

      ipNotationTypeComboBox.addActionListener(e -> {
        // To be safe. No-op for components that were not added in the first place.
        valuesPanel.remove(ipOrCidrValuesPanel);
        valuesPanel.remove(virtualCloudNetworkValues.getPanel());
        valuesPanel.remove(virtualCloudNetworkOcidValues.getPanel());

        switch (ipNotationTypeComboBox.getSelectedIndex()) {
          case 0:
          case 1:
            valuesPanel.add(ipOrCidrValuesPanel);
            break;
          case 2:
            valuesPanel.add(virtualCloudNetworkValues.getPanel());
            break;
          case 3:
            valuesPanel.add(virtualCloudNetworkOcidValues.getPanel());
            break;
        }
        valuesPanel.revalidate();
      });
    }
  }

  private class VirtualCloudNetworkValues {
    private ComboBox<String> virtualCloudNetworkComboBox;
    private final JTextField ipAddressesOrCIDRsOptionalTextField = new JTextField();
    private final JPanel valuesPanel = new JPanel();

    public VirtualCloudNetworkValues() {
      buildVirtualCloudNetworkValuesPanel();
    }

    public JPanel getPanel() {
      return valuesPanel;
    }

    private void buildVirtualCloudNetworkValuesPanel() {
      final var virtualCloudNetworkAndChangeCompartmentComponent =
              new ComboBoxAndChangeCompartmentDuo("Virtual cloud network in ", VcnListFunction);

      virtualCloudNetworkComboBox = virtualCloudNetworkAndChangeCompartmentComponent.getComboBox();

      // Create a wrapper panel for "IP addresses or CIDRs (Optional)" and wrap text field in it.
      final JPanel ipAddressesOrCidrsOptionalTextFieldPanel = new JPanel();
      ipAddressesOrCidrsOptionalTextFieldPanel.setLayout(new BoxLayout(ipAddressesOrCidrsOptionalTextFieldPanel, BoxLayout.LINE_AXIS));
      ipAddressesOrCidrsOptionalTextFieldPanel.setBorder(BorderFactory.createTitledBorder("IP addresses or CIDRs          Optional"));
      ipAddressesOrCidrsOptionalTextFieldPanel.add(ipAddressesOrCIDRsOptionalTextField);

      valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.PAGE_AXIS));
      valuesPanel.add(virtualCloudNetworkAndChangeCompartmentComponent.getPanel());
      valuesPanel.add(ipAddressesOrCidrsOptionalTextFieldPanel);
    }

    public ComboBox<String> getVirtualCloudNetworkComboBox() {
      return virtualCloudNetworkComboBox;
    }

    public String getIpAddressesOrCIDRsOptionalValue() {
      return ipAddressesOrCIDRsOptionalTextField.getText();
    }
  }

  private class VirtualCloudNetworkOcidValues {
    private final JTextField valuesTextField = new JTextField();
    private final JTextField ipAddressesOrCIDRsOptionalTextField = new JTextField();
    private final JPanel valuesPanel = new JPanel();

    public VirtualCloudNetworkOcidValues() {
      buildVirtualCloudNetworkOcidValuesPanel();
    }

    public JPanel getPanel() {
      return valuesPanel;
    }

    private void buildVirtualCloudNetworkOcidValuesPanel() {
      // Create a wrapper panel for "Virtual cloud network in" and wrap the combo box in it.
      final JPanel virtualCloudNetworkOcidValuesTextFieldWrapperPanel = new JPanel();
      virtualCloudNetworkOcidValuesTextFieldWrapperPanel.setLayout(new BoxLayout(virtualCloudNetworkOcidValuesTextFieldWrapperPanel, BoxLayout.PAGE_AXIS));
      virtualCloudNetworkOcidValuesTextFieldWrapperPanel.setBorder(BorderFactory.createTitledBorder("Values"));
      virtualCloudNetworkOcidValuesTextFieldWrapperPanel.add(valuesTextField);

      // Create a wrapper panel for "IP addresses or CIDRs (Optional)" and wrap text field in it.
      final JPanel ipAddressesOrCidrsOptionalTextFieldPanel = new JPanel();
      ipAddressesOrCidrsOptionalTextFieldPanel.setLayout(new BoxLayout(ipAddressesOrCidrsOptionalTextFieldPanel, BoxLayout.LINE_AXIS));
      ipAddressesOrCidrsOptionalTextFieldPanel.setBorder(BorderFactory.createTitledBorder("IP addresses or CIDRs          Optional"));
      ipAddressesOrCidrsOptionalTextFieldPanel.add(ipAddressesOrCIDRsOptionalTextField);

      valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.PAGE_AXIS));
      valuesPanel.add(virtualCloudNetworkOcidValuesTextFieldWrapperPanel);
      valuesPanel.add(ipAddressesOrCidrsOptionalTextFieldPanel);
    }

    public String getValues() {
      return valuesTextField.getText();
    }

    public String getIpAddressesOrCIDRsOptionalValues() {
      return ipAddressesOrCIDRsOptionalTextField.getText();
    }
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    final JBScrollPane jbScrollPane = new JBScrollPane(mainPanel);
    jbScrollPane.setPreferredSize(new Dimension(900, 500));
    return jbScrollPane;
  }
}
