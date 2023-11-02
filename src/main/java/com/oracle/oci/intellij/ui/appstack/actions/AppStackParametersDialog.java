package com.oracle.oci.intellij.ui.appstack.actions;


import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AppStackParametersDialog extends DialogWrapper {
    JPanel mainPanel;

    private static final String WINDOW_TITLE = "App stack variables ";
    private static final String OK_TEXT = "Save";
    Map<String , JComponent> pdComponents = new LinkedHashMap<>();
    LinkedHashMap<String, PropertyDescriptor> descriptorsState;





    public AppStackParametersDialog(List<VariableGroup> varGroups,LinkedHashMap<String, PropertyDescriptor> descriptorsState) throws IntrospectionException {
        super(true);
        init();
        setTitle(WINDOW_TITLE);
        setOKButtonText(OK_TEXT);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        this.descriptorsState =descriptorsState;
        createGroupsPanels(varGroups);

    }


    private void createGroupsPanels(List<VariableGroup> varGroups) throws IntrospectionException {
        for (VariableGroup varGroup : varGroups) {
            Class<? extends VariableGroup> varGroupClazz = varGroup.getClass();
            BeanInfo beanInfo = Introspector.getBeanInfo(varGroupClazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            Arrays.sort(propertyDescriptors, Comparator.comparingInt(pd -> {
                PropertyOrder annotation = pd.getReadMethod().getAnnotation(PropertyOrder.class);
                return (annotation != null) ? annotation.value() : Integer.MAX_VALUE;
            }));

            // create group panel
            JPanel groupPanel = new JPanel();
            String className = varGroup.getClass().getSimpleName().replaceAll("_"," ");
            groupPanel.setBorder(BorderFactory.createTitledBorder(className));
            groupPanel.setLayout(new GridLayout(0, 2));

            for (PropertyDescriptor pd : propertyDescriptors) {
                if (pd.getName().equals("class")) {
                    continue;
                }
                convertPdToUI(pd,varGroup,groupPanel);
            }

            mainPanel.add(groupPanel);
            JPanel spacer = new JPanel();
            spacer.setBorder(JBUI.Borders.empty(0, 20));
            mainPanel.add(spacer);
        }
    }


    private void convertPdToUI(PropertyDescriptor pd,VariableGroup varGroup,JPanel groupPanel) {


        JLabel label = new JLabel( pd.getDisplayName());
        label.setToolTipText( pd.getShortDescription());
        JComponent component ;

        // check if it's a required file
        if (pd.getValue("required") != null) {
            boolean required = (boolean) pd.getValue("required");
            if (required) {
                label.setText(label.getText() + " *");
            }
        }

        // create component
        component = createComponentVariable(pd, varGroup);


        groupPanel.add(label);
        groupPanel.add(component);


    }

    private JComponent createComponentVariable(PropertyDescriptor pd,VariableGroup varGroup) {

        Class<?> propertyType = pd.getPropertyType();
        JComponent component ;

        if (propertyType.getName().equals("boolean")) {

            JCheckBox checkBox = new JCheckBox();
            component = checkBox;
            checkBox.addActionListener(e -> {
                try {
                    pd.setValue("value",checkBox.isSelected());
                    pd.getWriteMethod().invoke(varGroup,checkBox.isSelected());
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            });
            checkBox.setSelected(Boolean.getBoolean((String) pd.getValue("default")) );
            // add this to the condition || ((String)pd.getValue("type")).startsWith("oci")
        } else if (propertyType.isEnum() || ((String)pd.getValue("type")).startsWith("oci") ) {

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
                updateDependencies(pd);


                selectCompartmentBtn.addActionListener(e->{

                    if (compartmentSelection.showAndGet()){
                        final Compartment selected = compartmentSelection.getSelectedCompartment();
                        try {
                            compartmentName.setText(selected.getName());
                            pd.setValue("value",selected);
                            pd.getWriteMethod().invoke(varGroup,selected);
                            updateDependencies(pd);

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
                        comboBox.addItem(enumValue);
                    }
                } else {
                    //todo  suggest values from account of user   in a combobox depending on  type
                    /* example
                     * oci:identity:compartment:id --> compartments of the user
                     * oci:core:vcn:id --> existed vcn s ...
                     *
                     */
                    // we need to set a custom renderer
                    List<ExplicitlySetBmcModel> suggestedValues = (List<ExplicitlySetBmcModel>) getSuggestedValues(pd);
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
                    }

                    comboBox.addItemListener(e -> {
                        if (e.getStateChange() == ItemEvent.SELECTED) {

                            pd.setValue("value", comboBox.getItem());
                            updateDependencies(pd);
                        }

                    });

                }

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
        component.setPreferredSize(new Dimension(200,30));

        return component;
    }
    void updateDependencies(PropertyDescriptor pd){
        List<String> dependencies = Utils.depondsOn.get(pd.getName());
        if (dependencies != null) {
            for (String dependent : dependencies) {
                ComboBox jComboBox = (ComboBox) pdComponents.get(dependent);
                if (jComboBox == null) continue;
                jComboBox.removeAllItems();
                PropertyDescriptor dependentPd = descriptorsState.get(dependent);

                List<? extends ExplicitlySetBmcModel> suggestedvalues = Utils.getSuggestedValuesOf((String) dependentPd.getValue("type")).apply(dependentPd, descriptorsState);
                if (suggestedvalues == null) return;
                for (ExplicitlySetBmcModel enumValue : suggestedvalues) {
                    jComboBox.addItem(enumValue);
                }
                jComboBox.repaint();

            }
        }
    }

    private List<? extends ExplicitlySetBmcModel> getSuggestedValues(PropertyDescriptor pd) {
        String varType = (String) pd.getValue("type");
        return Utils.getSuggestedValuesOf(varType).apply(pd,descriptorsState);
    }

    @NotNull
    private static JTextField getjTextField(PropertyDescriptor pd, VariableGroup varGroup) {
        JTextField textField = new JTextField();
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent focusEvent) {
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

    private boolean visible(PropertyDescriptor pd) {
        if (pd.getValue("visible") == null) {
            return true;
        }
        if (pd.getValue("visible") instanceof String) {
            // there is just varible
            System.out.println(pd.getValue("visible"));
            return true;
        }


        LinkedHashMap visible = (LinkedHashMap) pd.getValue("visible");
        if (visible.containsKey("and")) {
            if (visible.get("and") instanceof String) {
                // there is just variable
                System.out.println(pd.getValue("and"));
                return true;
            }
            LinkedHashMap andCondition = (LinkedHashMap) visible.get("and");
        }

        return true;
    }


    @Override
    protected @Nullable JComponent createCenterPanel() {
        return new JBScrollPane(mainPanel);
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }
}

class Utils{
    static LinkedHashMap<String,SuggestConsumor<PropertyDescriptor,LinkedHashMap<String, PropertyDescriptor>,List<? extends ExplicitlySetBmcModel>>> suggestedValues = new LinkedHashMap<>();
    static {
        suggestedValues.put("oci:identity:compartment:id",(pd,pds)->{
            /* there are :
            * default: ${compartment_id}
            * default: compartment_ocid
             */
            // we have to pop up the compartment selection ....
            Compartment rootCompartment = OracleCloudAccount.getInstance().getIdentityClient().getRootCompartment();
            List<Compartment> compartmentList = OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(rootCompartment.getCompartmentId());

            return compartmentList;
        });

        suggestedValues.put("oci:core:vcn:id",(pd,pds)->{
                String vcn_compartment_id = ( (Compartment)pds.get("vcn_compartment_id").getValue("value")).getId();

                List<Vcn> vcn = OracleCloudAccount.getInstance().getVirtualNetworkClientProxy().listVcns(vcn_compartment_id);

                return vcn;
        });

        suggestedValues.put("oci:core:subnet:id",(pd,pds)->{
            String vcn_compartment_id=( (Compartment)pds.get("vcn_compartment_id").getValue("value")).getId();
            Vcn vcn = (Vcn) pds.get("existing_vcn_id").getValue("value");
            if (vcn == null) return null;
            String existing_vcn_id =vcn.getId();;


            // todo
            LinkedHashMap dependsOn = (LinkedHashMap) pd.getValue("dependsOn");
            boolean hidePublicSubnet = (boolean) dependsOn.get("hidePublicSubnet");
            List<Subnet> subnets = OracleCloudAccount.getInstance().getVirtualNetworkClientProxy().listSubnets(vcn_compartment_id,existing_vcn_id,hidePublicSubnet);

            return subnets;

        });

        suggestedValues.put("oci:identity:availabilitydomain:name",(pd,pds)->{
            String compartment_id =( (Compartment)pds.get("compartment_id").getValue("value")).getId();  ;
            List<AvailabilityDomain> availabilityDomains = OracleCloudAccount.getInstance().getIdentityClient().getAvailabilityDomainsList(compartment_id);
            return availabilityDomains;
        });

        suggestedValues.put("oci:database:autonomousdatabase:id",(pd,pds)->{

            String compartment_id = ( (Compartment)pds.get("compartment_id").getValue("value")).getId();
            if (compartment_id== null) return null;
            List<AutonomousDatabaseSummary> autonomousDatabases = OracleCloudAccount.getInstance().getDatabaseClient().getAutonomousDatabaseList(compartment_id);
            return autonomousDatabases;

        });

        suggestedValues.put("oci:kms:vault:id",(pd,pds)->{
            String vault_compartment_id = ( (Compartment) pds.get("vault_compartment_id").getValue("value")).getId();;

            List<VaultSummary> vaultList = OracleCloudAccount.getInstance().getIdentityClient().getVaultsList(vault_compartment_id);
            return vaultList;
        });

        suggestedValues.put("oci:kms:key:id",(pd,pds)->{
            String vault_compartment_id = ( (Compartment) pds.get("vault_compartment_id").getValue("value")).getId();
            VaultSummary vault =(VaultSummary) pds.get("vault_id").getValue("value");
            if (vault == null) return null ;
            String vault_id = vault.getId();
            List<KeySummary> keyList = OracleCloudAccount.getInstance().getIdentityClient().getKeyList(vault_compartment_id,vault);

            return keyList;

        });


    }

    static public Map<String , List<String>> depondsOn = new LinkedHashMap<>(){{
        put("compartment_id", List.of("availability_domain","autonomous_database"));
        put("vault_compartment_id",List.of("vault_id","key_id"));
        put("vault_id",List.of("key_id"));
        put("vcn_compartment_id",List.of("existing_vcn_id","existing_app_subnet_id","existing_db_subnet_id","existing_lb_subnet_id"));
        put("existing_vcn_id",List.of("existing_app_subnet_id","existing_db_subnet_id","existing_lb_subnet_id"));
    }};

    static SuggestConsumor<PropertyDescriptor,LinkedHashMap<String,PropertyDescriptor> ,List<? extends ExplicitlySetBmcModel>> getSuggestedValuesOf(String type){
        return suggestedValues.get(type);
    }


}
@FunctionalInterface
interface SuggestConsumor<T,U,R> {
    R apply(T t,U u);
}



