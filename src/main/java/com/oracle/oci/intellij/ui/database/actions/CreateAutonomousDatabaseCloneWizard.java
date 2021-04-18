package com.oracle.oci.intellij.ui.database.actions;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CreateAutonomousDatabaseCloneWizard extends DialogWrapper {
  private static final String WINDOW_TITLE = "Create Autonomous Database Clone";
  private JPanel mainPanel;
  private JPanel chooseCloneTypePanel;
  private JPanel cloneSourcePanel;
  private JRadioButton fullCloneRadioButton;
  private JRadioButton refreshableCloneRadioButton;
  private JRadioButton metadataCloneRadioButton;
  private JRadioButton cloneFromDatabaseInstanceRadioButton;
  private JRadioButton cloneFromABackupRadioButton;
  private JPanel basicInformationPanel;
  private JLabel CreateCompartmentLabel;
  private JComboBox createCompartmentComboBox;
  private JLabel sourceDatabaseNameLabel;
  private JTextField sourceDatabaseNameTextField;
  private JLabel displayNameLabel;
  private JTextField displayNameTextField;
  private JLabel databaseNameLabel;
  private JTextField databaseNameTextField;
  private JLabel chooseDatabaseVersionLabel;
  private JComboBox chooseDatabaseVersionComboBox;
  private JPanel configureDatabasePanel;
  private JLabel ocpuCountLabel;
  private JSpinner ocpuCountSpinner;
  private JLabel storageLabel;
  private JSpinner storageSpinner;
  private JCheckBox autoScalingCheckBox;
  private JPanel createAdministratorCredentialsPanel;
  private JLabel usernameLabel;
  private JLabel passwordLabel;
  private JPasswordField passwordField;
  private JPasswordField confirmPasswordField;
  private JTextField usernameTextField;
  private JLabel confirmPasswordLabel;
  private JPanel chooseNetworkAccessPanel;
  private JLabel accessTypeLabel;
  private JRadioButton allowSecureAccessRadioButton;
  private JRadioButton virtualCloudNetworkRadioButton;

  private ButtonGroup cloneTypeButtonGroup;
  private ButtonGroup cloneSource;

  public CreateAutonomousDatabaseCloneWizard() {
    super(true);
    setTitle(WINDOW_TITLE);
    setOKButtonText(WINDOW_TITLE);

    cloneTypeButtonGroup.add(fullCloneRadioButton);
    cloneTypeButtonGroup.add(refreshableCloneRadioButton);
    cloneTypeButtonGroup.add(metadataCloneRadioButton);

    cloneSource.add(cloneFromDatabaseInstanceRadioButton);
    cloneSource.add(cloneFromABackupRadioButton);

    cloneSource.add(cloneFromABackupRadioButton);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel(){
    return mainPanel;
  }
}
