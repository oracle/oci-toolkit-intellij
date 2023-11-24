package com.oracle.oci.intellij.ui.appstack;



import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.appstack.actions.AppStackParametersWizardDialog;
import com.oracle.oci.intellij.ui.appstack.actions.CustomWizardModel;
import com.oracle.oci.intellij.ui.appstack.actions.CustomWizardStep;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;
import com.oracle.oci.intellij.ui.appstack.models.*;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


public class YamlLoader {
    static List<VariableGroup> varGroups;
      final ExecutorService executorService = Executors.newSingleThreadExecutor();
      Compartment compartment ;

    public  void load() throws IntrospectionException, InvocationTargetException, IllegalAccessException {

        varGroups = init();
        LinkedHashMap<String, PropertyDescriptor> descriptorsState = new LinkedHashMap<>();


        CustomWizardStep.variableGroups = new LinkedHashMap<>();
        for (VariableGroup appVarGroup : varGroups){
            CustomWizardStep.variableGroups.put(appVarGroup.getClass().getSimpleName(),appVarGroup);

            Class<?> appVarGroupClazz = appVarGroup.getClass();
            BeanInfo beanInfo = Introspector.getBeanInfo(appVarGroupClazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();



            for (PropertyDescriptor pd:propertyDescriptors){

                VariableMetaData annotation = pd.getReadMethod().getAnnotation(VariableMetaData.class);

                if (pd.getName().equals("class") || annotation == null ) {
                    continue;
                }


                pd.setDisplayName((annotation.title() != null)? annotation.title() : "");
                pd.setShortDescription((annotation.description() != null) ? annotation.description() :  "" );
                //                // recheck this default value thing
                if (annotation.defaultVal() != null  && !annotation.defaultVal().isEmpty()) {

                    Object defaultValue =  getDefaultValue(pd, annotation);
                    System.out.println(pd.getName());
                    pd.setValue("default", defaultValue);

                    pd.setValue("value",defaultValue);
                    pd.getWriteMethod().invoke(appVarGroup,defaultValue);
                }
                if (annotation.dependsOn() != null && !annotation.dependsOn().isEmpty()) {
                    pd.setValue("dependsOn",annotation.dependsOn());
                }

                pd.setValue("required", annotation.required());

                if (annotation.enumValues() != null) {
                    if (!annotation.enumValues().isEmpty()){
                        List<String> list = getEnumList(annotation.enumValues());
                        pd.setValue("enum", list);
                    }            }
                if (annotation.visible() != null) {
                    if (!annotation.visible().isEmpty()){
                        pd.setValue("visible", annotation.visible());
                    }
                }
                if (annotation.defaultVal() != null) {
                    pd.setValue("type", annotation.type());
                }

                descriptorsState.put(pd.getName(),pd);



            }







        }
        CustomWizardModel customWizardModel = new CustomWizardModel(varGroups,descriptorsState);
        AppStackParametersWizardDialog dialog = new AppStackParametersWizardDialog(customWizardModel);
        dialog.show();
//        JPanel mainPanel = new JPanel(new BorderLayout());
//        JPanel leftPanel = new JPanel();
//        JLabel label = new JLabel("hii");
//        JLabel label1 = new JLabel("hii");
//
//        JLabel label22 = new JLabel("hii");
//
//        leftPanel.add(label1);
//        leftPanel.add(label);
//        leftPanel.add(label22);
//
//
//        mainPanel.add(leftPanel,BorderLayout.EAST);
//        mainPanel.add(dialog.getContentPanel(),BorderLayout.WEST);
//        // create new panel
//        DialogWrapper dialogWrapper = new DialogWrapper(true) {
//            @Override
//            protected @Nullable JComponent createCenterPanel() {
//                return mainPanel;
//            }
//        };
//        dialogWrapper.show();


    }

    private  List<String> getEnumList(String enums) {
        String [] items = enums.replaceAll("\\[\\]","").split(",");
        return List.of(items);
    }

    private  Object getDefaultValue(PropertyDescriptor pd,VariableMetaData metaData )  {


        if (metaData.defaultVal().contains("compartment_ocid") || metaData.defaultVal().contains("compartment_id")) {
            if (compartment == null)
                return compartment = OracleCloudAccount.getInstance().getIdentityClient().getCompartment(SystemPreferences.getCompartmentId());
            return compartment;
        }
        if (metaData.type().equals("enum")){
            Class<?> type = pd.getPropertyType();
            String normalizedItem = metaData.defaultVal().trim().replaceAll("\\.","_");
            Enum<?> enumValue = Enum.valueOf((Class<Enum>) type, normalizedItem);
            return enumValue;
        }
        if (metaData.type().equals("boolean")){
            return Boolean.parseBoolean(metaData.defaultVal());
        }
        if (metaData.type().equals("number")){
            return Integer.parseInt(metaData.defaultVal());
        }
        return metaData.defaultVal();
    }

    private static List<VariableGroup> init() {
        List<VariableGroup> varGroups = new ArrayList<>();
        varGroups.add(new General_Configuration());
        varGroups.add(new Application());
        varGroups.add(new Stack_authentication());
        varGroups.add(new Application_Performance_Monitoring());
        varGroups.add(new Database());
        varGroups.add(new Other_parameters());
        varGroups.add(new Application_configuration_SSL_communication_between_backends_and_load_balancer());
        varGroups.add(new Application_URL());
        varGroups.add(new Network());
        varGroups.add(new Container_instance_configuration());
        return varGroups;
    }
}
