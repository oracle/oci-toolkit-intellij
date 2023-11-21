package com.oracle.oci.intellij.ui.appstack.actions;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.wizard.WizardModel;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.intellij.util.ui.JBDimension;
import com.oracle.bmc.core.model.Subnet;
import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.http.client.internal.ExplicitlySetBmcModel;
import com.oracle.bmc.identity.model.AvailabilityDomain;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.keymanagement.model.KeySummary;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.ui.appstack.models.VariableGroup;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomWizardStep extends WizardStep {
    JBScrollPane mainScrollPane;
    JPanel mainPanel;
    LinkedHashMap <String, JComponent> varPanels= new LinkedHashMap<>();
    static Map<String , JComponent> pdComponents = new LinkedHashMap<>();
    LinkedHashMap<String, PropertyDescriptor> descriptorsState;
//    List <JLabel> errorLabels = new ArrayList<>() ;
    static Map<String , JComponent> errorLabels = new LinkedHashMap<>();


    public static Map<String, VariableGroup> variableGroups ;



    public CustomWizardStep(VariableGroup varGroup, PropertyDescriptor[] propertyDescriptors, LinkedHashMap<String, PropertyDescriptor> descriptorsState, List<VariableGroup> varGroups) {
        mainPanel = new JPanel();
        mainScrollPane = new JBScrollPane(mainPanel);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));


        this.descriptorsState = descriptorsState;

        String className = varGroup.getClass().getSimpleName().replaceAll("_"," ");
        mainPanel.setBorder(BorderFactory.createTitledBorder(className));
//        mainPanel.setLayout(new GridLayout(propertyDescriptors.length, 1));


        for (PropertyDescriptor pd : propertyDescriptors) {
            if (pd.getName().equals("class") || pd.getName().equals("db_compartment")) {
                continue;
            }
            try {
                mainPanel.add(createVarPanel(pd,varGroup))  ;
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private JPanel createVarPanel( PropertyDescriptor pd,VariableGroup variableGroup) throws InvocationTargetException, IllegalAccessException {
        JPanel varPanel = new JPanel(new BorderLayout());
        varPanel.setPreferredSize(new Dimension(760, 60));
        varPanel.setMaximumSize(varPanel.getPreferredSize());


        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(pd.getDisplayName());
        label.setToolTipText( pd.getShortDescription());
        if (pd.getValue("required") != null) {
            boolean required = (boolean) pd.getValue("required");
            if (required) {
                label.setText(label.getText() + " *");
            }
        }
        labelPanel.add(label);
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));


        JPanel componentErrorPanelFlow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel componentErrorPanel = new JPanel();
        componentErrorPanel.setLayout(new BorderLayout());

        JLabel errorLabel = new JLabel();
        errorLabels.put(pd.getName(), errorLabel);
        errorLabel.setVisible(false);
        errorLabel.setForeground(JBColor.RED);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(0,80,0,0));





        JComponent component = createVarComponent(pd,variableGroup,errorLabel);
        componentErrorPanel.add(component,BorderLayout.NORTH);
        componentErrorPanel.add(errorLabel,BorderLayout.SOUTH);

        boolean  isVisible = isVisible((String) pd.getValue("visible"));
        component.setEnabled(isVisible);


        varPanel.add(labelPanel, BorderLayout.WEST);
        componentErrorPanelFlow.add(componentErrorPanel);
        varPanel.add(componentErrorPanelFlow,BorderLayout.EAST);
//        varPanel.setPreferredSize(new JBDimension(660,30));
//        varPanel.setMaximumSize( new JBDimension(660,30));
        return varPanel;
    }

    private  boolean isVisible(String rule) {
        if (rule == null || rule.isEmpty()){
            return true;
        }
        if (rule.startsWith("not(")){
            return !isVisible(rule.substring(4,rule.length()-1));
        }
        if (rule.startsWith("and(")){
            return evaluateAnd(rule.substring(4, rule.lastIndexOf(')')));
        }
        if (rule.startsWith("eq(")){
            String[] parts = rule.substring(3,rule.length()-1).split(",");
            String variable = parts[0];
            String value = parts[1].trim().replaceAll("'","");

            Enum varValue = (Enum) descriptorsState.get(variable).getValue("value");

            return varValue.toString().equals(value);
        }
        boolean varValue = (boolean) descriptorsState.get(rule.trim()).getValue("value");
        return varValue;
    }

    private  boolean evaluateAnd(String rule) {
        int parenCount = 0;
        StringBuilder part = new StringBuilder();
        List<String> parts = new ArrayList<>();

        for (char c : rule.toCharArray()) {
            if (c == '(') {
                parenCount++;
            } else if (c == ')') {
                parenCount--;
            }

            if (c == ',' && parenCount == 0) {
                parts.add(part.toString());
                part = new StringBuilder();
            } else {
                part.append(c);
            }
        }
        parts.add(part.toString()); // Add the last part

        for (String p : parts) {
            if (!isVisible(p.trim())) {
                return false;
            }
        }
        return true;
    }

    private JComponent createVarComponent(PropertyDescriptor pd,VariableGroup varGroup,JLabel errorLabel) throws InvocationTargetException, IllegalAccessException {
        Class<?> propertyType = pd.getPropertyType();
        JComponent component ;

        if (propertyType.getName().equals("boolean")) {

            JCheckBox checkBox = new JCheckBox();
            component = checkBox;

            checkBox.addActionListener(e -> {
                pd.setValue("value",checkBox.isSelected());
                try {
                    pd.getWriteMethod().invoke(varGroup,checkBox.isSelected());
                    updateVisibility(pd,varGroup);

                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }


            });
            checkBox.setSelected( (boolean)(pd.getValue("default")!= null?pd.getValue("default"):true ));
            // add this to the condition || ((String)pd.getValue("type")).startsWith("oci")
        } else if (propertyType.isEnum() || ((String)pd.getValue("type")).startsWith("oci")  ) {

            // if it's an compartment object


            if (pd.getValue("type").equals("oci:identity:compartment:id")){
                JPanel compartmentPanel = new JPanel();
                JButton selectCompartmentBtn  = new JButton("select");
                JTextField compartmentName = new JTextField("");
                compartmentName.setPreferredSize(new Dimension(260,30));
                compartmentName.setEnabled(false);
                compartmentPanel.add(compartmentName);
                compartmentPanel.add(selectCompartmentBtn);
                final CompartmentSelection compartmentSelection = CompartmentSelection.newInstance();

                compartmentSelection.setSelectedCompartment((Compartment) pd.getValue("default"));
                compartmentName.setText(((Compartment) pd.getValue("default")).getName());
                updateDependencies(pd,varGroup);


                selectCompartmentBtn.addActionListener(e->{
                    final CompartmentSelection compartmentSelection1 = CompartmentSelection.newInstance();


                    if (compartmentSelection1.showAndGet()){
                        final Compartment selected = compartmentSelection1.getSelectedCompartment();
                        try {
                            compartmentName.setText(selected.getName());
                            pd.setValue("value",selected);
                            pd.getWriteMethod().invoke(varGroup,selected);
                            updateDependencies(pd, varGroup);

                        } catch (IllegalAccessException | InvocationTargetException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
                pdComponents.put(pd.getName(),compartmentPanel);
                return compartmentPanel;
            }else {

                ComboBox comboBox = new ComboBox();
                AtomicReference<List<String>> enumValues = new AtomicReference<>((List<String>) pd.getValue("enum"));
                if (enumValues.get() != null) {
                    for (String enumValue : enumValues.get()) {
                        Class<?> type = pd.getPropertyType();
                        String normalizedItem =enumValue.trim().replaceAll("[. ]","_");

                        Enum<?> enumValue1 = Enum.valueOf((Class<Enum>) type, normalizedItem);
                        comboBox.addItem(enumValue1);
                    }

                } else {
                    //todo  suggest values from account of user   in a combobox depending on  type
                    /* example
                     * oci:identity:compartment:id --> compartments of the user
                     * oci:core:vcn:id --> existed vcn s ...
                     *
                     */
                    // we need to set a custom renderer
                    List<ExplicitlySetBmcModel> suggestedValues = (List<ExplicitlySetBmcModel>) getSuggestedValues(pd,varGroup);
//                    enumValues.set(getSuggestedValues(pd));


                    comboBox.setRenderer(new DefaultListCellRenderer(){

                        //todo enhance this later
                        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                            if (value instanceof AutonomousDatabaseSummary) {
                                AutonomousDatabaseSummary adb = (AutonomousDatabaseSummary) value;
                                setText(adb.getDisplayName()); // Set the display name of the instance
                            } else if (value instanceof VaultSummary) {
                                VaultSummary adb = (VaultSummary) value;
                                setText(adb.getDisplayName()); // Set the display name of the instance
                            }else if (value instanceof KeySummary) {
                                KeySummary adb = (KeySummary) value;
                                setText(adb.getDisplayName()); // Set the display name of the instance
                            }else if (value instanceof AvailabilityDomain) {
                                AvailabilityDomain adb = (AvailabilityDomain) value;
                                setText(adb.getName()); // Set the display name of the instance
                            }else if (value instanceof Subnet) {
                                Subnet adb = (Subnet) value;
                                setText(adb.getDisplayName()); // Set the display name of the instance
                            }else if (value instanceof Vcn) {
                                Vcn adb = (Vcn) value;
                                setText(adb.getDisplayName()); // Set the display name of the instance
                            }else if (value instanceof Compartment) {
                                Compartment adb = (Compartment) value;
                                setText(adb.getName()); // Set the display name of the instance
                            }
                            return this;
                        }
                    });

                    if (suggestedValues != null) {
                        for (ExplicitlySetBmcModel enumValue : suggestedValues) {
                            comboBox.addItem(enumValue);
                        }
                        if (!suggestedValues.isEmpty()){
                            comboBox.setSelectedItem(suggestedValues.get(0));
                            pd.setValue("value",suggestedValues.get(0));
                            pd.getWriteMethod().invoke(varGroup,suggestedValues.get(0));
                        }

                    }
                    ListCellRenderer res = comboBox.getRenderer();
                    System.out.println(res);

//                    comboBox.addItemListener(e -> {
//                        if (e.getStateChange() == ItemEvent.SELECTED) {
//
//                            pd.setValue("value", comboBox.getSelectedItem());
//                            try {
//                                pd.getWriteMethod().invoke(varGroup,comboBox.getSelectedItem());
//                            } catch (IllegalAccessException | InvocationTargetException ex) {
//                                throw new RuntimeException(ex);
//                            }
//                            updateDependencies(pd, varGroup);
//                            updateVisibility(pd);
//                        }
//
//                    });

                }

                comboBox.addItemListener(e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {

                        pd.setValue("value", comboBox.getSelectedItem());
                        try {
                            pd.getWriteMethod().invoke(varGroup,comboBox.getSelectedItem());
                        } catch (IllegalAccessException | InvocationTargetException ex) {
                            throw new RuntimeException(ex);
                        }
                        updateDependencies(pd, varGroup);
                        updateVisibility(pd,varGroup);
                    }

                });

                if (pd.getValue("default") != null) {
                    comboBox.setSelectedItem(pd.getValue("default"));
                }

                component = comboBox;
            }
        } else if (propertyType.getName().equals("int")) {
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
            JSpinner spinner = new JSpinner(spinnerModel);


            Object value = pd.getValue("default");
            if (value != null){
                if (value instanceof String) {
                    if (((String)value).isEmpty()){
                        value = 0;
                    } else {
                        value = Integer.parseInt((String) value);
                    }
                }
                spinner.setValue(value);
            }
            spinner.addChangeListener(e->{
                try {
                    pd.setValue("value",spinner.getValue());
                    pd.getWriteMethod().invoke(varGroup,spinner.getValue());
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            });


            component = spinner;
        } else {

            JTextField textField = getjTextField(pd, varGroup);
            if (pd.getValue("default") != null){
                textField.setText(pd.getValue("default").toString());
            }
            textField.addFocusListener(new FocusAdapter() {
                                           @Override
                                           public void focusLost(FocusEvent e) {
                                               try {
                                                   pd.setValue("value",textField.getText());
                                                   pd.getWriteMethod().invoke(varGroup,textField.getText());

                                                   if (textField.getText().isEmpty() && pd.getValue("required") != null && (boolean) pd.getValue("required")) {
                                                       textField.setBorder(BorderFactory.createLineBorder(JBColor.RED));
                                                       errorLabel.setVisible(true);
                                                       errorLabel.setText("This field is required");
                                                       return;
                                                   }
                                                   if (pd.getValue("pattern") != null){
                                                       if (!textField.getText().matches((String)pd.getValue("pattern"))) {
                                                           textField.setBorder(BorderFactory.createLineBorder(JBColor.RED));
                                                           errorLabel.setText("invalid format ");
                                                           return;
                                                       }
                                                   }

                                                   errorLabel.setVisible(false);
                                                   textField.setBorder(UIManager.getBorder("TextField.border")); // Reset to default border
                                                   errorLabel.setText("");

                                                   pd.getWriteMethod().invoke(varGroup, textField.getText());
                                               } catch (IllegalAccessException | InvocationTargetException ex) {
                                                   throw new RuntimeException(ex);
                                               }
                                           }
                                       }

            );
            component = textField;
        }
        pdComponents.put(pd.getName(),component);
        component.setPreferredSize(new JBDimension(350,40));


        return component;
    }

    private List<? extends ExplicitlySetBmcModel> getSuggestedValues(PropertyDescriptor pd, VariableGroup varGroup) {
        String varType = (String) pd.getValue("type");
        try {
            return Utils.getSuggestedValuesOf(varType).apply(pd,descriptorsState,varGroup);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateVisibility(PropertyDescriptor pd,VariableGroup variableGroup) {
        List<String> dependencies = Utils.visibilty.get(pd.getName());
        if (dependencies != null) {

            for (String dependency : dependencies) {
                JComponent dependencyComponent  = pdComponents.get(dependency);
                if (dependencyComponent == null) continue;

                PropertyDescriptor dependentPd = descriptorsState.get(dependency);
                boolean isVisible = isVisible((String) dependentPd.getValue("visible"));
                dependencyComponent.setEnabled(isVisible);

                if (dependencyComponent instanceof JPanel){
                    JPanel dependencyComponentP = (JPanel) dependencyComponent;
                    Component[] components = dependencyComponentP.getComponents();
                    for (Component component:
                            components) {
                        if (component instanceof JButton){
                            component.setEnabled(false);
                        }
                    }
                }
                if (!isVisible){
                    // empty the field from it's value

                    JLabel errorLabel = (JLabel) errorLabels.get(dependency);
                    if (errorLabel == null) continue;
                    if (dependencyComponent instanceof JTextField){
                        errorLabel.setVisible(false);
                        dependencyComponent.setBorder(UIManager.getBorder("TextField.border")); // Reset to default border
                        ((JTextField) dependencyComponent).setText("");
                        dependentPd.setValue("value","");
                        String className = dependentPd.getReadMethod().getDeclaringClass().getSimpleName();

                        VariableGroup varGroup = variableGroups.get(className);
                        try {
                            dependentPd.getWriteMethod().invoke(varGroup,"");
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                        errorLabel.setText("");
                    }


                }


            }
        }
    }

    private void updateDependencies(PropertyDescriptor pd, VariableGroup varGroup) {
        List<String> dependencies = Utils.depondsOn.get(pd.getName());
        if (dependencies != null) {
            for (String dependent : dependencies) {
                ComboBox jComboBox = (ComboBox) pdComponents.get(dependent);
                if (jComboBox == null) continue;
                jComboBox.removeAllItems();
                PropertyDescriptor dependentPd = descriptorsState.get(dependent);

                List<? extends ExplicitlySetBmcModel> suggestedvalues = null;
                try {
                    suggestedvalues = Utils.getSuggestedValuesOf((String) dependentPd.getValue("type")).apply(dependentPd, descriptorsState,varGroup);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (suggestedvalues == null) return;
                for (ExplicitlySetBmcModel enumValue : suggestedvalues) {
                    jComboBox.addItem(enumValue);
                }
                if (!suggestedvalues.isEmpty()){
                    jComboBox.setSelectedItem(suggestedvalues.get(0));
                    dependentPd.setValue("value",suggestedvalues.get(0));
                }



            }
        }
    }

    private static JTextField getjTextField(PropertyDescriptor pd, VariableGroup varGroup) {
        JTextField textField = new JTextField();
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                try {
                    String value = textField.getText();
                    pd.setValue("value",value);
                    pd.getWriteMethod().invoke(varGroup, value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return textField;
    }


    @Override
    public JComponent prepare(WizardNavigationState state) {
        return mainScrollPane;
    }

    @Override
    public WizardStep onNext(WizardModel model) {
        CustomWizardModel appStackWizardModel = (CustomWizardModel) model;
        AppStackParametersWizardDialog.isProgramaticChange = true;
        appStackWizardModel.getGroupMenuList().setSelectedIndex(appStackWizardModel.getGroupMenuList().getSelectedIndex()+1);
        AppStackParametersWizardDialog.isProgramaticChange = false;
        return super.onNext(model);
    }

    @Override
    public WizardStep onPrevious(WizardModel model) {
        CustomWizardModel appStackWizardModel = (CustomWizardModel) model;
        AppStackParametersWizardDialog.isProgramaticChange = true;
        appStackWizardModel.getGroupMenuList().setSelectedIndex(appStackWizardModel.getGroupMenuList().getSelectedIndex()-1);
        AppStackParametersWizardDialog.isProgramaticChange = false;

        return super.onPrevious(model);
    }
}

class Utils{
    static LinkedHashMap<String, SuggestConsumor<PropertyDescriptor,LinkedHashMap<String, PropertyDescriptor>,List<? extends ExplicitlySetBmcModel>,VariableGroup>> suggestedValues = new LinkedHashMap<>();
    static {
        suggestedValues.put("oci:identity:compartment:id",(pd,pds,varGroup)->{
            /* there are :
             * default: ${compartment_id}
             * default: compartment_ocid
             */
            // we have to pop up the compartment selection ....
            Compartment rootCompartment = OracleCloudAccount.getInstance().getIdentityClient().getRootCompartment();
            List<Compartment> compartmentList = OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(rootCompartment);

            return compartmentList;
        });

        suggestedValues.put("oci:core:vcn:id",(pd,pds,varGroup)->{
            PropertyDescriptor compartmentPd = pds.get("vcn_compartment_id");
            String vcn_compartment_id ;
            VariableGroup compartmentVarGroup =  CustomWizardStep.variableGroups.get("Network");
            vcn_compartment_id =((Compartment) compartmentPd.getReadMethod().invoke(compartmentVarGroup)).getId();


            List<Vcn> vcn = OracleCloudAccount.getInstance().getVirtualNetworkClientProxy().listVcns(vcn_compartment_id);

            return vcn;
        });

        suggestedValues.put("oci:core:subnet:id",(pd,pds,varGroup)->{
            PropertyDescriptor compartmentPd = pds.get("vcn_compartment_id");
            VariableGroup networkVarGroup =  CustomWizardStep.variableGroups.get("Network");

            String vcn_compartment_id ;
            vcn_compartment_id =((Compartment) compartmentPd.getReadMethod().invoke(networkVarGroup)).getId();
            Vcn vcn = (Vcn) pds.get("existing_vcn_id").getReadMethod().invoke(networkVarGroup);
            if (vcn == null) return null;
            String existing_vcn_id =vcn.getId();;


            // todo
//            LinkedHashMap dependsOn = (LinkedHashMap) pd.getValue("dependsOn");
//            boolean hidePublicSubnet = (boolean) dependsOn.get("hidePublicSubnet");
            boolean hidePublicSubnet = Boolean.parseBoolean(getVaribaleValue("hidePublicSubnet",pd.getValue("dependsOn")));
            List<Subnet> subnets = OracleCloudAccount.getInstance().getVirtualNetworkClientProxy().listSubnets(vcn_compartment_id,existing_vcn_id,hidePublicSubnet);

            return subnets;

        });

        suggestedValues.put("oci:identity:availabilitydomain:name",(pd,pds,varGroup)->{
            VariableGroup general_ConfigurationVarGroup =  CustomWizardStep.variableGroups.get("General_Configuration");

            String compartment_id =( (Compartment)pds.get("compartment_id").getReadMethod().invoke(general_ConfigurationVarGroup)).getId();
            List<AvailabilityDomain> availabilityDomains = OracleCloudAccount.getInstance().getIdentityClient().getAvailabilityDomainsList(compartment_id);
            return availabilityDomains;
        });

        suggestedValues.put("oci:database:autonomousdatabase:id",(pd,pds,varGroup)->{

            VariableGroup general_ConfigurationVarGroup = CustomWizardStep.variableGroups.get("General_Configuration");


            String compartment_id = ( (Compartment)pds.get("compartment_id").getReadMethod().invoke(general_ConfigurationVarGroup)).getId();
            if (compartment_id== null) return null;
            List<AutonomousDatabaseSummary> autonomousDatabases = OracleCloudAccount.getInstance().getDatabaseClient().getAutonomousDatabaseList(compartment_id);
            return autonomousDatabases;

        });

        suggestedValues.put("oci:kms:vault:id",(pd,pds,varGroup)->{
            VariableGroup general_ConfigurationVarGroup =  CustomWizardStep.variableGroups.get("Stack_authentication");

            String vault_compartment_id = ((Compartment) pds.get("vault_compartment_id").getReadMethod().invoke(general_ConfigurationVarGroup)).getId();;

            List<VaultSummary> vaultList = OracleCloudAccount.getInstance().getIdentityClient().getVaultsList(vault_compartment_id);
            return vaultList;
        });

        suggestedValues.put("oci:kms:key:id",(pd,pds,varGroup)->{
            VariableGroup general_ConfigurationVarGroup =  CustomWizardStep.variableGroups.get("Stack_authentication");

            String vault_compartment_id = ( (Compartment) pds.get("vault_compartment_id").getReadMethod().invoke(general_ConfigurationVarGroup)).getId();
            VaultSummary vault =(VaultSummary) pds.get("vault_id").getReadMethod().invoke(varGroup);
            if (vault == null) return null ;


            List<KeySummary> keyList = OracleCloudAccount.getInstance().getIdentityClient().getKeyList(vault_compartment_id,vault);

            return keyList;

        });


    }

    private static String getVaribaleValue(String variableName, Object dependsOn) {
        Pattern pattern = Pattern.compile(variableName+"=([^,}]*)");
        Matcher matcher = pattern.matcher(dependsOn.toString());

        if (matcher.find()){
            return matcher.group(1);
        }
        return "";
    }

    static public Map<String , List<String>> depondsOn = new LinkedHashMap<>(){{
        put("compartment_id", List.of("availability_domain","autonomous_database"));
        put("vault_compartment_id",List.of("vault_id","key_id"));
        put("vault_id",List.of("key_id"));
        put("vcn_compartment_id",List.of("existing_vcn_id","existing_app_subnet_id","existing_db_subnet_id","existing_lb_subnet_id"));
        put("existing_vcn_id",List.of("existing_app_subnet_id","existing_db_subnet_id","existing_lb_subnet_id"));
    }};

    static public Map<String, List<String>> visibilty = new LinkedHashMap<>() {{
        put("application_source", List.of("application_type", "repo_name", "branch", "build_command", "artifact_location", "artifact_id", "registry_id", "image_path", "exposed_port", "use_username_env", "use_password_env", "use_tns_admin_env", "tns_admin_env", "use_default_ssl_configuration", "cert_pem", "private_key_pem", "ca_pem", "vm_options", "program_arguments"));
        put("application_type", List.of("program_arguments", "use_default_ssl_configuration"));
        put("use_existing_database", List.of("autonomous_database_display_name", "autonomous_database_admin_password", "data_storage_size_in_tbs", "cpu_core_count", "ocpu_count", "autonomous_database", "autonomous_database_user", "autonomous_database_password", "use_existing_db_subnet", "db_subnet_cidr"));
        put("use_existing_vault", List.of("new_vault_display_name", "vault_compartment_id", "vault_id", "key_id"));
        put("use_existing_token", List.of("current_user_token"));
        put("use_connection_url_env", List.of("connection_url_env"));
        put("use_username_env", List.of("username_env"));
        put("use_password_env", List.of("password_env"));
        put("use_tns_admin_env", List.of("tns_admin_env"));
        put("use_default_ssl_configuration", List.of("port_property", "keystore_property", "key_alias_property", "keystore_password_property", "keystore_type_property"));
        put("create_fqdn", List.of("dns_compartment", "zone", "subdomain", "certificate_ocid"));
        put("create_new_vcn", List.of("vcn_compartment_id", "existing_vcn_id", "vcn_cidr", "use_existing_app_subnet", "use_existing_db_subnet", "use_existing_lb_subnet"));
        put("use_existing_app_subnet", List.of("existing_app_subnet_id", "app_subnet_cidr"));
        put("use_existing_db_subnet", List.of("existing_db_subnet_id", "db_subnet_cidr"));
        put("use_existing_lb_subnet", List.of("existing_lb_subnet_id", "lb_subnet_cidr"));
        put("use_default_lb_configuration", List.of("maximum_bandwidth_in_mbps", "minimum_bandwidth_in_mbps", "health_checker_url_path", "health_checker_return_code", "enable_session_affinity"));
        put("enable_session_affinity", List.of("session_affinity", "session_affinity_cookie_name"));
    }};

    static SuggestConsumor<PropertyDescriptor,LinkedHashMap<String,PropertyDescriptor> ,List<? extends ExplicitlySetBmcModel>,VariableGroup> getSuggestedValuesOf(String type){
        return suggestedValues.get(type);
    }


}
@FunctionalInterface
interface SuggestConsumor<T,U,R,O> {
    R apply(T t,U u,O o) throws InvocationTargetException, IllegalAccessException;
}

