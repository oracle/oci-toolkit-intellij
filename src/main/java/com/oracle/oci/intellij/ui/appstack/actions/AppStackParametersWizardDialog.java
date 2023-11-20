package com.oracle.oci.intellij.ui.appstack.actions;

import com.intellij.ui.components.JBList;
import com.intellij.ui.wizard.WizardDialog;
import com.intellij.ui.wizard.WizardModel;
import com.intellij.ui.wizard.WizardStep;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.oracle.oci.intellij.ui.appstack.models.VariableGroup;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.lang.reflect.Method;

public class AppStackParametersWizardDialog extends WizardDialog {
    public static  boolean isProgramaticChange = false;

    public AppStackParametersWizardDialog(WizardModel wizardModel){
        super(true ,  wizardModel);
    }




    @Override
    protected JComponent createCenterPanel() {
        JComponent wizard = super.createCenterPanel();
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = (JPanel) createMenuPanel();

        mainPanel.add(leftPanel,BorderLayout.EAST);
        mainPanel.add(wizard,BorderLayout.WEST);
        mainPanel.setPreferredSize(new JBDimension(980,600));
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

        JBList menuList = new JBList<>(listModel);
        appStackModel.setGroupMenuList(menuList);
        isProgramaticChange = true;
        menuList.setSelectedIndex(0);
        isProgramaticChange = false;
        menuList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && !isProgramaticChange) {
                    int selectedIndex = menuList.getSelectedIndex();
                    WizardStep nextStep =  appStackModel.getMySteps().get(selectedIndex);
                    if (nextStep == WizardStep.FORCED_GOAL_DROPPED) {
                        appStackModel.cancel();
                        return;
                    }

                    if (nextStep == WizardStep.FORCED_GOAL_ACHIEVED) {
                        appStackModel.finish();
                        return;
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
                }
            }
        });

        menuList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setOpaque(true);
                Color bg = isSelected ? UIUtil.getListSelectionBackground(true) : UIUtil.getListBackground();
                itemPanel.setBackground(bg);
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBackground(bg);
                label.setForeground(UIUtil.getListForeground());
                label.setFont(label.getFont().deriveFont(1));
                label.setBorder(JBUI.Borders.emptyLeft(20));
                itemPanel.add(label, "West");
                itemPanel.setPreferredSize(new JBDimension(240,30));

                return itemPanel;
            }
        });
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        leftPanel.add(menuList);

        leftPanel.setPreferredSize(new JBDimension(320,leftPanel.getHeight()));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Groups"));

        return leftPanel;
    }



}

