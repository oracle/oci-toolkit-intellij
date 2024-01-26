package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.oci.intellij.ui.appstack.actions.CustomWizardStep;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

public class Validator implements VetoableChangeListener {
    private static Validator INSTANCE;
    private static Controller controller = Controller.getInstance();



    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        String propertyName = evt.getPropertyName();
        Object newValue = evt.getNewValue();
        CustomWizardStep.VarPanel varPanel = controller.getVarPanelByName(propertyName);
        PropertyDescriptor pd = varPanel.getPd();
        doValidate(pd,newValue,evt);
        //todo check type of the variable , then validate depending on the type ....
        //todo then update the view by showing the error


    }
    public static void doValidate(PropertyDescriptor pd,Object newValue,PropertyChangeEvent evt) throws PropertyVetoException {
        String pdType = (String) pd.getValue("type");
        if (pd.getValue("required") != null && pd.getValue("required").equals(true) && newValue == null) throw new PropertyVetoException("This field is required",evt);
        if(pdType.equals("string") || pdType.equals("password")){
            validateString(pd,(String)newValue,evt);
        }else if(pdType.equals("number")) {
            validateNumber(pd,(int)newValue,evt);
        }else if (pdType.startsWith("oci")){
            validateOciResource(pd,newValue,evt);
        }
    }

    private static void validateOciResource(PropertyDescriptor pd, Object newValue, PropertyChangeEvent evt) throws PropertyVetoException {
        if (newValue == null || newValue instanceof String){
            throw new PropertyVetoException("This field is required",evt);
        }
    }

    private static void validateString(PropertyDescriptor pd, String newValue,PropertyChangeEvent evt) throws PropertyVetoException {
        String pattern = (String) pd.getValue("pattern");

        if ( pd.getValue("required") != null && pd.getValue("required").equals(true) && newValue.trim().isEmpty()){
            throw new PropertyVetoException("This field is required",evt);
        }
        if (pattern != null && !pattern.isEmpty()) {
            if (!newValue.matches(pattern)) {
                throw new PropertyVetoException("Invalid input", evt);
            }
        }
    }
    private static void validateNumber(PropertyDescriptor pd, int newValue,PropertyChangeEvent evt) throws PropertyVetoException {
        if (newValue == 0){
            throw new PropertyVetoException("this field can't be 0",evt);
         }
    }
}
