/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.account;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBScrollPane;
import com.oracle.bmc.Region;
import com.oracle.oci.intellij.account.ConfigFileHandler;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.util.LogHandler;
import com.oracle.oci.intellij.account.ConfigFileHandler.*;
import com.oracle.oci.intellij.ui.common.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.oracle.bmc.util.internal.FileUtils.expandUserHome;

public class ConfigureOracleCloudDialog extends DialogWrapper {

  private static final String ADD_PROFILE = "Add profile";
  private static final Pattern PATTERN = Pattern.compile("\\$\\{(\\w+)\\}");

  private int defaultRegionIndex = 0;

  private JPanel mainPanel;
  private TextFieldWithBrowseButton configFileTextField;
  private JComboBox<String> selectProfileComboBox;
  private JComboBox<String> regionCombo;
  private JTextField ocidTextField;
  private JTextField tenantOcidTextField;
  private JTextField privateKeyFileTextField;
  private JTextField fingerPrintTextField;
  private JTextField passPhraseTextField;
  private JButton saveProfileButton;
  private JTextField profileNameTextField;
  private JLabel profileNameLabel;
  private JLabel exampleConfigurationLabel;
  private JLabel ociFreeTrialLabel;
  private final JFileChooser fileChooser = new JFileChooser();
  private final JFileChooser pemFileChooser = new JFileChooser();

  public static ConfigureOracleCloudDialog newInstance() {
    return new ConfigureOracleCloudDialog();
  }

  private ConfigureOracleCloudDialog() {
    super(true);
    init();
    setTitle("Oracle Cloud Infrastructure Configuration");
    setOKButtonText("Apply");
    mainPanel.setPreferredSize(new Dimension(650, 430));

    configFileTextField.addActionListener(event -> {
      fileChooser.setSelectedFile(new File(SystemPreferences.getConfigFilePath()));
      fileChooser.setFileHidingEnabled(false);

      int state = fileChooser.showDialog(mainPanel, "Select Config File");
      if (state == JFileChooser.APPROVE_OPTION) {
        String configFile = fileChooser.getSelectedFile().getAbsolutePath();
        configFileTextField.setText(configFile);
        onConfigFileChange(configFile, SystemPreferences.getProfileName());
      }
    });

    pemFileChooser.setAcceptAllFileFilterUsed(false);

    final FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "PrivateKey File", "pem");
    pemFileChooser.addChoosableFileFilter(filter);
    pemFileChooser.setFileHidingEnabled(false);

    privateKeyFileTextField.addActionListener(event -> {
      int state = pemFileChooser.showDialog(mainPanel, "Select private key file");
      if (state == JFileChooser.APPROVE_OPTION) {
        privateKeyFileTextField
                .setText(pemFileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    profileNameTextField.setEditable(false);
    selectProfileComboBox.addItem(ADD_PROFILE);
    selectProfileComboBox.addItemListener((e) -> onProfileChange());

    Region[] regions = Region.values();
    for (int idx = 0; idx < regions.length; idx++) {
      String regionId = regions[idx].getRegionId();
      regionCombo.addItem(regionId);

      if (regionId.equals(SystemPreferences.DEFAULT_REGION)) {
        defaultRegionIndex = idx;
      }
    }

    regionCombo.setSelectedIndex(defaultRegionIndex);
    saveProfileButton.addActionListener(e -> saveProfile());

    UIUtil.createWebLink(exampleConfigurationLabel,
            "https://docs.cloud.oracle.com/iaas/Content/API/Concepts/sdkconfig.htm");
    UIUtil.createWebLink(ociFreeTrialLabel, "https://www.oracle.com/cloud/free/");
    onConfigFileChange(SystemPreferences.getConfigFilePath(), SystemPreferences.getProfileName());
  }

  @Override
  public void doOKAction() {
    final String configFile = getConfigFile();
    final String profileName = getSelectedProfile();
    final String region = getSelectedRegion();

    if (configFile == null || configFile.trim().isEmpty()) {
      Messages.showErrorDialog("Invalid config file", "Error");
    } else if (profileName == null || profileName.trim().isEmpty()) {
      Messages.showErrorDialog("Invalid profile name", "Error");
    } else if (region == null || region.trim().isEmpty()) {
      Messages.showErrorDialog("Invalid region", "Error");
    } else {
      close(DialogWrapper.OK_EXIT_CODE);
      try {
        OracleCloudAccount.getInstance().configure(configFile, profileName);
      } catch (IOException ioException) {
        UIUtil.fireNotification(NotificationType.ERROR, ioException.getMessage(), null);
      }
    }
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    mainPanel.setPreferredSize(new Dimension(800, 500));
    return new JBScrollPane(mainPanel);
  }

  private void onConfigFileChange(String configFile, String givenProfileName) {
    String profileNameToSelect;

    try {
      final ProfileSet profileSet = ConfigFileHandler.parse(configFile);

      if (profileSet.get(givenProfileName) != null) {
        // Given profile name is found in the config file.
        profileNameToSelect = givenProfileName;
      } else {
        // Given profile is not found. Use the default profile.
        if (profileSet.get(SystemPreferences.DEFAULT_PROFILE_NAME) != null) {
          Messages.showInfoMessage(
                  String.format("The profile %s isn't found in the config file %s. Switching to %s profile.",
                          givenProfileName, configFile, SystemPreferences.DEFAULT_PROFILE_NAME),
                  "Oracle Cloud Infrastructure configuration");

          profileNameToSelect = SystemPreferences.DEFAULT_PROFILE_NAME;
        } else {
          // Even the default profile is not found in the given config.
          final String message;
          if (givenProfileName.equals(SystemPreferences.DEFAULT_PROFILE_NAME)) {
            message = String.format("The profile %s isn't found in the config file %s.",
                    givenProfileName, configFile);
          } else {
            message = String.format("The profile %s and fall back profile %s aren't found in the config file %s.",
                    givenProfileName, SystemPreferences.DEFAULT_PROFILE_NAME, configFile);
          }
          throw new IllegalStateException(message);
        }
      }

      final Set<String> profileNames = profileSet.getProfileNames();
      configFileTextField.setText(configFile);
      selectProfileComboBox.removeAllItems();
      selectProfileComboBox.addItem(ADD_PROFILE);

      /*
       ADD_PROFILE is the first item (index 0) in select combo.
       If there are one or more profiles in config file, add
       them to select combo from index 1. That means, the actual
       profiles start at index 1. */

      int idx = 1;
      // Select the first profile (at index 1).
      int selectedIndex = profileNames.size() > 0 ? 1 : 0;

      for (String profileName : profileNames) {
        selectProfileComboBox.addItem(profileName);

        if (profileName.equalsIgnoreCase(profileNameToSelect)) {
          selectedIndex = idx;
        }
        idx++;
      }

      selectProfileComboBox.setSelectedIndex(selectedIndex);
      if (selectProfileComboBox.getSelectedIndex() > 1) {
        onProfileChange();
      }
    } catch (Exception ex) {
      Messages.showErrorDialog(ex.getMessage(), "Oracle Cloud Infrastructure configuration");
    }
  }

  private void onProfileChange() {
    if (selectProfileComboBox.getSelectedIndex() == 0) {
      prepareForAddProfile();
    } else if (selectProfileComboBox.getSelectedIndex() > 0) {
      final String profileName = (String) selectProfileComboBox.getSelectedItem();
      populateProfileParameters(profileName);
    }
  }

  private void prepareForAddProfile() {
    ocidTextField.setText("");
    ocidTextField.setEditable(true);

    tenantOcidTextField.setText("");
    tenantOcidTextField.setEditable(true);

    privateKeyFileTextField.setText("");
    privateKeyFileTextField.setEnabled(true);

    fingerPrintTextField.setText("");
    fingerPrintTextField.setEditable(true);

    passPhraseTextField.setEditable(true);

    regionCombo.setSelectedIndex(defaultRegionIndex);
    regionCombo.setEnabled(true);

    profileNameLabel.setVisible(true);
    profileNameTextField.setVisible(true);
    saveProfileButton.setEnabled(true);
    myOKAction.setEnabled(false);
  }

  private void populateProfileParameters(String profileName) {
    try {
      final ProfileSet profileSet = ConfigFileHandler.parse(configFileTextField.getText());
      final Profile profile = profileSet.get(profileName);

      ocidTextField.setText(profile.getEntries().getProperty("user"));
      ocidTextField.setEditable(false);

      tenantOcidTextField.setText(profile.getEntries().getProperty("tenancy"));
      tenantOcidTextField.setEditable(false);

      privateKeyFileTextField
              .setText(expandUser(expandVars(profile.getEntries().getProperty("key_file"))));
      privateKeyFileTextField.setEnabled(false);

      fingerPrintTextField.setText(profile.getEntries().getProperty("fingerprint"));
      fingerPrintTextField.setEditable(false);

      passPhraseTextField.setText("");
      passPhraseTextField.setEditable(false);

      String regionNameInProfile = profile.getEntries().getProperty("region");
      if (regionNameInProfile != null) {
        int count = 0;
        regionNameInProfile = regionNameInProfile.trim();
        for (Region region : Region.values()) {
          if (region.getRegionId().equalsIgnoreCase(regionNameInProfile)) {
            regionCombo.setSelectedIndex(count);
            break;
          }
          count++;
        }
      }

      regionCombo.setEnabled(false);
      profileNameLabel.setVisible(false);
      profileNameTextField.setVisible(false);
      profileNameTextField.setEditable(true);
      saveProfileButton.setEnabled(false);
      myOKAction.setEnabled(true);
    } catch (IOException ioEx) {
      UIUtil.fireNotification(NotificationType.ERROR, ioEx.getMessage(), null);
    }
  }

  /*
   * os.path.expandUser
   */
  public static String expandUser(String path){
    String user = System.getProperty("user.home");

    return path.replaceFirst("~", user);
  }

  /*
   * os.path.expandVars
   */
  public static String expandVars(String path){
    String result = path;
    final Matcher matcher = PATTERN.matcher(path);

    while (matcher.find()) {
      String var = matcher.group(1);
      String value = System.getenv(var);

      if (value != null) {
        result = result.replace("${" + var + "}", value);
      }
    }

    return result;
  }

  private void saveProfile() {
    try {
      validateAddProfileParams();

      final ConfigFileHandler.Profile profile =
              new ConfigFileHandler.Profile(profileNameTextField.getText());

      profile.add("user", ocidTextField.getText())
              .add("fingerprint", fingerPrintTextField.getText())
              .add("key_file", privateKeyFileTextField.getText());

      if (!passPhraseTextField.getText().isEmpty()) {
        profile.add("pass_phrase", passPhraseTextField.getText());
      }

      profile.add("tenancy", tenantOcidTextField.getText());

      for (Region region : Region.values()) {
        String regionName = region.toString();
        String selectedRegionName = Objects.requireNonNull(regionCombo.getSelectedItem()).toString();

        if (regionName.equalsIgnoreCase(selectedRegionName.replace('-', '_'))) {
          profile.add("region", selectedRegionName);
          break;
        }
      }

      ConfigFileHandler.save(expandUserHome(configFileTextField.getText()), profile);
      Messages.showInfoMessage("A new profile has been added to the config file.", "Save Profile");

      onConfigFileChange(configFileTextField.getText(), profile.getName());
    } catch (Exception ex) {
      LogHandler.error("Error", ex);
      Messages.showErrorDialog(ex.getMessage(), "Error");
    }
  }

  private void validateAddProfileParams() throws IOException {
    final String profileName = profileNameTextField.getText();
    if (profileName.isEmpty()) {
      throw new IllegalStateException("Profile name cannot be empty.");
    }

    final ConfigFileHandler.ProfileSet profileSet =
            ConfigFileHandler.parse(configFileTextField.getText());

    if (profileSet.containsKey(profileName)) {
      throw new IllegalStateException("Profile " + profileName + " already exists.");
    }

    // Validate OCID of the user.
    final String userOcid = ocidTextField.getText();
    if (userOcid.isEmpty()) {
      throw new IllegalStateException("OCID of the user cannot be empty.");
    }

    if (!userOcid.startsWith("ocid") ||
            !userOcid.contains("user")) {
      throw new IllegalStateException("OCID of the user is invalid.");
    }

    // Validate Fingerprint for the public key.
    final String fingerPrint = fingerPrintTextField.getText();

    if (fingerPrint.isEmpty()) {
      throw new IllegalStateException("Fingerprint for the public key cannot be empty.");
    }

    final String fingerprintRegex = "([0-9a-f]{2}:){15}[0-9a-f]{2}";
    if (!fingerPrint.matches(fingerprintRegex)) {
      throw new IllegalStateException("Fingerprint for the public key is invalid.");
    }

    // Validate path and filename of the private key
    final String keyFile = privateKeyFileTextField.getText();
    if (keyFile.isEmpty()) {
      throw new IllegalStateException("Private key file path cannot be empty.");
    }

    final String tenantOcid = tenantOcidTextField.getText();
    if (tenantOcid.isEmpty()) {
      throw new IllegalStateException("OCID of the your tenancy cannot be empty.");
    }

    if (!tenantOcid.startsWith("ocid") ||
            !tenantOcid.contains("tenancy")) {
      throw new IllegalStateException("OCID of your tenancy is invalid.");
    }
  }

  public String getSelectedProfile(){
    if (selectProfileComboBox.getSelectedIndex() > 0) {
      return (String) selectProfileComboBox.getSelectedItem();
    }
    else {
      return null;
    }
  }

  public String getConfigFile() {
    return configFileTextField.getText();
  }

  public String getSelectedRegion(){
    return (String) regionCombo.getSelectedItem();
  }

}
