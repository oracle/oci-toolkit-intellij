package com.oracle.oci.intellij.ui.appstack.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.labels.BoldLabel;
import com.intellij.util.ui.JBDimension;
import com.oracle.oci.intellij.ui.appstack.models.Controller;
import com.oracle.oci.intellij.ui.appstack.models.VariableGroup;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.LinkedHashMap;
import java.util.List;

public class ReviewDialog extends DialogWrapper {
    LinkedHashMap<String,String> variables;
    JPanel mainPanel ;
    JBScrollPane mainScrollPane;
    Controller controller = Controller.getInstance();

    protected ReviewDialog(LinkedHashMap<String,String> variables,List<VariableGroup> varGroups) {
        super(false);
        setTitle("Oracle Cloud Infrastructure Configuration");
        setOKButtonText("Apply");
        mainPanel = new JPanel();
//        mainPanel.setPreferredSize(new JBDimension(800,800));

        mainScrollPane = new JBScrollPane(mainPanel);

        this.variables = variables ;
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
        final PropertyDescriptor[][] pds = new PropertyDescriptor[1][1];
        varGroups.forEach(varGroup->{
            try {
                JPanel groupPanel = new JPanel();
                String className = varGroup.getClass().getSimpleName().replaceAll("_"," ");
                groupPanel.setBorder(BorderFactory.createTitledBorder(className));
                groupPanel.setLayout(new BoxLayout(groupPanel,BoxLayout.Y_AXIS));

                pds[0] = controller.getSortedProertyDescriptorsByVarGroup(varGroup);

                for (PropertyDescriptor pd : pds[0]) {
                    if (pd.getName().equals("class") || pd.getName().equals("db_compartment") || !variables.containsKey(pd.getName())) {
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

//        variables.forEach((key,value)->{
//
//            // i need to get pd , and
//
//
//        });

        init();

    }

//    private void createVariablePanel() {
//
//    }

//    private ReviewVarPanel createVarPanel(String key , String value ) {
////        return new ReviewVarPanel(key, value);
//    }

    @Override
    protected @Nullable JComponent createCenterPanel() {

        return mainScrollPane;
    }

    class ReviewVarPanel extends JPanel {
        JLabel keyLabel;
        JLabel valueLabel;

        ReviewVarPanel(PropertyDescriptor pd, String value){
            setLayout(new BorderLayout());
//            setPreferredSize(new JBDimension(760, 40));

            this.keyLabel = new JLabel(pd.getDisplayName()+" : ");
            keyLabel.setToolTipText(pd.getShortDescription());
            keyLabel.setFont(new Font(keyLabel.getFont().getName(), Font.BOLD, keyLabel.getFont().getSize()));
            this.valueLabel = new JLabel(value);
            this.keyLabel.setPreferredSize(new JBDimension(300,20));

            setBorder(BorderFactory.createEmptyBorder(0,8,0,0));

            add(keyLabel,BorderLayout.WEST);
//            add(border,BorderLayout.EAST);
            add(valueLabel,BorderLayout.CENTER);
        }
    }
}
