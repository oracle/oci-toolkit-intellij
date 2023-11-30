package com.oracle.oci.intellij.ui.appstack.models;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.wizard.WizardStep;
import com.oracle.bmc.http.client.internal.ExplicitlySetBmcModel;
import com.oracle.oci.intellij.ui.appstack.actions.CustomWizardStep;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    LinkedHashMap<String, PropertyDescriptor> descriptorsState;
    Map<String , JComponent> pdComponents = new LinkedHashMap<>();
    Map<String , JComponent> errorLabels = new LinkedHashMap<>();
    Map<String, VariableGroup> variableGroups ;
    private static Controller instance ;

    public LinkedHashMap<String, PropertyDescriptor> getDescriptorsState() {
        return descriptorsState;
    }

    public void setDescriptorsState(LinkedHashMap<String, PropertyDescriptor> descriptorsState) {
        this.descriptorsState = descriptorsState;
    }

    public Map<String, JComponent> getPdComponents() {
        return pdComponents;
    }

    public void setPdComponents(Map<String, JComponent> pdComponents) {
        this.pdComponents = pdComponents;
    }

    public Map<String, JComponent> getErrorLabels() {
        return errorLabels;
    }

    public void setErrorLabels(Map<String, JComponent> errorLabels) {
        this.errorLabels = errorLabels;
    }

    public Map<String, VariableGroup> getVariableGroups() {
        return variableGroups;
    }

    public void setVariableGroups(Map<String, VariableGroup> variableGroups) {
        this.variableGroups = variableGroups;
    }

    public static Controller getInstance() {
        if (instance == null){
            instance = new Controller();
        }
        return instance;
    }



    public void updateDependencies(String pdName, VariableGroup varGroup){
        PropertyDescriptor pd = descriptorsState.get(pdName);
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
    public void updateVisibility(String pdName,VariableGroup variableGroup){
        PropertyDescriptor pd = descriptorsState.get(pdName);
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
                            component.setEnabled(isVisible);
                        }
                    }
                    continue;
                }
                if (!isVisible){

                    // empty the error labels
                    JLabel errorLabel = (JLabel) errorLabels.get(dependency);
                    if (errorLabel == null) continue;
                    errorLabel.setVisible(false);
                    dependencyComponent.setBorder(UIManager.getBorder("TextField.border")); // Reset to default border
                    errorLabel.setText("");
                    // empty the value
                    if (dependencyComponent instanceof JTextField){
                        ((JTextField) dependencyComponent).setText("");
                        dependentPd.setValue("value","");
                        String className = dependentPd.getReadMethod().getDeclaringClass().getSimpleName();

                        VariableGroup varGroup = variableGroups.get(className);
                        try {
                            dependentPd.getWriteMethod().invoke(varGroup,"");
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }


                }


            }
        }
    }
    public boolean isVisible(String rule) {
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
    public WizardStep doValidate(WizardStep wizardStep){
        CustomWizardStep cWizardStep = (CustomWizardStep)wizardStep;
        boolean isvalide = true ;
        JComponent errorComponent = null;
        PropertyDescriptor errorPd = null;
        for (PropertyDescriptor pd:
                cWizardStep.getStepPropertyDescriptors()) {
            if (pdComponents.get(pd.getName()).isEnabled() && (boolean)pd.getValue("required")){
                String className = pd.getReadMethod().getDeclaringClass().getSimpleName();

                VariableGroup varGroup = variableGroups.get(className);
                Object value = null;
                try {
                    value  = pd.getReadMethod().invoke(varGroup);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                if (value== null || value.equals("")){
                    errorPd = pd;
                    errorComponent = pdComponents.get(pd.getName());
                    isvalide = false;
                    break;
                }
            }
        }
        if (!isvalide){
            errorComponent.grabFocus();
            errorComponent.requestFocusInWindow();
            JLabel errorLabel = (JLabel) errorLabels.get(errorPd.getName());
            errorLabel.setVisible(true);
            errorLabel.setText("This field is required");
            errorComponent.setBorder(BorderFactory.createLineBorder(JBColor.RED));
            return cWizardStep;
        }
        return null;
    }

    public VariableGroup getVariableGroup(PropertyDescriptor pd) {
        String className = pd.getReadMethod().getDeclaringClass().getSimpleName();

        return variableGroups.get(className);
    }

    public List<? extends ExplicitlySetBmcModel> getSuggestedValues(PropertyDescriptor pd, VariableGroup varGroup) {
        String varType = (String) pd.getValue("type");
        try {
            return Utils.getSuggestedValuesOf(varType).apply(pd,descriptorsState,varGroup);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
