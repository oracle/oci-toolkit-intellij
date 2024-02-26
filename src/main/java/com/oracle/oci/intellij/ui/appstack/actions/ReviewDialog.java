package com.oracle.oci.intellij.ui.appstack.actions;


import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBDimension;
import com.oracle.oci.intellij.ui.appstack.models.Controller;
import com.oracle.oci.intellij.ui.appstack.models.VariableGroup;
import com.oracle.oci.intellij.ui.common.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReviewDialog extends DialogWrapper {
    LinkedHashMap<String,String> variables;
    JPanel mainPanel ;
    JBScrollPane mainScrollPane;
    Controller controller = Controller.getInstance();
    JBCheckBox isApplyCheckBox ;
    private boolean isShowStackVariables ;




    public ReviewDialog(Map<String, String> variables, List<VariableGroup> varGroups,boolean isShowStackVariables ) {
        super(false);
        this.isShowStackVariables = isShowStackVariables;
        setTitle("Oracle Cloud Infrastructure Configuration");
        setOKButtonText("Create");
        mainPanel = new JPanel();
//        mainPanel.setPreferredSize(new JBDimension(800,800));

        mainScrollPane = new JBScrollPane(mainPanel);

        this.variables = (LinkedHashMap<String, String>) variables;
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
        final PropertyDescriptor[][] pds = new PropertyDescriptor[1][1];
        varGroups.forEach(varGroup->{
            try {
                JPanel groupPanel = new JPanel();
                String className = varGroup.getClass().getSimpleName().replaceAll("_"," ");
                TitledBorder titledBorder = BorderFactory.createTitledBorder(className);
                titledBorder.setTitleFont(titledBorder.getTitleFont().deriveFont(Font.BOLD));
                groupPanel.setBorder(titledBorder);
                groupPanel.setLayout(new BoxLayout(groupPanel,BoxLayout.Y_AXIS));

                pds[0] = controller.getSortedProertyDescriptorsByVarGroup(varGroup);

                for (PropertyDescriptor pd : pds[0]) {
                    if (pd.getName().equals("class")  || !variables.containsKey(pd.getName())) {
                        continue;
                    }
                    ReviewVarPanel varPanel ;
                    varPanel = new ReviewVarPanel(pd,variables.get(pd.getName()));
                    groupPanel.add(varPanel);
                }

                mainPanel.add(groupPanel);
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        });





        // todo create the panel of  the check if user wants to immediately
        if (!isShowStackVariables){
            addRunApplyCheckbox();
        }
        init();

    }

    private void addRunApplyCheckbox() {
        JPanel isApplyPanel = new JPanel(new BorderLayout());
        JBLabel isApplyLable = new JBLabel("Run apply on the created stack?\n");
        isApplyLable.setToolTipText("Immediately provision the resources defined in the Terraform configuration by running the apply action on the new stack.\n" +
                "\n");
        isApplyCheckBox = new JBCheckBox("Run Apply");
        isApplyCheckBox.setSelected(true);
        isApplyPanel.add(isApplyLable,BorderLayout.WEST);
        isApplyPanel.add(isApplyCheckBox,BorderLayout.CENTER);


        mainPanel.add(isApplyPanel);
    }

    public boolean isApply() {
        return isApplyCheckBox.isSelected();
    }

    @Override
    protected Action @NotNull [] createActions() {
        if (isShowStackVariables){
            getCancelAction().putValue("Name","Close");
            return new Action[]{getCancelAction()};
        }
        return super.createActions();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return mainScrollPane;
    }

    class ReviewVarPanel extends JPanel {
        JLabel keyLabel;
        JLabel valueLabel;

        ReviewVarPanel(PropertyDescriptor pd, String value){
            setLayout(new BorderLayout());

            if (pd.getValue("type").equals("password")){
                value = "****";
            }

            this.keyLabel = new JBLabel(pd.getDisplayName()+" : ");
            keyLabel.setToolTipText(pd.getShortDescription());
            this.valueLabel = new JLabel(value);
            this.keyLabel.setPreferredSize(new JBDimension(300,20));

            setBorder(BorderFactory.createEmptyBorder(0,8,0,0));

            add(keyLabel,BorderLayout.WEST);
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttonsPanel.add(valueLabel);


            String fullText = value;
            if (fullText.length()>=30 && ((String)pd.getValue("type")).contains("oci")){
                String showIconPath = Icons.SHOW.getPath();
                String hideIconPath = Icons.HIDE.getPath();


                int start = fullText.length() - 9;
                String truncatedText = "..."+ fullText.substring(start,fullText.length()) ;
                valueLabel.setText(truncatedText);

                JButton toggleButton = new JButton(IconLoader.getIcon(showIconPath));
                toggleButton.setToolTipText("Show");

                toggleButton.setBackground(null);
                toggleButton.setBorder(null);
                toggleButton.setOpaque(false);
                toggleButton.setContentAreaFilled(false); //
                toggleButton.setPreferredSize(new JBDimension(20,20));

                toggleButton.addActionListener(new ActionListener() {
                    private boolean isFullTextShown = false;  // Start with the full text hidden

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isFullTextShown) {
                            valueLabel.setText(truncatedText);
                            toggleButton.setIcon(IconLoader.getIcon(showIconPath));
                            toggleButton.setToolTipText("Show");

                            isFullTextShown = false;
                        } else {
                            valueLabel.setText(fullText);
                            toggleButton.setIcon(IconLoader.getIcon(hideIconPath));
                            toggleButton.setToolTipText("Hide");

                            isFullTextShown = true;
                        }

                    }
                });
                buttonsPanel.add(toggleButton);
            }


            if (pd.getValue("type").toString().contains("oci")){
                String copyPath = Icons.COPY.getPath();

                // Create the button and set the icon
                JButton copyButton = new JButton(IconLoader.getIcon(copyPath));
                copyButton.setBackground(null);
                copyButton.setPreferredSize(new JBDimension(20,20));
                copyButton.setBorder(null);
                String finalValue = value;
                copyButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String textToCopy = finalValue;  // Replace with the actual text you want to copy
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        StringSelection selection = new StringSelection(textToCopy);
                        clipboard.setContents(selection, selection);
                        copyButton.setIcon(null);
                        copyButton.setToolTipText("Copy");
                        copyButton.setOpaque(false);
                        copyButton.setContentAreaFilled(false); //
//                        copyButton.setText("copied");
                        // notify
                        new Timer(500,ev->{
                            copyButton.setIcon(IconLoader.getIcon(copyPath));
                        }).start();
                    }
                });
                buttonsPanel.add(copyButton);
            }
            add(buttonsPanel,BorderLayout.CENTER);
        }
    }


}
