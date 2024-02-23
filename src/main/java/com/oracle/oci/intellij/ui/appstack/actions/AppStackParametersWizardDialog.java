package com.oracle.oci.intellij.ui.appstack.actions;

import com.intellij.ui.SeparatorComponent;
import com.intellij.ui.components.JBList;
import com.intellij.ui.wizard.WizardDialog;
import com.intellij.ui.wizard.WizardModel;
import com.intellij.ui.wizard.WizardStep;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.appstack.models.Controller;
import com.oracle.oci.intellij.ui.appstack.models.VariableGroup;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class AppStackParametersWizardDialog extends WizardDialog {
    public static  boolean isProgramaticChange = false;
    JBList menuList;
    private LinkedHashMap<String,String> userInput;
    private boolean isCreateStack =false;
    private boolean isApplyJob = false;


    public AppStackParametersWizardDialog(WizardModel wizardModel){
        super(true ,  wizardModel);
    }




    @Override
    protected JComponent createCenterPanel() {
        JComponent wizard = super.createCenterPanel();
        modifyComponents(wizard);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = (JPanel) createMenuPanel();

        if (!UIUtil.isUnderDarcula())
            leftPanel.setBackground(Color.white);


        mainPanel.add(leftPanel,BorderLayout.WEST);
        wizard.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
        mainPanel.add(wizard,BorderLayout.CENTER);
        mainPanel.setMinimumSize(new JBDimension(1090, mainPanel.getHeight()));
        return mainPanel;
    }

    private void modifyComponents(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                LayoutManager layout = panel.getLayout();

                // Check if this is the header panel
                if (layout instanceof BoxLayout) {
                    panel.setBorder(null); // Remove border from header panel
                }else if (layout instanceof BorderLayout){
                   LayoutManager borderLayout =  panel.getLayout();
                    BorderLayout brd = (BorderLayout) borderLayout;
                    brd.setHgap(0);
                    brd.setVgap(0);
                }


                // Continue traversing for nested components
                modifyComponents(panel);
            } else if (comp instanceof SeparatorComponent || comp instanceof Box.Filler) {
                container.remove(comp);
                container.revalidate();
                container.repaint();
            }
        }
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
                        CustomWizardStep currentStep = (CustomWizardStep) appStackModel.getMySteps().get(lastSelectedIndex[0]);
                        boolean isValide =  Controller.getInstance().doValidate(currentStep);
                        currentStep.setDirty(!isValide);

                        nextStep = nextStep != null ? nextStep : appStackModel.getMySteps().get(selectedIndex);
                        if (changeToStep(nextStep,appStackModel)) return;
                        lastSelectedIndex[0] = selectedIndex;
                    }



                    repaint();
                }
            }
        });

        menuList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                CustomWizardStep currentStep = (CustomWizardStep) appStackModel.getMySteps().get(index);
                JLabel renderedLabel = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);


                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setOpaque(true);
//                Color bg = isSelected ? UIUtil.getListSelectionBackground(true) : UIUtil.getListBackground();
//                itemPanel.setBackground(bg);
                itemPanel.setToolTipText((String) value);
//                renderedLabel.setBackground(bg);
//                renderedLabel.setForeground(UIUtil.getListForeground());
                if (currentStep.isDirty()){
                    renderedLabel.setFont(renderedLabel.getFont().deriveFont(1));
                }else {
                    renderedLabel.setFont(renderedLabel.getFont().deriveFont(0));
                }
                renderedLabel.setBorder(JBUI.Borders.emptyLeft(20));
                itemPanel.add(renderedLabel);
                setPreferredSize(new JBDimension(270,30));
//                itemPanel.setPreferredSize(new JBDimension(340,30));

                return itemPanel;
            }
        });
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        leftPanel.add(menuList);

//        leftPanel.setPreferredSize(new JBDimension(320,leftPanel.getHeight()));
//        leftPanel.setBorder(BorderFactory.createTitledBorder("Groups"));

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
        // add cuurent user id , tenency id , region name
        addNeededParameters(variables);


        ReviewDialog reviewDialog = new ReviewDialog(variables,appStackModel.getVarGroups());
        if (reviewDialog.showAndGet()){
            isApplyJob = reviewDialog.isApply();
            isCreateStack = true;
            userInput = variables;
//            createAppStack();
            freeCache();
            System.out.println(variables);
            super.doOKAction();
        }



    }
    public Map<String,String> getUserInput(){
        return userInput;
    }

//    private void createAppStack() {
//        try {
//          OracleCloudAccount.ResourceManagerClientProxy proxy = OracleCloudAccount.getInstance().getResourceManagerClientProxy();
//          String compartmentId = SystemPreferences.getCompartmentId();
//          ClassLoader cl = AppStackDashboard.class.getClassLoader();
//          CreateStackCommand command =
//            new CreateStackCommand(proxy, compartmentId, cl, "appstackforjava.zip");
//            Map<String,String> variables = new ModelLoader().loadTestVariables();
//            command.setVariables(variables);
//          this.dashboard.commandStack.execute(command);
//        } catch (Exception e1) {
//          throw new RuntimeException(e1);
//        }
//    }

    private void addNeededParameters(LinkedHashMap<String, String> variables) {
        String currentUserId =   OracleCloudAccount.getInstance().getCurrentUserId();
        String currentTenancy = OracleCloudAccount.getInstance().getCurrentTenancy();
        String currentRegion = SystemPreferences.getRegionName();

        variables.put("current_user_ocid",currentUserId);
        variables.put("tenancy_ocid",currentTenancy);
        variables.put("region",currentRegion);
        variables.put("compartment_ocid",SystemPreferences.getCompartmentId());
    }

    private void freeCache() {
        // free cache
        CompartmentCache compartmentCache = CompartmentCache.getInstance();
        compartmentCache.setCaching(false);
        compartmentCache.clearCache();
    }


    public boolean isCreateStack() {
        return isCreateStack;
    }
    public boolean isApplyJob() {
        return isApplyJob;
    }
}

