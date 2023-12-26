package com.oracle.oci.intellij.ui.appstack.actions;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.wizard.WizardModel;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.intellij.util.ui.JBDimension;
import com.oracle.bmc.core.model.Subnet;
import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.devops.model.RepositorySummary;
import com.oracle.bmc.dns.model.ZoneSummary;
import com.oracle.bmc.http.client.internal.ExplicitlySetBmcModel;
import com.oracle.bmc.identity.model.AvailabilityDomain;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.keymanagement.model.KeySummary;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.ui.appstack.models.Controller;
import com.oracle.oci.intellij.ui.appstack.models.Utils;
import com.oracle.oci.intellij.ui.appstack.models.Validator;
import com.oracle.oci.intellij.ui.appstack.models.VariableGroup;
import com.oracle.oci.intellij.ui.common.CompartmentSelection;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomWizardStep extends WizardStep implements PropertyChangeListener {
    JBScrollPane mainScrollPane;
    JPanel mainPanel;
    VariableGroup variableGroup;
    boolean dirty = true ;
    List<VarPanel> varPanels ;
    Controller controller = Controller.getInstance();


    public CustomWizardStep(VariableGroup varGroup, PropertyDescriptor[] propertyDescriptors, LinkedHashMap<String, PropertyDescriptor> descriptorsState, List<VariableGroup> varGroups) {
        mainPanel = new JPanel();
        mainScrollPane = new JBScrollPane(mainPanel);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        varPanels = new ArrayList<>();
        varGroup.addPropertyChangeListener(this);
        varGroup.addVetoableChangeListener(new Validator());
        this.variableGroup = varGroup;


        controller.setDescriptorsState(descriptorsState) ;

        String className = varGroup.getClass().getSimpleName().replaceAll("_"," ");
        Border emptyBorder = BorderFactory.createEmptyBorder();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(emptyBorder, className);

        Font currentFont = titledBorder.getTitleFont();
        if (currentFont == null) {
            currentFont = UIManager.getFont("TitledBorder.font");
        }
        titledBorder.setTitleFont(currentFont.deriveFont(Font.BOLD));
        mainPanel.setBorder(titledBorder);


        for (PropertyDescriptor pd : propertyDescriptors) {
            if (pd.getName().equals("class") ) {
                continue;
            }
            try {
               VarPanel varPanel =new VarPanel(pd,variableGroup);
               varPanels.add(varPanel) ;
               controller.addVariablePanel( varPanel);
               mainPanel.add(varPanel)  ;
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public List<VarPanel> getVarPanels() {
        return varPanels;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public JComponent prepare(WizardNavigationState state) {
        return mainScrollPane;
    }

    @Override
    public WizardStep onNext(WizardModel model) {
        boolean isValidated = controller.doValidate(this);

        setDirty(!isValidated);



        CustomWizardModel appStackWizardModel = (CustomWizardModel) model;
        AppStackParametersWizardDialog.isProgramaticChange = true;
        appStackWizardModel.getGroupMenuList().setSelectedIndex(appStackWizardModel.getGroupMenuList().getSelectedIndex()+1);
        AppStackParametersWizardDialog.isProgramaticChange = false;
        return super.onNext(model);
    }



    @Override
    public boolean onFinish() {
        boolean isValidated = controller.doValidate(this);
        setDirty(!isValidated);

        return super.onFinish();
    }

    @Override
    public WizardStep onPrevious(WizardModel model) {
        boolean isValidated = controller.doValidate(this);
        setDirty(!isValidated);

        CustomWizardModel appStackWizardModel = (CustomWizardModel) model;
        AppStackParametersWizardDialog.isProgramaticChange = true;
        appStackWizardModel.getGroupMenuList().setSelectedIndex(appStackWizardModel.getGroupMenuList().getSelectedIndex()-1);
        AppStackParametersWizardDialog.isProgramaticChange = false;
        return super.onPrevious(model);
    }



    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        // execute the updateDependency ...
        controller.updateDependencies(evt.getPropertyName(),variableGroup);
        // execute update-visibility ..... for the pd that changed
        controller.updateVisibility(evt.getPropertyName(),variableGroup);

    }

    public class VarPanel extends JPanel {
        JLabel label ;
        JComponent mainComponent ;
        JLabel errorLabel;
        PropertyDescriptor pd;
        VariableGroup variableGroup;



        VarPanel(PropertyDescriptor pd, VariableGroup variableGroup) throws InvocationTargetException, IllegalAccessException {
            this.pd = pd;
            this.variableGroup = variableGroup;
            createVarPanel(pd,variableGroup);
        }
        private void createVarPanel( PropertyDescriptor pd,VariableGroup variableGroup) throws InvocationTargetException, IllegalAccessException {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(760, 40));
            setMaximumSize(getPreferredSize());
            String varTitle = "";
            if (pd.getValue("required").equals(false)){
                varTitle+=" (Optional)";
            }

            label = new JLabel("<html><body style='width: 175px'>"+pd.getDisplayName()+" <i>"+varTitle+"</i></body></html>");
            label.setPreferredSize(new JBDimension(250,45));
            label.setToolTipText( pd.getShortDescription());


            errorLabel = new JLabel();
            errorLabel.setForeground(JBColor.RED);
            errorLabel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));





             mainComponent = createVarComponent(pd,variableGroup,errorLabel);


            boolean  isVisible = controller.isVisible((String) pd.getValue("visible"));
            this.setVisible(isVisible);


            add(label, BorderLayout.WEST);
            add(mainComponent,BorderLayout.CENTER);
            add(errorLabel,BorderLayout.EAST);


            setBorder(BorderFactory.createEmptyBorder(0,8,8,0));

        }



        private JComponent createVarComponent(PropertyDescriptor pd,VariableGroup varGroup,JLabel errorLabel) throws InvocationTargetException, IllegalAccessException {
            Class<?> propertyType = pd.getPropertyType();
            JComponent component ;

            if (propertyType.getName().equals("boolean")) {

                JCheckBox checkBox = new JCheckBox();
                component = checkBox;

                checkBox.addActionListener(e -> {
                        controller.setValue(checkBox.isSelected(),varGroup,pd);
                });
                boolean defaultValue = (boolean)(pd.getValue("default")!= null?pd.getValue("default"):true );
                controller.setValue(defaultValue,varGroup,pd);
                checkBox.setSelected(defaultValue);

                // add this to the condition || ((String)pd.getValue("type")).startsWith("oci")
            } else if (propertyType.isEnum() || ((String)pd.getValue("type")).startsWith("oci")  ) {

                // if it's an compartment object


                if (pd.getValue("type").equals("oci:identity:compartment:id")){
                    JPanel compartmentPanel = new JPanel();
                    JButton selectCompartmentBtn  = new JButton("select");
                    JTextField compartmentName = new JTextField("");
                    compartmentName.setPreferredSize(new JBDimension(405,30));
                    compartmentName.setEnabled(false);
                    compartmentPanel.add(compartmentName);
                    compartmentPanel.add(selectCompartmentBtn);
                    Compartment selectedCompartment = (Compartment) controller.getValue(varGroup,pd);

                    compartmentName.setText(selectedCompartment.getName());

                    ExecutorService executorService = Executors.newSingleThreadExecutor();

                    selectCompartmentBtn.addActionListener(e->{
                        final CompartmentSelection compartmentSelection1 = CompartmentSelection.newInstance();


                        if (compartmentSelection1.showAndGet()){
                            final Compartment selected = compartmentSelection1.getSelectedCompartment();
                            compartmentName.setText(selected.getName());
                            executorService.submit(() -> {
                                controller.setValue(selected,varGroup,pd);
                                return null;
                            });
                        }
                    });
                    return compartmentPanel;
                }else {

                    ComboBox comboBox = new ComboBox();
                    AtomicReference<List<String>> enumValues = new AtomicReference<>((List<String>) pd.getValue("enum"));
                    if (enumValues.get() != null) {
                        for (String enumValue : enumValues.get()) {
                            Class<?> type = pd.getPropertyType();
                            String normalizedItem =enumValue.trim().replaceAll("[. ]","_");

                            Enum<?> enumValue1 = Enum.valueOf((Class<Enum>) type, normalizedItem);
                            comboBox.addItem(enumValue1);
                        }

                    } else {
                        //todo  suggest values from account of user   in a combobox depending on  type
                        /* example
                         * oci:identity:compartment:id --> compartments of the user
                         * oci:core:vcn:id --> existed vcn s ...
                         *
                         */
                        // we need to set a custom renderer
                        comboBox.setModel(new DefaultComboBoxModel<>(new String[] {"Loading..."}));
                        controller.loadComboBoxValues(pd,varGroup,comboBox);
                        System.out.println("----------"+pd.getName()+"----------------");


                        comboBox.setRenderer(new DefaultListCellRenderer(){

                            //todo enhance this later
                            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                                if (value instanceof AutonomousDatabaseSummary) {
                                    AutonomousDatabaseSummary adb = (AutonomousDatabaseSummary) value;
                                    setText(adb.getDisplayName()); // Set the display name of the instance
                                } else if (value instanceof VaultSummary) {
                                    VaultSummary adb = (VaultSummary) value;
                                    setText(adb.getDisplayName()); // Set the display name of the instance
                                }else if(value instanceof RepositorySummary){
                                    RepositorySummary repositorySummary = (RepositorySummary)value;
                                    setText(repositorySummary.getName());
                                }else if (value instanceof KeySummary) {
                                    KeySummary adb = (KeySummary) value;
                                    setText(adb.getDisplayName()); // Set the display name of the instance
                                }else if (value instanceof AvailabilityDomain) {
                                    AvailabilityDomain adb = (AvailabilityDomain) value;
                                    setText(adb.getName()); // Set the display name of the instance
                                }else if (value instanceof Subnet) {
                                    Subnet adb = (Subnet) value;
                                    setText(adb.getDisplayName()); // Set the display name of the instance
                                }else if (value instanceof Vcn) {
                                    Vcn adb = (Vcn) value;
                                    setText(adb.getDisplayName()); // Set the display name of the instance
                                }else if (value instanceof Compartment) {
                                    Compartment adb = (Compartment) value;
                                    setText(adb.getName()); // Set the display name of the instance
                                }else if (value instanceof ZoneSummary) {
                                    ZoneSummary zone = (ZoneSummary) value;
                                    setText(zone.getName()); // Set the display name of the instance
                                } else if(value == null){
                                    setText("No items");
                                }
                                return this;
                            }
                        });


                    }

                    comboBox.addItemListener(e -> {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            controller.setValue(comboBox.getSelectedItem(),varGroup,pd);
                        }

                    });

                    if (pd.getValue("default") != null) {
                        controller.setValue(pd.getValue("default"),varGroup,pd);
                        comboBox.setSelectedItem(pd.getValue("default"));
                    }

                    component = comboBox;
                }
            } else if (propertyType.getName().equals("int")) {
                SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
                JSpinner spinner = new JSpinner(spinnerModel);

                JComponent editorComponent = spinner.getEditor();

                if (editorComponent instanceof JSpinner.DefaultEditor) {
                    JTextField textField = ((JSpinner.DefaultEditor) editorComponent).getTextField();
                    textField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            controller.setValue(spinner.getValue(),varGroup,pd);
                           focusValidation(spinner.getValue());
                        }
                    });
                }


                Object value = pd.getValue("default");
                if (value != null){
                    if (value instanceof String) {
                        if (((String)value).isEmpty()){
                            value = 0;
                        } else {
                            value = Integer.parseInt((String) value);
                        }
                    }
                    controller.setValue(value,varGroup,pd);
                    spinner.setValue(value);

                }

                spinner.addChangeListener(e->{
                    controller.setValue(spinner.getValue(),varGroup,pd);
                });


                component = spinner;
            } else {

                JTextComponent textField = getjTextField(pd, varGroup);
                if (pd.getValue("default") != null){
                    textField.setText(pd.getValue("default").toString());
                    controller.setValue(pd.getValue("default").toString(),varGroup,pd);
                }

                component = textField;
            }
            component.setPreferredSize(new JBDimension(200,40));


            return component;
        }

        private void errorCheck(PropertyDescriptor pd, JLabel errorLabel, JSpinner spinner) {
            if ( pd.getValue("required") != null && (boolean) pd.getValue("required")   ) {
                String errorMsg ="This field is required";

                if ((int) spinner.getValue() == 0){
                    errorMsg = "this field can't be 0";
                }else {
                    spinner.setBorder(UIManager.getBorder("TextField.border")); // Reset to default border
                    errorLabel.setText("");
                    return;
                }

                spinner.setBorder(BorderFactory.createLineBorder(JBColor.RED));
                errorLabel.setText(errorMsg);
            }


        }


        public JLabel getLabel() {
            return label;
        }

        public void setLabel(JLabel label) {
            this.label = label;
        }

        public JComponent getMainComponent() {
            return mainComponent;
        }

        public void setMainComponent(JComponent mainComponent) {
            this.mainComponent = mainComponent;
        }

        public JLabel getErrorLabel() {
            return errorLabel;
        }

        public void setErrorLabel(JLabel errorLabel) {
            this.errorLabel = errorLabel;
        }

        public PropertyDescriptor getPd() {
            return pd;
        }

        public void setPd(PropertyDescriptor pd) {
            this.pd = pd;
        }

        public VariableGroup getVariableGroup() {
            return variableGroup;
        }

        public void setVariableGroup(VariableGroup variableGroup) {
            this.variableGroup = variableGroup;
        }

        private JTextComponent getjTextField(PropertyDescriptor pd, VariableGroup varGroup) {
            JTextComponent textField  ;
            if (pd.getValue("type").equals("password")){
                textField = new JPasswordField();
            }else {
                textField = new JTextField();
            }
            textField.addFocusListener(new FocusAdapter() {
                                           @Override
                                           public void focusLost(FocusEvent e) {
                                               controller.setValue(textField.getText(),varGroup,pd);
                                               focusValidation(textField.getText());
                                           }
                                       }

            );


            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    documentChanged();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    documentChanged();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {documentChanged();}

                private void documentChanged() {
                    // Handle text field changes here
                        controller.setValue(textField.getText(),varGroup,pd);
                }

            });
            return textField;
        }

        void focusValidation(Object value){
            try {
                Validator.doValidate(pd,value,null);
                controller.handleValidated(pd);
            } catch (PropertyVetoException ex) {
                controller.handleError(pd,ex.getMessage());
            }
        }
    }
}






