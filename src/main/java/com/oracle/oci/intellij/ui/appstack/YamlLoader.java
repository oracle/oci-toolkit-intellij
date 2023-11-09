package com.oracle.oci.intellij.ui.appstack;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.appstack.actions.AppStackParametersDialog;
import com.oracle.oci.intellij.ui.appstack.annotations.VariableMetaData;
import com.oracle.oci.intellij.ui.appstack.models.*;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YamlLoader {

    static LinkedHashMap<String, PropertyDescriptor> descriptorsState = new LinkedHashMap<>();

    public static void Load() throws StreamReadException, DatabindException, IOException, IntrospectionException, InvocationTargetException, IllegalAccessException {
//        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
//        LinkedHashMap readValue =
//                mapper.readValue(new File("/Users/aallali/Desktop/working/oci-toolkit-repo/oci-intellij-plugin/src/main/resources/interface.yaml"), LinkedHashMap.class);

//        LinkedHashMap variables = (LinkedHashMap) readValue.get("variables");


//        List<LinkedHashMap> groups = (List<LinkedHashMap>) readValue.get("variableGroups");








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




//        mapToDescriptors(variables,varGroups);

        for (VariableGroup varGroup:varGroups){
            Class<?> appVarGroupClazz = varGroup.getClass();
            BeanInfo beanInfo = Introspector.getBeanInfo(appVarGroupClazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            // create Map from group instance ....
            Map <VariableGroup ,Map<String,Object> > GroupMetadata;
//            LinkedHashMap<String, PropertyDescriptor> descriptorsState = new LinkedHashMap<>();


            for (PropertyDescriptor pd:propertyDescriptors){

                VariableMetaData annotation = pd.getReadMethod().getAnnotation(VariableMetaData.class);
                System.out.println(annotation);
                VariableMetaData metaData = annotation;

//            Map varMetaData = new LinkedHashMap();
//            varMetaData.put("title",metaData.title());
//            varMetaData.put("description",metaData.description());
//            varMetaData.put("required",metaData.required());
//            varMetaData.put("type",metaData.type());
//            varMetaData.put("default",metaData.defaultVal());
//            varMetaData.put("dependsOn",metaData.dependsOn());
//            varMetaData.put("visible",metaData.visible());

                if (pd.getName().equals("class")) {
                    continue;
                }
                int pdOrder = 0;
//                pd.getPropertyType()

                pd.setDisplayName((metaData.title() != null)? metaData.title() : "");
                pd.setShortDescription((metaData.description() != null) ? metaData.description() :  "" );
//                // recheck this default value thing
//                if (metaData.defaultVal() != null) {
//                    pd.setValue("default", metaData.defaultVal());
//                }

                if (metaData.defaultVal() != null) {
                    // fill default variables ....
                    Object defaultValue =  getDefaultValue(pd,metaData);
                    pd.setValue("default", defaultValue);
                    pd.setValue("value",defaultValue);
//                    pd.getWriteMethod().invoke(group,defaultValue);
                }

                if (metaData.defaultVal() != null) {
                    pd.setValue("type", metaData.type());
                }

                pd.setValue("required", metaData.required());

                if (metaData.enumValues() != null) {
                    List<String> list = getEnumList(metaData.enumValues());
                    pd.setValue("enum", list);
                }
                if (metaData.visible() != null) {
                    pd.setValue("visible", metaData.visible());
                }

                descriptorsState.put(pd.getName(),pd);



            }
        }
        createUIForm(varGroups,descriptorsState);






    }

    private static List<String> getEnumList(String enums) {
        String [] items = enums.replaceAll("\\[\\]","").split(",");
        return List.of(items);
    }
//
//    static void mapToDescriptors (LinkedHashMap<String, LinkedHashMap> variables,List<VariableGroup> groups) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
//        for (VariableGroup group : groups) {
//            Class<? extends VariableGroup> varGroupClazz = group.getClass();
//            BeanInfo beanInfo = Introspector.getBeanInfo(varGroupClazz);
//            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
//
//            for (PropertyDescriptor pd : propertyDescriptors) {
//                if (pd.getName().equals("class")) {
//                    continue;
//                }
//                LinkedHashMap variable = variables.get(pd.getName());
//                pd.setDisplayName((variable.get("title") != null)? variable.get("title").toString() : "");
//                pd.setShortDescription((variable.get("description") != null) ? variable.get("description").toString() : "" );
//
//                //                // recheck this default value thing
//                if (variable.get("default") != null) {
//                    // fill default variables ....
//                   Object defaultValue =  getDefaultValue(pd,);
//                    pd.setValue("default", defaultValue);
//                    pd.setValue("value",defaultValue);
////                    pd.getWriteMethod().invoke(group,defaultValue);
//                }
//                if (variable.get("type") != null) {
//                    pd.setValue("type", variable.get("type"));
//                }
//                if (variable.get("dependsOn") != null) {
//                    pd.setValue("dependsOn", variable.get("dependsOn"));
//                }
//                if (variable.get("required") != null) {
//                    pd.setValue("required", variable.get("required"));
//                }
//                if (variable.get("enum") != null) {
//                    pd.setValue("enum", variable.get("enum"));
//                }
//                if (variable.get("visible") != null) {
//                    pd.setValue("visible", variable.get("visible"));
////                    isVisible(pd);
//                }else {
//                    pd.setHidden(false);
//                }
//
//                // visible logic
//
//
//                descriptorsState.put(pd.getName(),pd);
//            }
//        }
//    }
//
//    private static boolean isVisible(PropertyDescriptor pd) {
//        Object rules = pd.getValue("visible");
//        if (rules instanceof String){
//            System.out.println(rules);
//        }else {
//            System.out.println("hi");
//        }
//        Map operation = (LinkedHashMap)rules;
//       if (operation.get("eq") != null){
//           List<String> eqParam = (ArrayList<String>) operation.get("eq");
//           isVisible(eqParam,"eq");
//       }else if (operation.get("and") != null){
//           isVisible(operation.get("and"),"and");
//       } else if (operation.get("not") != null) {
//           isVisible(operation.get("not"),"not");
//
//       }
//
//        return true;
//    }
//
//    static boolean isVisible(Object rule){
//
//            if (rule instanceof String) {
//                // Rule is a variable name
//                String variableName = (String) rule;
//                System.out.println(rule);
//                return true;
//            } else if (rule instanceof List) {
//                List<?> ruleList = (List<?>) rule;
////                Object operation =  ruleList.get(0);
//
//
//                 if ("eq".equals(op)) {
//                    // Equality operation
//                    String variableName = (String) ruleList.get(1);
//                    Object expectedValue = ruleList.get(2);
//                    return descriptorsState.containsKey(variableName) && descriptorsState.get(variableName).equals(expectedValue);
//                } else if ("and".equals(op)) {
//                    // Logical AND operation
//                    for (int i = 1; i < ruleList.size(); i++) {
//                        if (!isVisible(ruleList.get(i),"and")) {
//                            return false;
//                        }
//                    }
//                    return true;
//                } else if ("not".equals(op)) {
//                    // Logical NOT operation
//                     ruleList.get(0);
//                    return !isVisible(ruleList.get(1),"not");
//                }
//            }
//            // Invalid rule format
//            throw new IllegalArgumentException("Invalid rule format: " + rule);
//
//    }

    private static Object getDefaultValue(PropertyDescriptor pd,VariableMetaData metaData ) {
        if (metaData.defaultVal().contains("compartment_ocid") || metaData.defaultVal().contains("compartment_id"))
            return OracleCloudAccount.getInstance().getIdentityClient().getCompartment(SystemPreferences.getCompartmentId());
        return metaData.defaultVal();
    }



    public static void createUIForm(List<VariableGroup> varGroups,LinkedHashMap<String, PropertyDescriptor> descriptorsState) throws IntrospectionException {
        AppStackParametersDialog dialog =new AppStackParametersDialog(varGroups,descriptorsState) ;
        dialog.showAndGet();
    }
    private static void generateVariableGroup(LinkedHashMap<String, Object> group, LinkedHashMap<String, Object> varMetadatas) throws IOException {
        String title = (String) group.get("title");
        List<String> variables = (List<String>) group.get("variables");
        String className = title.replaceAll(" ", "_");

        StringBuilder builder = new StringBuilder();
        builder.append("package org.example.appStackGroup.models;\n");
        builder.append("public class ");
        builder.append(className);
        builder.append(" extends VariableGroup {\n");
        builder.append("    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);\n\n");

        variables.forEach(var ->
        {
            Map<String, Object> varMetadata = (Map<String,Object>) varMetadatas.get(var);
            String javaFromYamlType = "java.lang.String"; //default
            String body = "";
            if (varMetadata != null) {
                String yamltype = (String) varMetadata.get("type");
                if (yamltype != null) {
                    javaFromYamlType = mapType(yamltype);
                    if(javaFromYamlType.equals("enum")) {

                        StringBuilder enumBuilder = new StringBuilder();
                        enumBuilder.append("{\n");
                        List<String> enumValues = (List<String>) varMetadata.get("enum");
                        for (String enumValue : enumValues) {
                            enumBuilder.append("        ");
                            enumBuilder.append(enumValue);
                            enumBuilder.append(",\n");
                        }
                        enumBuilder.append("    }\n\n");
                        body = enumBuilder.toString();
                    }
                }
            }
            builder.append(String.format("    private %s ", javaFromYamlType));
            builder.append(var);
            builder.append(body);
            builder.append(";\n\n");
        });

        builder.append("    public void addPropertyChangeListener(PropertyChangeListener listener) {\n" +
                "         this.pcs.addPropertyChangeListener(listener);\n" +
                "     }\n" +
                "\n" +
                "    public void removePropertyChangeListener(PropertyChangeListener listener) {\n" +
                "         this.pcs.removePropertyChangeListener(listener);\n" +
                "     }\n");


        builder.append("}");
        convertToJavaFile(builder, className);
        System.out.println(builder.toString());
    }

    static void convertToJavaFile(StringBuilder content, String className) throws IOException {
        FileWriter fw = new FileWriter("src/main/java/org/example/appStackGroup/models/"+className+".java");
        fw.write(content.toString());
        fw.close();
    }
    private static String mapType(String yamlType) {
        switch(yamlType) {
            case "string":
                return "java.lang.String";
            case "boolean":
                return "boolean";
            case "number":
                return "int";
            case "enum":
                return "enum";
            default:
                return "java.lang.Object";
        }
    }

}


