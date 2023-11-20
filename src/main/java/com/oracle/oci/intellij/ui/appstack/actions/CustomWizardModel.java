package com.oracle.oci.intellij.ui.appstack.actions;

import com.intellij.ui.components.JBList;
import com.intellij.ui.wizard.WizardModel;
import com.intellij.ui.wizard.WizardStep;

import com.oracle.oci.intellij.ui.appstack.models.VariableGroup;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;

public class CustomWizardModel extends WizardModel {
    private final List<WizardStep> mySteps = new ArrayList<>();
    private  JBList<String> groupMenuList = new JBList<>() ;


    List<VariableGroup> varGroups;
    LinkedHashMap<String, PropertyDescriptor> descriptorsState;

    public CustomWizardModel( List<VariableGroup> varGroups, LinkedHashMap<String, PropertyDescriptor> descriptorsState) throws IntrospectionException {
        super("App Stack Variable");
        this.varGroups = varGroups;
        this.descriptorsState = descriptorsState;



        // create the wizard steps
        initWizardSteps();
    }

    public JBList<String> getGroupMenuList() {
        return groupMenuList;
    }

    public void setGroupMenuList(JBList<String> groupMenuList) {
        this.groupMenuList = groupMenuList;
    }

    private void initWizardSteps() throws IntrospectionException {
        for (VariableGroup varGroup : varGroups) {

            Class<? extends VariableGroup> varGroupClazz = varGroup.getClass();
            BeanInfo beanInfo = Introspector.getBeanInfo(varGroupClazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            Arrays.sort(propertyDescriptors, Comparator.comparingInt(pd -> {
                PropertyOrder annotation = pd.getReadMethod().getAnnotation(PropertyOrder.class);
                return (annotation != null) ? annotation.value() : Integer.MAX_VALUE;
            }));

            // create first  wizard step
            CustomWizardStep varWizardStep = new CustomWizardStep(varGroup, propertyDescriptors, descriptorsState, varGroups);
            mySteps.add(varWizardStep);
            add(varWizardStep);

        }
    }



    public List<WizardStep> getMySteps(){
        return mySteps;
    }
}
