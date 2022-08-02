package com.oracle.oci.intellij.ui.database.actions;

import static com.oracle.oci.intellij.ui.database.model.CIDRBlockType.CIDR_BLOCK_PATTERN;

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
import com.oracle.oci.intellij.util.LogHandler;

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

    this.moreInformationHyperlinkLabel.setText("<html><a href=''>More information (opens browser link)</a></html>");
    this.moreInformationHyperlinkLabel.addMouseListener(new MouseAdapter() {
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
    buttonGroup.add(this.secureAccessFromEverywhereRadioButton);
    //buttonGroup.add(privateEndpointAccessOnlyRadioButton);

    // Defaults.
    this.secureAccessFromEverywhereRadioButton.setSelected(true);
    this.privateEndPointAccessPanel.setVisible(false);

    // Toggle the sub panels based on the radio button selected.
    this.secureAccessFromEverywhereRadioButton.addActionListener(event -> {
      this.configureAccessControlRulesPanel.setVisible(true);
      this.privateEndPointAccessPanel.setVisible(false);
    });

    // Toggle the sub panels based on the radio button selected.
    this.privateEndpointAccessOnlyRadioButton.addActionListener(event -> {
      this.configureAccessControlRulesPanel.setVisible(false);
      this.privateEndPointAccessPanel.setVisible(true);
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
      boolean networkAccessConfigured = this.configureAccessControlRulesCheckBox.isSelected();
      updateAclUpdate(networkAccessConfigured);
    });
    Optional<Boolean> isMtlsConnectionRequired = 
        Optional.ofNullable(this.autonomousDatabaseSummary.getIsMtlsConnectionRequired());
    isMtlsConnectionRequired.
        ifPresentOrElse(
            isRequired -> this.requireMutualTLSMTLSCheckBox.setSelected(isRequired.booleanValue()),
            new Runnable() { public void run() {LogHandler.error("Is Mtls Connection Required is null");}}); //$NON-NLS-1$
    
    if (aclEnabled) {
      System.out.println("aclEnabled"); //$NON-NLS-1$
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

  private static AccessControlType parseAcl(String aclStr) {
    if (aclStr.startsWith("ocid")) //$NON-NLS-1$
    {
      OcidBasedAccessControlType ocidAcl = OcidBasedAccessControlType.parseOcidAcl(aclStr);
      ocidAcl.setVcn(getVcn(ocidAcl.getOcid()));
      return ocidAcl;
    }
    Matcher matcher = IPAddressType.IP_ADDR_PATTERN.matcher(aclStr);
    if (matcher.matches()) {
      return new IPAddressType(aclStr);
    }
    matcher = CIDR_BLOCK_PATTERN.matcher(aclStr);
    if (matcher.matches()) {
      return new CIDRBlockType(aclStr);
    }
    return new UnknownAccessControlType(aclStr);
  }

  private void updateAclUpdate(boolean enabled) {
    System.out.println(enabled);
    recurseComponents(this.accessTypeSelectionPanel, enabled);
    recurseComponents(this.configureAccessControlRulesPanel, enabled);
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
    this.accessControlDynamicPanel.setLayout(new BoxLayout(this.accessControlDynamicPanel, BoxLayout.PAGE_AXIS));

    // The action handler of this button should add a new IP notation type and values pair to dynamic panel.
    this.addAccessControlRuleButton.addActionListener(event -> {
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
                
                default:
                    throw new UnsupportedOperationException("Unsupported action: "+e.getActionCommand());
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
        menu.show(this.addAccessControlRuleButton , 0, 0);  
    });
  }


  private void addIPNotationTypeValues(AccessControlType acl) {
    // Add IP notation type and values pair panel to wrapper panel.
    final JPanel outerWrapperPanel = new JPanel();
    outerWrapperPanel.setLayout(new BoxLayout(outerWrapperPanel, BoxLayout.LINE_AXIS));

    // Get a new instance of IP notation type and values pair.
    Consumer<AccessControlRules> deleteFunction = acr -> {
        this.accessControlRulesList.remove(acr);
        this.accessControlDynamicPanel.remove(outerWrapperPanel);
        this.accessControlDynamicPanel.revalidate();
    };

    final AccessControlRules accessControlRules = new AccessControlRules(acl, this.autonomousDatabaseSummary, deleteFunction);
    this.accessControlDynamicPanel.revalidate();
    // Keep the newly created instances in a list to read the user selected values.
    this.accessControlRulesList.add(accessControlRules);
    // callback ignores component event for now; just revalidate
    accessControlRules.valuesUI.addRevalidateCallback(c -> {
        updateErrorInfo(doValidateAll());});

    outerWrapperPanel.add(accessControlRules.getPanel());

    // Now add this wrapper panel to the dynamic panel.
    this.accessControlDynamicPanel.add(outerWrapperPanel);
    JPanel sepPanel = new JPanel();
    sepPanel.add(new JSeparator());
    this.accessControlDynamicPanel.add(sepPanel);
    this.accessControlDynamicPanel.revalidate();
    System.out.println("Size = " + this.accessControlRulesList.size()); //$NON-NLS-1$
  }

  @Override
  protected void doOKAction() {
    if (this.secureAccessFromEverywhereRadioButton.isSelected()) {
      Boolean isMtlsConnectionRequired = this.autonomousDatabaseSummary.getIsMtlsConnectionRequired();
      if (isMtlsConnectionRequired == null) {
        isMtlsConnectionRequired = Boolean.FALSE;
      }

      final UpdateState curState = new UpdateState(this.autonomousDatabaseSummary.getWhitelistedIps(), 
                                                   isMtlsConnectionRequired.booleanValue());

      boolean configureAccess = this.configureAccessControlRulesCheckBox.isSelected();
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
      updater.execute(this.autonomousDatabaseSummary);
    } 

    close(OK_EXIT_CODE);
  }

  private boolean isMTLSRequired() {
      return this.requireMutualTLSMTLSCheckBox.isSelected();
  }

  private List<String> computeWhitelistedIps() {
      List<String> whitelistedIps = new ArrayList<>();

      for (AccessControlRules acr : this.accessControlRulesList) {
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
                databaseClient.updateRequiresMTLS(autonomousDatabaseSummary, Updater.this.newState.isMtlsConnectionRequired);
            }
        };
        final List<String> whitelistIpsFinal = this.newState.whitelistedIps;
        
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
            if (this.curState.isMtlsConnectionRequiredChanged(this.newState))
            {
                if (this.curState.isAclChanged(this.newState))
                {
                    if (!this.curState.isMtlsConnectionRequired)
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
            else if (this.curState.isAclChanged(this.newState))
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

    private static void waitForAvailable(AutonomousDatabaseSummary autonomousDatabase, final int maxWaitMs, final int sleepLengthMs) {
      int remainingMaxWaitMs = maxWaitMs;  
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
            remainingMaxWaitMs -= sleepLengthMs;
            if (remainingMaxWaitMs <= 0)
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
      comboBoxPanel.add(this.comboBox);
      //ApplicationManager.getApplication().executeOnPooledThread(() -> {
      final String compartmentName = OracleCloudAccount.getInstance().getIdentityClient()
              .getCompartment(adbCompartmentId).getName();
      final TitledBorder comboBoxPanelTitleBorder = BorderFactory.createTitledBorder(panelTextPrefix + compartmentName);
      comboBoxPanel.setBorder(comboBoxPanelTitleBorder);

      repopulateComboBox(getVcnListByNameFn, SystemPreferences.getCompartmentId());
      if (acl instanceof OcidBasedAccessControlType 
              && ((OcidBasedAccessControlType)acl).getVcn() == null) {
          VcnComboWrapper selectedItem = (VcnComboWrapper) this.comboBox.getSelectedItem();
          ((OcidBasedAccessControlType)acl).setVcn(selectedItem.getValue());
      }
      //});

      // set listener after we optionally initi
      this.comboBox.addActionListener(event -> {
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
      changeCompartmentPanel.setBorder(BorderFactory.createTitledBorder(" ")); //$NON-NLS-1$
      changeCompartmentPanel.add(changeCompartmentButton);

      // Now wrap the panels "Virtual cloud network in " and "Change Compartment" in a new panel.
      this.comboBoxAndChangeCompartmentPanel.setLayout(new BoxLayout(this.comboBoxAndChangeCompartmentPanel, BoxLayout.LINE_AXIS));
      this.comboBoxAndChangeCompartmentPanel.add(comboBoxPanel);
      this.comboBoxAndChangeCompartmentPanel.add(changeCompartmentPanel);
    }

    private void repopulateComboBox(Function<String, List<Vcn>> function, String compartmentId) {
        this.comboBox.removeAllItems();

        List<Vcn> apply = function.apply(compartmentId);
          Optional.ofNullable(apply).ifPresent(vcns -> {
              for (Vcn vcn : vcns) {
                  this.comboBox.addItem(new VcnComboWrapper(vcn));
              }
          });
    }

    public JPanel getPanel() {
      return this.comboBoxAndChangeCompartmentPanel;
    }

    public void populate() {
        Optional<Vcn> vcnOptional = Optional.ofNullable(((OcidBasedAccessControlType)this.acl).getVcn());
        vcnOptional.ifPresent(vcn -> {
            String displayName = vcn.getDisplayName();
            System.out.printf("Acl.getValue()=%s\n", displayName);
            VcnComboWrapper wrapper = new VcnComboWrapper(vcn);
            this.comboBox.setSelectedItem(wrapper);
            ((OcidBasedAccessControlType)this.acl).setVcn(vcn);
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
        return this.acl.getValue();
    }

    public JPanel getPanel() {
      return this.panel;
    }

    private AbstractValuesUI buildValuesPanel() {
      // Now create a wrapper panel and add the left and right component panels in it.
      this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.LINE_AXIS));

      // Right side component. Wrap this one too in its own panel.
      this.valuesPanel.setLayout(new BoxLayout(this.valuesPanel, BoxLayout.LINE_AXIS));
      this.valuesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedSoftBevelBorder(),
              this.acl.getTypeLabel()));
      
      AbstractValuesUI valuesUI;
      switch(this.acl.getType()) {
          case IP:
            valuesUI = new IPNotationValues(this.acl);
            break;
          case CIDR:
              valuesUI = new CIDRNotationValues(this.acl);
            break;
          case VCN_BY_OCID:
              valuesUI = new VirtualCloudNetworkValues(this.acl, this.adb.getCompartmentId());
              break;
          default:
              throw new AssertionError();
      }
      valuesUI.buildPanel();
      this.valuesPanel.add(valuesUI.getPanel());
      this.panel.add(this.valuesPanel);
      
      // Anonymous block to create a namespace.
      {
        final JButton removeButton = new JButton("x"); //$NON-NLS-1$
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
        this.panel.add(removePanel);
      }
      return valuesUI;
    }
    
    private void populate(AccessControlType acl2) {
        this.valuesUI.populatePanel(acl2);
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
          this.revalidateCallback = Optional.of(callback);
      }

      protected JPanel getPanel() {
          return this.panel;
      }
      
      /**
       * @return a validation info if there are issues or null if no issues.
       */
      @SuppressWarnings("static-method")
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
        getPanel().add(this.ipValuesTextField);
        this.ipValuesTextField.addKeyListener(new KeyAdapter() {

          @Override
          public void keyReleased(KeyEvent e) {
              IPNotationValues.this.acl.setValue(IPNotationValues.this.ipValuesTextField.getText());
               IPNotationValues.this.revalidateCallback.ifPresent(c -> { c.accept(e);} );
          }
        });

    }

    @Override
    public void populatePanel(AccessControlType acl) {
        this.ipValuesTextField.setText(acl.getValue());
    }

    @Override
    protected @Nullable ValidationInfo validate() {
        String valueValid = this.acl.isValueValid();
        if (valueValid != null) {
            return new ValidationInfo(valueValid, this.ipValuesTextField);
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
        getPanel().add(this.cidrNotationTextField);
        this.cidrNotationTextField. addKeyListener(new KeyAdapter() {
          @Override
          public void keyReleased(KeyEvent e) {
              CIDRNotationValues.this.acl.setValue(CIDRNotationValues.this.cidrNotationTextField.getText());
              CIDRNotationValues.this.revalidateCallback.ifPresent(c -> { c.accept(e);} );
          }
        });
    }

    @Override
    public void populatePanel(AccessControlType acl) {
        this.cidrNotationTextField.setText(acl.getValue());
    }
    
    @Override
    protected @Nullable ValidationInfo validate() {
        String valueValid = this.acl.isValueValid();
        if (valueValid != null) {
            return new ValidationInfo(valueValid, this.cidrNotationTextField);
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
      ipAddressesOrCidrsOptionalTextFieldPanel.add(this.ipAddressesOrCIDRsOptionalTextField);
      this.ipAddressesOrCIDRsOptionalTextField.addKeyListener(new KeyAdapter() {
          @Override
          public void keyReleased(KeyEvent e) {
              ((OcidBasedAccessControlType)VirtualCloudNetworkValues.this.acl).setIPList(VirtualCloudNetworkValues.this.ipAddressesOrCIDRsOptionalTextField.getText());
              VirtualCloudNetworkValues.this.revalidateCallback.ifPresent(c -> { c.accept(e);} );
          }
      });
      getPanel().setLayout(new BoxLayout(getPanel(), BoxLayout.PAGE_AXIS));
      getPanel().add(this.vcnComboCompartmentSelectDuo.getPanel());
      getPanel().add(ipAddressesOrCidrsOptionalTextFieldPanel);
    }

    @Override
    protected @Nullable ValidationInfo validate() {
        String valueValid = ((OcidBasedAccessControlType)this.acl).isOptionalIPPartValid();
        if (valueValid != null) {
            return new ValidationInfo(valueValid, this.ipAddressesOrCIDRsOptionalTextField);
        }
        return null; // no error
    }

    @Override
    public void populatePanel(AccessControlType acl) {
        this.vcnComboCompartmentSelectDuo.populate();
        this.ipAddressesOrCIDRsOptionalTextField.setText(((OcidBasedAccessControlType)acl).getIPListAsString());
    }
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    final JBScrollPane jbScrollPane = new JBScrollPane(this.mainPanel);
    jbScrollPane.setPreferredSize(new Dimension(900, 500));
    doValidateAll();
    return jbScrollPane;
  }
  
  static class UpdateState {
      private List<String> whitelistedIps;
      private boolean isMtlsConnectionRequired;

      public UpdateState(AutonomousDatabaseSummary instance) {
          this(instance.getWhitelistedIps(), 
             instance.getIsMtlsConnectionRequired() != null ? instance.getIsMtlsConnectionRequired().booleanValue() : false);
      }

      public UpdateState(List<String> whitelistIps, boolean isMtlsConnectionRequired) {
          this.whitelistedIps = new ArrayList<>(whitelistIps != null ? whitelistIps : Collections.emptyList());
          Collections.sort(this.whitelistedIps);
          this.isMtlsConnectionRequired = isMtlsConnectionRequired;
      }

      public boolean isAclChanged(UpdateState otherState) {
          if (otherState.whitelistedIps.size() != this.whitelistedIps.size()) {
              return true;
          }
          for (int i = 0; i < this.whitelistedIps.size(); i++) {
              String str1 = this.whitelistedIps.get(i);
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
          return this.isMtlsConnectionRequired != otherState.isMtlsConnectionRequired;
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
          hashCodeBuilder.append(this.isMtlsConnectionRequired);
          hashCodeBuilder.append(this.whitelistedIps.toArray());
          return hashCodeBuilder.toHashCode();
      }
  }
}
