package com.oracle.oci.intellij.ui.appstack.actions;

import com.intellij.ui.components.JBList;
import com.intellij.ui.wizard.WizardDialog;
import com.intellij.ui.wizard.WizardModel;
import com.intellij.ui.wizard.WizardStep;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.oracle.oci.intellij.ui.appstack.models.Controller;
import com.oracle.oci.intellij.ui.appstack.models.VariableGroup;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

public class AppStackParametersWizardDialog extends WizardDialog {
    public static  boolean isProgramaticChange = false;
    JBList menuList;

    public AppStackParametersWizardDialog(WizardModel wizardModel){
        super(true ,  wizardModel);
    }




    @Override
    protected JComponent createCenterPanel() {
        JComponent wizard = super.createCenterPanel();
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = (JPanel) createMenuPanel();

        mainPanel.add(leftPanel,BorderLayout.WEST);
        mainPanel.add(wizard,BorderLayout.EAST);
        mainPanel.setPreferredSize(new JBDimension(1100,780));
        return mainPanel;
    }

    JComponent createMenuPanel(){
        CustomWizardModel appStackModel = (CustomWizardModel) this.myModel;
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

//        List<String> groupList = new ArrayList<>(List.of("db","network"));
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (VariableGroup var :appStackModel.varGroups ){
            listModel.addElement(var.getClass().getSimpleName().replaceAll("_"," "));
        }

        menuList = new JBList<>(listModel);
        appStackModel.setGroupMenuList(menuList);
        isProgramaticChange = true;
        menuList.setSelectedIndex(0);
        isProgramaticChange = false;
        final int[] lastSelectedIndex = {0}; // Initialize with -1 to indicate no selection initially
        menuList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if ( e.getValueIsAdjusting() ) {
                    if(!isProgramaticChange) {
                        int selectedIndex = menuList.getSelectedIndex();
                        WizardStep nextStep = null;
//                        boolean ischangeValide = true;
                        CustomWizardStep currentStep = (CustomWizardStep) appStackModel.getMySteps().get(lastSelectedIndex[0]);
//                        currentStep.
                        boolean isValide =  Controller.getInstance().doValidate(currentStep);
                        currentStep.setDirty(!isValide);
//                        if (nextStep != null) {
//                            ischangeValide = false;
//                        }

                        nextStep = nextStep != null ? nextStep : appStackModel.getMySteps().get(selectedIndex);
                        if (changeToStep(nextStep,appStackModel)) return;
//                        if (!ischangeValide) {
//                            isProgramaticChange = true;
//                            menuList.setSelectedIndex(lastSelectedIndex[0]);
//                            isProgramaticChange = false;
//                        } else {
                            lastSelectedIndex[0] = selectedIndex;
//                        }
                    }

//                    }else {
//                        // put the title menu in not bold
//
//
//                    }

                    repaint();
                }
            }
        });

        menuList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                CustomWizardStep currentStep = (CustomWizardStep) appStackModel.getMySteps().get(index);

                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setOpaque(true);
                Color bg = isSelected ? UIUtil.getListSelectionBackground(true) : UIUtil.getListBackground();
                itemPanel.setBackground(bg);
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                itemPanel.setToolTipText((String) value);
                label.setBackground(bg);
                label.setForeground(UIUtil.getListForeground());
                if (currentStep.isDirty()){
                    label.setFont(label.getFont().deriveFont(1));
                }else {
                    label.setFont(label.getFont().deriveFont(0));
                }
                label.setBorder(JBUI.Borders.emptyLeft(20));
                itemPanel.add(label, "West");
                setPreferredSize(new JBDimension(270,30));
//                itemPanel.setPreferredSize(new JBDimension(340,30));

                return itemPanel;
            }
        });
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        leftPanel.add(menuList);

        leftPanel.setPreferredSize(new JBDimension(320,leftPanel.getHeight()));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Groups"));

        return leftPanel;
    }

    private boolean changeToStep(WizardStep nextStep,CustomWizardModel appStackModel) {
        if (nextStep == WizardStep.FORCED_GOAL_DROPPED) {
            appStackModel.cancel();
            return true;
        }

        if (nextStep == WizardStep.FORCED_GOAL_ACHIEVED) {
            appStackModel.finish();
            return true;
        }

        if (nextStep == null) {
//                    nextStep = getNextFor(getCurrentStep());
            throw new RuntimeException("ex");

        }

        try {
            Method method = WizardModel.class.getDeclaredMethod("changeToStep", WizardStep.class);
            method.setAccessible(true);
            method.invoke(appStackModel, nextStep);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void doCancelAction() {
        freeCache();
        super.doCancelAction();
    }

    @Override
    protected void doOKAction() {
        // create hashMap that contains all the variables and it's value .....

        CustomWizardModel appStackModel = (CustomWizardModel)myModel;
        int stepindex = 0 ;
        for (WizardStep step:
             appStackModel.getMySteps()) {
            CustomWizardStep customWizardStep = (CustomWizardStep) step;
            if (customWizardStep.isDirty()){
                // move to this dirty wizard step  .
                changeToStep(customWizardStep,appStackModel);
                AppStackParametersWizardDialog.isProgramaticChange = true;
                menuList.setSelectedIndex(stepindex);
                AppStackParametersWizardDialog.isProgramaticChange = false;

                return;
            }
            stepindex++;

        }

        LinkedHashMap<String,String> variables = appStackModel.collectVariables();


        ReviewDialog reviewDialog = new ReviewDialog(variables,appStackModel.getVarGroups());
        if (reviewDialog.showAndGet()){
            freeCache();

            System.out.println(variables);
            super.doOKAction();
        }



    }

    private void freeCache() {
        // free cache
        CompartmentCache compartmentCache = CompartmentCache.getInstance();
        compartmentCache.setCaching(false);
        compartmentCache.clearCache();
    }

}

