package com.oracle.oci.intellij.ui.database.actions;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBScrollPane;
import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.database.model.AutonomousDatabase;
import com.oracle.bmc.database.model.AutonomousDatabase.LifecycleState;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.OracleCloudAccount.DatabaseClientProxy;
import com.oracle.oci.intellij.account.OracleCloudAccount.VirtualNetworkClientProxy;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;
import com.oracle.oci.intellij.ui.database.AutonomousDatabasesDashboard;
import com.oracle.oci.intellij.ui.database.model.AccessControlType;
import com.oracle.oci.intellij.ui.database.model.CIDRBlockType;
import com.oracle.oci.intellij.ui.database.model.IPAddressType;
import com.oracle.oci.intellij.ui.database.model.OCIModelWrappers.VcnComboWrapper;
import com.oracle.oci.intellij.ui.database.model.OcidBasedAccessControlType;
import com.oracle.oci.intellij.ui.database.model.UnknownAccessControlType;

public class UpdateNetworkAccessDialog extends DialogWrapper {
  private JPanel mainPanel;
  private JRadioButton secureAccessFromEverywhereRadioButton;
  private JRadioButton privateEndpointAccessOnlyRadioButton;
  private JPanel configureAccessControlRulesPanel;
  private JPanel privateEndPointAccessPanel;
  private JButton addAccessControlRuleButton;
  private JPanel accessControlDynamicPanel;
  private JCheckBox configureAccessControlRulesCheckBox;
  private JCheckBox requireMutualTLSMTLSCheckBox;
  private JPanel accessTypeSelectionPanel;
  private JLabel  moreInformationHyperlinkLabel;

  private final List<AccessControlRules> accessControlRulesList = new ArrayList<>();
  // TODO: FUTURE private PrivateEndpointAccessContent privateEndpointAccessContent = null;

  private AutonomousDatabaseSummary autonomousDatabaseSummary;

  private static final VirtualNetworkClientProxy virtualNetworkClientProxy =
          OracleCloudAccount.getInstance().getVirtualNetworkClientProxy();

  protected static List<Vcn> getVcnList(String compartmentId) {
      return virtualNetworkClientProxy.listVcns(compartmentId);
  }
  
  private static Vcn getVcn(String ocid) {
      return virtualNetworkClientProxy.getVcn(ocid);
  }

  protected UpdateNetworkAccessDialog(AutonomousDatabaseSummary autonomousDatabaseSummary) {
    super(true);
    this.autonomousDatabaseSummary = autonomousDatabaseSummary;
    init();
    setTitle("Update Network Access");
    setOKButtonText("Update");

    moreInformationHyperlinkLabel.setText("<html><a href=''>More information (opens browser link)</a></html>");
    moreInformationHyperlinkLabel.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                 
                Desktop.getDesktop().browse(new URI("https://docs.oracle.com/en/cloud/paas/autonomous-database/adbsa/access-control-rules-autonomous.html#GUID-483CD2B4-5898-4D27-B74E-6735C32CB58C"));
            } catch (IOException | URISyntaxException e1) {
                e1.printStackTrace();
            }
        }
    });
    final ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(secureAccessFromEverywhereRadioButton);
    //buttonGroup.add(privateEndpointAccessOnlyRadioButton);

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
    /// TODO: FUTURE initPrivateEndpointAccessDynamicPanel();
    initShouldConfigure();
  }

  @Override
    protected @Nullable ValidationInfo doValidate() {
      for (AccessControlRules rule : this.accessControlRulesList) {
          @Nullable ValidationInfo validate = rule.valuesUI.validate();
          if (validate != null) {
              return validate;
          }
      }
      return null;
    }
    
    private void initShouldConfigure() {
    List<String> whiteListedIps = this.autonomousDatabaseSummary.getWhitelistedIps();
    if (whiteListedIps == null)
    {
      whiteListedIps = Collections.emptyList();
    }

    boolean aclEnabled;
    if (!whiteListedIps.isEmpty()) {
      aclEnabled = true;
    } else {
      aclEnabled = false;
    }
    this.configureAccessControlRulesCheckBox.setSelected(aclEnabled);
    this.configureAccessControlRulesCheckBox.addActionListener(event -> {
      boolean networkAccessConfigured = configureAccessControlRulesCheckBox.isSelected();
      updateAclUpdate(networkAccessConfigured);
    });
    this.requireMutualTLSMTLSCheckBox.setSelected(this.autonomousDatabaseSummary.getIsMtlsConnectionRequired());
    if (aclEnabled) {
      System.out.println("aclEnabled");
      populateAccessOptionsUI(whiteListedIps);
    }
    updateAclUpdate(aclEnabled);
  }

  private void populateAccessOptionsUI(List<String> whiteListedIps) {
    for (String whiteListedIp : whiteListedIps) {
      AccessControlType parseAcl = parseAcl(whiteListedIp);
      addIPNotationTypeValues(parseAcl);
    }
  }

  private AccessControlType parseAcl(String aclStr) {
    if (aclStr.startsWith("ocid"))
    {
      OcidBasedAccessControlType ocidAcl = OcidBasedAccessControlType.parseOcidAcl(aclStr);
      ocidAcl.setVcn(getVcn(ocidAcl.getOcid()));
      return ocidAcl;
    }
    Matcher matcher = IPAddressType.IP_ADDR_PATTERN.matcher(aclStr);
    if (matcher.matches()) {
      return new IPAddressType(aclStr);
    } else {
      matcher = CIDRBlockType.CIDR_BLOCK_PATTERN.matcher(aclStr);
      if (matcher.matches()) {
        return new CIDRBlockType(aclStr);
      } else {
        return new UnknownAccessControlType(aclStr);
      }
    }
  }

  private void updateAclUpdate(boolean enabled) {
    System.out.println(enabled);
    recurseComponents(accessTypeSelectionPanel, enabled);
    recurseComponents(configureAccessControlRulesPanel, enabled);
  }

  private void recurseComponents(Component root, boolean enabled) {
    if (root instanceof Container) {
      Component components[] = ((Container)root).getComponents();
      for (int i = 0; i < components.length; i++) {
        recurseComponents(components[i], enabled);
      }
    }
    root.setEnabled(enabled);
  }
  private void initSecureAccessFromEverywhereDynamicPanel() {
    // This is the dynamically growing panel.
    accessControlDynamicPanel.setLayout(new BoxLayout(accessControlDynamicPanel, BoxLayout.PAGE_AXIS));

    // The action handler of this button should add a new IP notation type and values pair to dynamic panel.
    addAccessControlRuleButton.addActionListener(event -> {
        final Frame f= new Frame("PopupMenu");  
        JPopupMenu menu = new JPopupMenu();
        
        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (e.getActionCommand())
                {
                case "IP Notation":
                    addIPNotationTypeValues(new IPAddressType(""));
                    break;
                case "CIDR Notation":
                    addIPNotationTypeValues(new CIDRBlockType(""));
                    break;
                case "VCN by Name":
                    addIPNotationTypeValues(new OcidBasedAccessControlType("", Collections.emptyList()));
                    break;
                }
            }
        };
        
        JMenuItem menuItem = new JMenuItem("IP Notation");
        menuItem.setActionCommand("IP Notation");
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("CIDR Notation");
        menuItem.setActionCommand("CIDR Notation");
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("VCN by Name");
        menuItem.setActionCommand("VCN by Name");
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        f.add(menu);   
        f.setSize(400,400);  
        f.setLayout(null);  
        f.setVisible(true);  
        menu.show(addAccessControlRuleButton , 0, 0);  
    });
  }


  private void addIPNotationTypeValues(AccessControlType acl) {
    // Add IP notation type and values pair panel to wrapper panel.
    final JPanel outerWrapperPanel = new JPanel();
    outerWrapperPanel.setLayout(new BoxLayout(outerWrapperPanel, BoxLayout.LINE_AXIS));

    // Get a new instance of IP notation type and values pair.
    Consumer<AccessControlRules> deleteFunction = acr -> {
        accessControlRulesList.remove(acr);
        accessControlDynamicPanel.remove(outerWrapperPanel);
        accessControlDynamicPanel.revalidate();
    };

    final AccessControlRules accessControlRules = new AccessControlRules(acl, autonomousDatabaseSummary, deleteFunction);
    accessControlDynamicPanel.revalidate();
    // Keep the newly created instances in a list to read the user selected values.
    accessControlRulesList.add(accessControlRules);
    // callback ignores component event for now; just revalidate
    accessControlRules.valuesUI.addRevalidateCallback(c -> {
        updateErrorInfo(doValidateAll());});

    outerWrapperPanel.add(accessControlRules.getPanel());

    // Now add this wrapper panel to the dynamic panel.
    accessControlDynamicPanel.add(outerWrapperPanel);
    JPanel sepPanel = new JPanel();
    sepPanel.add(new JSeparator());
    accessControlDynamicPanel.add(sepPanel);
    accessControlDynamicPanel.revalidate();
    System.out.println("Size = " + accessControlRulesList.size());
  }

  @Override
  protected void doOKAction() {
    if (secureAccessFromEverywhereRadioButton.isSelected()) {
      final UpdateState curState = 
              new UpdateState(autonomousDatabaseSummary.getWhitelistedIps(), 
                      autonomousDatabaseSummary.getIsMtlsConnectionRequired());
      boolean configureAccess = configureAccessControlRulesCheckBox.isSelected();
      List<String> whitelistIps = null;
      boolean requireMTLS;
      if (configureAccess)
      {
          whitelistIps = computeWhitelistedIps();
          requireMTLS = isMTLSRequired();
      }
      else
      {
          whitelistIps = Collections.emptyList();
          requireMTLS = true;
      }
      final UpdateState newState = new UpdateState(whitelistIps, requireMTLS);
      Updater updater = new Updater(curState, newState);
      updater.execute(autonomousDatabaseSummary);
    } 

    close(OK_EXIT_CODE);
  }

  private boolean isMTLSRequired() {
      return requireMutualTLSMTLSCheckBox.isSelected();
  }

  private List<String> computeWhitelistedIps() {
      List<String> whitelistedIps = new ArrayList<>();

      for (AccessControlRules acr : accessControlRulesList) {
          whitelistedIps.add(acr.getWhitelistedValues());
      }
      
      return whitelistedIps;
  }

  private static class Updater {

    private UpdateState curState;
    private UpdateState newState;

    public Updater(UpdateState curState, UpdateState newState) {
        this.curState = curState;
        this.newState = newState;
    }

    public void execute(AutonomousDatabaseSummary autonomousDatabaseSummary) {
        final Runnable updateMTLS = new Runnable() {
            @Override
            public void run() {
                DatabaseClientProxy databaseClient = OracleCloudAccount.getInstance().getDatabaseClient();
                databaseClient.updateRequiresMTLS(autonomousDatabaseSummary, newState.isMtlsConnectionRequired);
            }
        };
        final List<String> whitelistIpsFinal = newState.whitelistedIps;
        
        final Runnable updateWhitelistIps = new Runnable() {
            @Override
            public void run() {
                DatabaseClientProxy databaseClient = OracleCloudAccount.getInstance().getDatabaseClient();
                databaseClient.updateAcl(autonomousDatabaseSummary, whitelistIpsFinal);
            }
        };
        
        final Runnable refresh = new Runnable() {
            @Override
            public void run() {
                AutonomousDatabasesDashboard.getInstance().populateTableData();
            }
        };
        
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            // if changing from mTLS to single, apply the acl first because the former will
            // fail without first having an ACL
            List<Runnable> todo = new ArrayList<Runnable>();
            if (curState.isMtlsConnectionRequiredChanged(newState))
            {
                if (curState.isAclChanged(newState))
                {
                    if (!curState.isMtlsConnectionRequired)
                    {
                        // if was one way, going to mTLs
                        todo.add(updateMTLS);
                        todo.add(updateWhitelistIps);
                    }
                    else
                    {
                        // cur is mTLS going to one-way.
                        todo.add(updateWhitelistIps);
                        todo.add(updateMTLS);
                    }
                }
                else
                {
                    todo.add(updateMTLS);
                }
            }
            else if (curState.isAclChanged(newState))
            {
                // if mTLS is not changing but acl is.
                todo.add(updateWhitelistIps);
            }
            // only add the refresh if there are other actions.
            if (!todo.isEmpty()) {
                todo.add(refresh);
            }

            for (Runnable r : todo)
            {
                r.run();
                waitForAvailable(autonomousDatabaseSummary, 30000, 5000);
            }
        });
    }

    private void waitForAvailable(AutonomousDatabaseSummary autonomousDatabase, int maxWaitMs, int sleepLengthMs) {
        boolean updating = true;
        POLLING: while (updating)
        {
            AutonomousDatabase databaseInfo = 
                 OracleCloudAccount.getInstance().getDatabaseClient().getDatabaseInfo(autonomousDatabase);
            LifecycleState lifecycleState = databaseInfo.getLifecycleState();
            if (lifecycleState == LifecycleState.Available)
            {
                break POLLING;
            }

            // not there yet.  sleep
            maxWaitMs -= sleepLengthMs;
            if (maxWaitMs <= 0)
            {
                throw new IllegalStateException();
            }
            try {
                Thread.sleep(sleepLengthMs);
            } catch (InterruptedException e) {
                Thread.interrupted();
                throw new IllegalStateException();
            }
        }
    }
  }

  private static class ComboBoxAndChangeCompartmentDuo {
    private final JPanel comboBoxAndChangeCompartmentPanel = new JPanel();
    private final ComboBox<VcnComboWrapper> comboBox = new ComboBox<>();
    private final AccessControlType acl;
    @SuppressWarnings("unused")
    private final String adbCompartmentId;

    public ComboBoxAndChangeCompartmentDuo(AccessControlType acl,
                                            String panelTextPrefix,
                                            String adbCompartmentId,
                                            Function<String, List<Vcn>> getVcnListByNameFn) 
    {
      this.acl = acl;
      this.adbCompartmentId = adbCompartmentId;
      final JPanel comboBoxPanel = new JPanel();
      comboBoxPanel.setLayout(new BoxLayout(comboBoxPanel, BoxLayout.LINE_AXIS));
      comboBoxPanel.add(comboBox);
      //ApplicationManager.getApplication().executeOnPooledThread(() -> {
      final String compartmentName = OracleCloudAccount.getInstance().getIdentityClient()
              .getCompartment(adbCompartmentId).getName();
      final TitledBorder comboBoxPanelTitleBorder = BorderFactory.createTitledBorder(panelTextPrefix + compartmentName);
      comboBoxPanel.setBorder(comboBoxPanelTitleBorder);

      repopulateComboBox(getVcnListByNameFn, SystemPreferences.getCompartmentId());
      if (acl instanceof OcidBasedAccessControlType 
              && ((OcidBasedAccessControlType)acl).getVcn() == null) {
          VcnComboWrapper selectedItem = (VcnComboWrapper) comboBox.getSelectedItem();
          ((OcidBasedAccessControlType)acl).setVcn(selectedItem.getValue());
      }
      //});

      // set listener after we optionally initi
      comboBox.addActionListener(event -> {
          VcnComboWrapper vcnWrapper = (VcnComboWrapper) ((ComboBox<?>)event.getSource()).getSelectedItem();
          ((OcidBasedAccessControlType)acl).setVcn(vcnWrapper.getValue());
      });
      // Now create a button for compartment selection.
      final JButton changeCompartmentButton = new JButton("Change Compartment");
      changeCompartmentButton.addActionListener(e -> {
        final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

        if(compartmentSelection.showAndGet()) {
          final Compartment selectedCompartment = compartmentSelection.getSelectedCompartment();
          repopulateComboBox(getVcnListByNameFn, selectedCompartment.getId());

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

    private void repopulateComboBox(Function<String, List<Vcn>> function, String compartmentId) {
        comboBox.removeAllItems();

        List<Vcn> apply = function.apply(compartmentId);
          Optional.ofNullable(apply).ifPresent(vcns -> {
              for (Vcn vcn : vcns) {
                  comboBox.addItem(new VcnComboWrapper(vcn));
              }
          });
    }

    public JPanel getPanel() {
      return comboBoxAndChangeCompartmentPanel;
    }

    public void populate() {
        Optional<Vcn> vcnOptional = Optional.ofNullable(((OcidBasedAccessControlType)acl).getVcn());
        vcnOptional.ifPresent(vcn -> {
            String displayName = vcn.getDisplayName();
            System.out.printf("Acl.getValue()=%s\n", displayName);
            VcnComboWrapper wrapper = new VcnComboWrapper(vcn);
            this.comboBox.setSelectedItem(wrapper);
            ((OcidBasedAccessControlType)acl).setVcn(vcn);
        });
    }
  }

  /**
   * The UI components of IP notation type and values pair.
   */
  private static class AccessControlRules {
    // the root panel for the ACR
    private final JPanel panel = new JPanel();
    private final AccessControlType acl;

    // Value panels for each type.
    final AbstractValuesUI valuesUI;
    final JPanel valuesPanel = new JPanel();
    private AutonomousDatabaseSummary adb;
    private Consumer<AccessControlRules> removeACL;

    public AccessControlRules(AccessControlType acl, AutonomousDatabaseSummary adb, Consumer<AccessControlRules> removeACL) {
      this.acl = acl;
      this.adb = adb;
      this.removeACL = removeACL;
      this.valuesUI = buildValuesPanel();
      populate(acl);
    }

    public String getWhitelistedValues() {
        return acl.getValue();
    }

    public JPanel getPanel() {
      return panel;
    }

    private AbstractValuesUI buildValuesPanel() {
      // Now create a wrapper panel and add the left and right component panels in it.
      panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

      // Right side component. Wrap this one too in its own panel.
      valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.LINE_AXIS));
      valuesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedSoftBevelBorder(),
              this.acl.getTypeLabel()));
      
      AbstractValuesUI valuesUI;
      switch(this.acl.getType()) {
          case IP:
            valuesUI = new IPNotationValues(acl);
            break;
          case CIDR:
              valuesUI = new CIDRNotationValues(acl);
            break;
          case VCN_BY_OCID:
              valuesUI = new VirtualCloudNetworkValues(acl, this.adb.getCompartmentId());
              break;
          default:
              throw new AssertionError();
      }
      valuesUI.buildPanel();
      valuesPanel.add(valuesUI.getPanel());
      panel.add(valuesPanel);
      
      // Anonymous block to create a namespace.
      {
        final JButton removeButton = new JButton("x");
        removeButton.setToolTipText("Remove Access Control Rule");
        removeButton.addActionListener(e -> {
          this.removeACL.accept(this);
        });
        // Wrap the remove button in a panel.
        final JPanel removePanel = new JPanel();
        BoxLayout layout = new BoxLayout(removePanel, BoxLayout.Y_AXIS);
        
        removePanel.setLayout(layout);
        removePanel.add(removeButton);
        // Add this remove panel the wrapper panel.
        panel.add(removePanel);
      }
      return valuesUI;
    }
    
    private void populate(AccessControlType acl2) {
        valuesUI.populatePanel(acl2);
    }
  }

  private abstract static class AbstractValuesUI {
      private final JPanel panel = new JPanel();
      protected final AccessControlType acl;
    protected Optional<Consumer<ComponentEvent>> revalidateCallback = Optional.empty();

      protected AbstractValuesUI(AccessControlType acl) {
          this.acl = acl;
      }
      
      public abstract void buildPanel();
      public abstract void populatePanel(AccessControlType acl);

      public void addRevalidateCallback(Consumer<ComponentEvent> callback) {
          this.revalidateCallback = Optional.of(callback);;
      }

      protected JPanel getPanel() {
          return panel;
      }
      
      protected @Nullable ValidationInfo validate() {
          return null;
      }
  }

  
  private static class IPNotationValues extends AbstractValuesUI {
      public IPNotationValues(AccessControlType acl) {
        super(acl);
    }

    private final JTextField ipValuesTextField = new JTextField();

    @Override
    public void buildPanel() {
        getPanel().setLayout(new BoxLayout(getPanel(), BoxLayout.X_AXIS));
        getPanel().add(ipValuesTextField);
        ipValuesTextField.addKeyListener(new KeyAdapter() {

          @Override
          public void keyReleased(KeyEvent e) {
              acl.setValue(ipValuesTextField.getText());
               revalidateCallback.ifPresent(c -> { c.accept(e);} );
          }
        });

    }

    @Override
    public void populatePanel(AccessControlType acl) {
        ipValuesTextField.setText(acl.getValue());
    }

    @Override
    protected @Nullable ValidationInfo validate() {
        String valueValid = acl.isValueValid();
        if (valueValid != null) {
            return new ValidationInfo(valueValid, ipValuesTextField);
        }
        return null; // no error
    }
  }
  
  private static class CIDRNotationValues extends AbstractValuesUI {
      protected CIDRNotationValues(AccessControlType acl) {
        super(acl);
  }

  private final JTextField cidrNotationTextField = new JTextField();

    @Override
    public void buildPanel() {
        getPanel().setLayout(new BoxLayout(getPanel(), BoxLayout.X_AXIS));
        getPanel().add(cidrNotationTextField);
        cidrNotationTextField. addKeyListener(new KeyAdapter() {
          @Override
          public void keyReleased(KeyEvent e) {
              acl.setValue(cidrNotationTextField.getText());
              revalidateCallback.ifPresent(c -> { c.accept(e);} );
          }
        });
    }

    @Override
    public void populatePanel(AccessControlType acl) {
        cidrNotationTextField.setText(acl.getValue());
    }
    
    @Override
    protected @Nullable ValidationInfo validate() {
        String valueValid = acl.isValueValid();
        if (valueValid != null) {
            return new ValidationInfo(valueValid, cidrNotationTextField);
        }
        return null; // no error
    }
  }
 
  private static class VirtualCloudNetworkValues extends AbstractValuesUI {
    //private ComboBox<String> virtualCloudNetworkComboBox;
    private final JTextField ipAddressesOrCIDRsOptionalTextField = new JTextField();
    private ComboBoxAndChangeCompartmentDuo vcnComboCompartmentSelectDuo;
    private String adbCompartmentId;

    public VirtualCloudNetworkValues(AccessControlType acl, String adbCompartmentId) {
        super(acl);
        this.adbCompartmentId = adbCompartmentId;
    }

    @Override
    public void buildPanel() {
      this.vcnComboCompartmentSelectDuo =
              new ComboBoxAndChangeCompartmentDuo(
                      this.acl, "Virtual cloud network in ", this.adbCompartmentId, UpdateNetworkAccessDialog::getVcnList);

      // Create a wrapper panel for "IP addresses or CIDRs (Optional)" and wrap text field in it.
      final JPanel ipAddressesOrCidrsOptionalTextFieldPanel = new JPanel();
      ipAddressesOrCidrsOptionalTextFieldPanel.setLayout(new BoxLayout(ipAddressesOrCidrsOptionalTextFieldPanel, BoxLayout.LINE_AXIS));
      ipAddressesOrCidrsOptionalTextFieldPanel.setBorder(BorderFactory.createTitledBorder("Optional IP addresses or CIDRs"));
      ipAddressesOrCidrsOptionalTextFieldPanel.add(ipAddressesOrCIDRsOptionalTextField);
      ipAddressesOrCIDRsOptionalTextField.addKeyListener(new KeyAdapter() {
          @Override
          public void keyReleased(KeyEvent e) {
              ((OcidBasedAccessControlType)acl).setIPList(ipAddressesOrCIDRsOptionalTextField.getText());
              revalidateCallback.ifPresent(c -> { c.accept(e);} );
          }
      });
      getPanel().setLayout(new BoxLayout(getPanel(), BoxLayout.PAGE_AXIS));
      getPanel().add(vcnComboCompartmentSelectDuo.getPanel());
      getPanel().add(ipAddressesOrCidrsOptionalTextFieldPanel);
    }

    @Override
    protected @Nullable ValidationInfo validate() {
        String valueValid = ((OcidBasedAccessControlType)acl).isOptionalIPPartValid();
        if (valueValid != null) {
            return new ValidationInfo(valueValid, ipAddressesOrCIDRsOptionalTextField);
        }
        return null; // no error
    }

    @Override
    public void populatePanel(AccessControlType acl) {
        vcnComboCompartmentSelectDuo.populate();
        ipAddressesOrCIDRsOptionalTextField.setText(((OcidBasedAccessControlType)acl).getIPListAsString());
    }
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    final JBScrollPane jbScrollPane = new JBScrollPane(mainPanel);
    jbScrollPane.setPreferredSize(new Dimension(900, 500));
    doValidateAll();
    return jbScrollPane;
  }
  
  static class UpdateState {
      private List<String> whitelistedIps;
      private boolean isMtlsConnectionRequired;

      public UpdateState(AutonomousDatabaseSummary instance) {
          this(instance.getWhitelistedIps(), 
             instance.getIsMtlsConnectionRequired() != null ? instance.getIsMtlsConnectionRequired() : false);
      }

      public UpdateState(List<String> whitelistIps, boolean isMtlsConnectionRequired) {
          this.whitelistedIps = new ArrayList<>(whitelistIps != null ? whitelistIps : Collections.emptyList());
          Collections.sort(this.whitelistedIps);
          this.isMtlsConnectionRequired = isMtlsConnectionRequired;
      }

      public boolean isAclChanged(UpdateState otherState) {
          if (otherState.whitelistedIps.size() != whitelistedIps.size()) {
              return true;
          }
          for (int i = 0; i < whitelistedIps.size(); i++) {
              String str1 = whitelistedIps.get(i);
              assert str1 != null;
              String str2 = otherState.whitelistedIps.get(i);
              assert str2 != null;
              // neither of these should be null;
              if (!str1.equals(str2)) {
                  return true;
              }
          }
          return false;
      }

      public boolean isMtlsConnectionRequiredChanged(UpdateState otherState) {
          return isMtlsConnectionRequired != otherState.isMtlsConnectionRequired;
      }

      public boolean equals(Object other) {
          if (this == other) {
              return true;
          }

          if (other instanceof UpdateState) {
              UpdateState otherState = (UpdateState) other;
              if (isAclChanged(otherState)) {
                  return false;
              }
              return !isMtlsConnectionRequiredChanged(otherState);
          }
          return false;
      }

      public int hashCode() {
          HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
          hashCodeBuilder.append(isMtlsConnectionRequired);
          hashCodeBuilder.append(whitelistedIps.toArray());
          return hashCodeBuilder.toHashCode();
      }
  }

  //TODO FUTURE:
//  private class PrivateEndpointAccessContent {
//
//  private final ComboBox<String> virtualCloudNetworkComboBox;
//  private final ComboBox<String> subnetComboBox;
//  private final JTextField hostNamePrefixTextField = new JTextField();
//  private final JPanel outerPanel = new JPanel();
//  private final List<ComboBox<String>> networkSecurityGroupsComboBoxList = new ArrayList<>();
//
//  private PrivateEndpointAccessContent() {
//    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.PAGE_AXIS));
//    {
//      final var virtualCloudNetworkAndChangeCompartmentComponent
//              = new ComboBoxAndChangeCompartmentDuo("Virtual cloud network in ", VcnListFunction);
//      virtualCloudNetworkComboBox = virtualCloudNetworkAndChangeCompartmentComponent.getComboBox();
//      outerPanel.add(virtualCloudNetworkAndChangeCompartmentComponent.getPanel());
//    }
//
//    {
//      final var subnetAndChangeCompartmentComponent
//              = new ComboBoxAndChangeCompartmentDuo("Subnet in ", listSubnetsFunction);
//      subnetComboBox = subnetAndChangeCompartmentComponent.getComboBox();
//      outerPanel.add(subnetAndChangeCompartmentComponent.getPanel());
//    }
//
//    {
//      final JPanel hostNamePrefixPanel = new JPanel();
//      hostNamePrefixPanel.setLayout(new BoxLayout(hostNamePrefixPanel, BoxLayout.PAGE_AXIS));
//
//      final TitledBorder hostNamePanelTitleBorder = BorderFactory.createTitledBorder("Host name prefix          Optional");
//      hostNamePrefixPanel.setBorder(hostNamePanelTitleBorder);
//      hostNamePrefixPanel.add(hostNamePrefixTextField);
//      hostNamePrefixTextField.setToolTipText("The name can contain only letters and numbers and a maximum of 63 characters.");
//      outerPanel.add(hostNamePrefixPanel);
//    }
//
//    {
//      final JPanel networkSecurityGroupsPanel = new JPanel();
//      networkSecurityGroupsPanel.setLayout(new BoxLayout(networkSecurityGroupsPanel, BoxLayout.PAGE_AXIS));
//
//      final TitledBorder nsgTitleBorder = BorderFactory.createTitledBorder("Network security groups (NSGs)");
//      networkSecurityGroupsPanel.setBorder(nsgTitleBorder);
//      networkSecurityGroupsPanel.setToolTipText("An NSG has a set of security rules that control allowed types of inbound and outbound traffic.");
//      outerPanel.add(networkSecurityGroupsPanel);
//      initDynamicPanel(networkSecurityGroupsPanel);
//    }
//  }
//
//  private void initDynamicPanel(JPanel networkSecurityGroupsPanel) {
//    final JPanel dynamicPanel = new JPanel();
//    dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.PAGE_AXIS));
//    // Add one panel initially.
//    addNsgAndRemoveButtonPanel(dynamicPanel);
//    networkSecurityGroupsPanel.add(dynamicPanel);
//
//    // The button "+ Another Network Security Group".
//    {
//      final JPanel anotherNetworkSecurityGroupButtonPanel = new JPanel();
//      anotherNetworkSecurityGroupButtonPanel.setLayout(new BoxLayout(anotherNetworkSecurityGroupButtonPanel, BoxLayout.LINE_AXIS));
//      anotherNetworkSecurityGroupButtonPanel.add(new JSeparator());
//      final JButton anotherNetworkSecurityGroupButton = new JButton("+ Another Network Security Group");
//      anotherNetworkSecurityGroupButton.addActionListener(e -> {
//        addNsgAndRemoveButtonPanel(dynamicPanel);
//        dynamicPanel.revalidate();
//      });
//      anotherNetworkSecurityGroupButtonPanel.add(anotherNetworkSecurityGroupButton);
//      networkSecurityGroupsPanel.add(anotherNetworkSecurityGroupButtonPanel);
//    }
//  }
//
//  private void addNsgAndRemoveButtonPanel(JPanel dynamicPanel) {
//    final JPanel nsgAndRemoveButtonPanel = new JPanel();
//    nsgAndRemoveButtonPanel.setLayout(new BoxLayout(nsgAndRemoveButtonPanel, BoxLayout.LINE_AXIS));
//
//    final ComboBoxAndChangeCompartmentDuo networkSecurityGroupsComboBoxAndChangeCompartment
//            = new ComboBoxAndChangeCompartmentDuo("Network Security Groups in ", nsgListFunction);
//    nsgAndRemoveButtonPanel.add(networkSecurityGroupsComboBoxAndChangeCompartment.getPanel());
//    networkSecurityGroupsComboBoxList.add(networkSecurityGroupsComboBoxAndChangeCompartment.getComboBox());
//
//    final JPanel removeButtonPanel = new JPanel();
//    removeButtonPanel.setLayout(new BoxLayout(removeButtonPanel, BoxLayout.PAGE_AXIS));
//    removeButtonPanel.setBorder(BorderFactory.createTitledBorder(" "));
//    final JButton removeButton = new JButton("X");
//    removeButton.setToolTipText("Remove Network Security Group");
//    removeButton.addActionListener(e -> {
//      networkSecurityGroupsComboBoxList.remove(networkSecurityGroupsComboBoxAndChangeCompartment.getComboBox());
//      dynamicPanel.remove(nsgAndRemoveButtonPanel);
//      dynamicPanel.revalidate();
//    });
//    removeButtonPanel.add(removeButton);
//    nsgAndRemoveButtonPanel.add(removeButtonPanel);
//    dynamicPanel.add(nsgAndRemoveButtonPanel);
//  }
//
//  public String getVirtualCloudNetwork() {
//    return (String) virtualCloudNetworkComboBox.getSelectedItem();
//  }
//
//  public String getSubnet() {
//    return (String) subnetComboBox.getSelectedItem();
//  }
//
//  public String getHostNamePrefix() {
//    return hostNamePrefixTextField.getText();
//  }
//
//  public List<String> getNetworkSecurityGroupsComboBoxList() {
//    final List<String> networkSecurityGroupsList = new ArrayList<>();
//    networkSecurityGroupsComboBoxList.forEach(comboBox -> networkSecurityGroupsList.add((String) comboBox.getSelectedItem()));
//    return networkSecurityGroupsList;
//  }
//
//  public JPanel getPanel() {
//    return outerPanel;
//  }
//}
//
//private void initPrivateEndpointAccessDynamicPanel() {
//  privateEndPointAccessPanel.setLayout(new BoxLayout(privateEndPointAccessPanel, BoxLayout.PAGE_AXIS));
//  privateEndpointAccessContent = new PrivateEndpointAccessContent();
//  privateEndPointAccessPanel.add((privateEndpointAccessContent).getPanel());
//}
}
