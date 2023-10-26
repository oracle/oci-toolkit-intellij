package com.oracle.oci.intellij.appStackGroup.ui;


import com.oracle.oci.intellij.appStackGroup.models.VariableGroup;

import javax.annotation.Nullable;
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

public class AppStackParametersDialog extends JFrame {
    JScrollPane scrollPane ;
    JPanel mainPanel;

    public AppStackParametersDialog(List<VariableGroup> varGroups,LinkedHashMap<String, PropertyDescriptor> descriptorsState) throws IntrospectionException {
        super("AppStack Properties");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(940, 800);
        setLayout(new GridLayout(0, 1));

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));




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
//

                convertPdToUI(pd,varGroup,groupPanel);


            }

            mainPanel.add(groupPanel);
            JPanel spacer = new JPanel();
            spacer.setBorder(new EmptyBorder(0, 20, 0, 20));
            mainPanel.add(spacer);
        }



        JButton saveButton = new JButton("Save");

        saveButton.addActionListener((e)->{
            System.out.println(varGroups);
            dispose();
        });


        mainPanel.add(saveButton);

        scrollPane = new JScrollPane(mainPanel);
        add(scrollPane);
        pack();
        setVisible(true);
    }



    private void convertPdToUI(PropertyDescriptor pd,VariableGroup varGroup,JPanel groupPanel) {
        if (pd.getName().equals("class")) {
            return ;
        }


        JLabel label = new JLabel( pd.getDisplayName());
        label.setToolTipText( pd.getShortDescription());
        JComponent component ;
        Class<?> propertyType = pd.getPropertyType();





        // check if it's a required file
        if (pd.getValue("required") != null) {
            boolean required = (boolean) pd.getValue("required");
            if (required) {
                label.setText(label.getText() + " (*)");
            }
        }



        // create component

        if (propertyType.getName().equals("boolean")) {


            JCheckBox checkBox = new JCheckBox();
            component = checkBox;
            checkBox.setSelected((boolean) pd.getValue("default"));


        } else if (propertyType.isEnum()) {


            JComboBox comboBox = new JComboBox();
            List<String> enumValues = (List<String>) pd.getValue("enum");
            for (String enumValue : enumValues) {
                comboBox.addItem(enumValue);
            }
            comboBox.setSelectedItem(pd.getValue("default"));
            component = comboBox;


        } else if (propertyType.getName().equals("int")) {



            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);

            // Create the JSpinner with the SpinnerNumberModel
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


            JTextField textField = new JTextField();
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent focusEvent) {
                    try {
                        String value = textField.getText();
                        pd.getWriteMethod().invoke(varGroup, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
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




        groupPanel.add(label);
        groupPanel.add(component);


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
                // there is just varible
                System.out.println(pd.getValue("and"));
                return true;
            }
            LinkedHashMap andCondition = (LinkedHashMap) visible.get("and");
        }
        return true;
    }


}