package com.oracle.oci.intellij.appStackGroup.ui;


import com.google.api.Property;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.Consumer;
import com.intellij.util.ui.JBUI;
import com.oracle.bmc.core.model.Subnet;
import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.identity.model.AvailabilityDomain;
import com.oracle.bmc.keymanagement.model.KeySummary;
import com.oracle.bmc.keymanagement.model.Vault;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.appStackGroup.models.VariableGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AppStackParametersDialog extends DialogWrapper {
    JPanel mainPanel;

    private static final String WINDOW_TITLE = "App stack variables ";
    private static final String OK_TEXT = "Save";



    public AppStackParametersDialog(List<VariableGroup> varGroups,LinkedHashMap<String, PropertyDescriptor> descriptorsState) throws IntrospectionException {
        super(true);
        init();
        setTitle(WINDOW_TITLE);
        setOKButtonText(OK_TEXT);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        createGroupsPanels(varGroups);

    }


    private void createGroupsPanels(List<VariableGroup> varGroups) throws IntrospectionException {
        for (VariableGroup varGroup : varGroups) {
            Class<? extends VariableGroup> varGroupClazz = varGroup.getClass();
            BeanInfo beanInfo = Introspector.getBeanInfo(varGroupClazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

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
                label.setText(label.getText() + " (*)");
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
            checkBox.setSelected((boolean) pd.getValue("default"));
            // add this to the condition || ((String)pd.getValue("type")).startsWith("oci")
        } else if (propertyType.isEnum() ) {
            ComboBox comboBox = new ComboBox();
            List<String> enumValues = (List<String>) pd.getValue("enum");
            if (enumValues != null){
                for (String enumValue : enumValues) {
                    comboBox.addItem(enumValue);
                }
            }else{
                //todo  suggest values from account of user   in a combobox depeding on type
                /* example
                 * oci:identity:compartment:id --> compartments of the user
                 * oci:core:vcn:id --> existed vcn s ...
                 *
                 */

                 enumValues = getSuggestedValues(pd);
                for (String enumValue : enumValues) {
                    comboBox.addItem(enumValue);
                }

            }

            if (pd.getValue("default") != null) {
                comboBox.setSelectedItem(pd.getValue("default"));
            }
            component = comboBox;


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

            component = spinner;
        } else {

            JTextField textField = getjTextField(pd, varGroup);
            if (pd.getValue("default") != null){
                textField.setText(pd.getValue("default").toString());
            }
            component = textField;
        }

        // check if it's visible
        //in progress

        //        if (!visible(pd)) {
        //            label.setVisible(false);
        //            component.setVisible(false);
        //        }


        return component;
    }

    private List<String> getSuggestedValues(PropertyDescriptor pd) {
        String varType = (String) pd.getValue("type");
        return Utils.getSuggestedValuesOf(varType).apply(pd);
    }

    @NotNull
    private static JTextField getjTextField(PropertyDescriptor pd, VariableGroup varGroup) {
        JTextField textField = new JTextField();
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent focusEvent) {
                try {
                    String value = textField.getText();
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
}

class Utils{
    static LinkedHashMap<String,SuggestConsumor<PropertyDescriptor,List<String>>> suggestedValues = new LinkedHashMap<>();
    static {
        suggestedValues.put("oci:identity:compartment:id",(pd)->{
            /* there are :
            * default: ${compartment_id}
            * default: compartment_ocid
             */
            // we have to pop up the compartment selection ....
            return null;
        });

        suggestedValues.put("oci:core:vcn:id",(pd)->{
                String vcn_compartment_id = "null";

                List<Vcn> vcn = OracleCloudAccount.getInstance().getVirtualNetworkClientProxy().listVcns(vcn_compartment_id);

                return vcn.stream().map(Vcn::getDisplayName).collect(Collectors.toList());
        });

        suggestedValues.put("oci:core:subnet:id",(pd)->{
            String vcn_compartment_id="" ;
            String existing_vcn_id ="";
            // todo
            LinkedHashMap dependsOn = (LinkedHashMap) pd.getValue("dependsOn");
            List<Subnet> vcn = OracleCloudAccount.getInstance().getVirtualNetworkClientProxy().listSubnets(vcn_compartment_id,existing_vcn_id);

            return vcn.stream().map(Subnet::getDisplayName).collect(Collectors.toList());

        });

        suggestedValues.put("oci:identity:availabilitydomain:name",(pd)->{
            String compartment_id ="" ;
            List<AvailabilityDomain> availabilityDomains = OracleCloudAccount.getInstance().getIdentityClient().getAvailabilityDomainsList(compartment_id);
            return availabilityDomains.stream().map(AvailabilityDomain::getName).collect(Collectors.toList());
        });

        suggestedValues.put("oci:database:autonomousdatabase:id",(pd)->{
            String compartment_id ="" ;
            List<AutonomousDatabaseSummary> autonomousDatabases = OracleCloudAccount.getInstance().getDatabaseClient().getAutonomousDatabaseList(compartment_id);
            return autonomousDatabases.stream().map(AutonomousDatabaseSummary::getDisplayName).collect(Collectors.toList());

        });

        suggestedValues.put("oci:kms:vault:id",(pd)->{
            String vault_compartment_id = "";

            List<VaultSummary> vaultList = OracleCloudAccount.getInstance().getIdentityClient().getVaultsList(vault_compartment_id);
            return vaultList.stream().map(VaultSummary::getDisplayName).collect(Collectors.toList());
        });

        suggestedValues.put("oci:kms:key:id",(pd)->{
            String vault_compartment_id = "";
            String vault_id = "";

            List<KeySummary> vaultList = OracleCloudAccount.getInstance().getIdentityClient().getKeyList(vault_compartment_id,vault_id);

            return vaultList.stream().map(KeySummary::getDisplayName).collect(Collectors.toList());

        });


    }

    static SuggestConsumor<PropertyDescriptor,List<String>> getSuggestedValuesOf(String type){
        return suggestedValues.get(type);
    }


}
@FunctionalInterface
interface SuggestConsumor<T,  R> {
    R apply(T t);
}