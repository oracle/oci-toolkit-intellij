package com.oracle.oci.intellij.appStackGroup.ui;


import com.oracle.oci.intellij.appStackGroup.models.VariableGroup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;

public class AppStackParametersDialog extends JFrame {
    JScrollPane scrollPane ;
    JPanel mainPanel;

    public AppStackParametersDialog(List<VariableGroup> varGroups, LinkedHashMap<String, LinkedHashMap> variables) throws IntrospectionException {
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

                LinkedHashMap variable = variables.get(pd.getName());
//                pd.setDisplayName(variable.get("title").toString());
//                pd.setShortDescription(variable.get("description").toString());
//                // recheck this default value thing
//                pd.setValue("default",variable.get("default"));
//                pd.setValue("required",variable.get("required"));
//                pd.setValue("enum",variable.get("enum"));
//                pd.setValue("visible",variable.get("visible"));
//                pd.setHidden();



//                Class<?> propertyType = pd.getReadMethod().getAnnotations();

                convertPdToUI(pd,variable,varGroup,groupPanel);


                }

            mainPanel.add(groupPanel);
            JPanel spacer = new JPanel();
            spacer.setBorder(new EmptyBorder(0, 20, 0, 20));
            mainPanel.add(spacer);
        }



        JButton saveButton = new JButton("Save");

        saveButton.addActionListener((e)->{
            System.out.println(varGroups);
            varGroups.forEach(item->{
                System.out.println(item);
            });
            dispose();
        });


        mainPanel.add(saveButton);

        scrollPane = new JScrollPane(mainPanel);
        add(scrollPane);
        pack();
        setVisible(true);
    }



    private void convertPdToUI(PropertyDescriptor pd, LinkedHashMap var,VariableGroup varGroup,JPanel groupPanel) {
        if (pd.getName().equals("class")) {
            return ;
        }


        JLabel label = new JLabel((String) var.get("title"));
        label.setToolTipText((String) var.get("description"));
        JComponent component ;
        Class<?> propertyType = pd.getPropertyType();


        // check if it's visible



        // check if it's a required file
        if (var.get("required") != null) {
            boolean required = (boolean) var.get("required");
            if (required) {
                label.setText(label.getText() + " (*)");
            }
        }


        if (propertyType.getName().equals("boolean")) {


            JCheckBox checkBox = new JCheckBox();
            component = checkBox;


        } else if (propertyType.isEnum()) {


            JComboBox comboBox = new JComboBox();
            List<String> enumValues = (List<String>) var.get("enum");
            for (String enumValue : enumValues) {
                comboBox.addItem(enumValue);
            }
            component = comboBox;


        } else if (propertyType.getName().equals("int")) {



            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);

            // Create the JSpinner with the SpinnerNumberModel
            JSpinner spinner = new JSpinner(spinnerModel);
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

            component = textField;
        }



        groupPanel.add(label);
        groupPanel.add(component);


    }




}














