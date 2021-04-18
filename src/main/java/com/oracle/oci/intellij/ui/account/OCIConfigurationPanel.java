/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.account;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.oracle.bmc.Region;
import com.oracle.oci.intellij.account.Profile;
import com.oracle.oci.intellij.account.ProfilesSet;
import com.oracle.oci.intellij.util.LogHandler;
import com.oracle.oci.intellij.account.ConfigFileHandler;
import com.oracle.oci.intellij.account.ServicePreferences;
import com.oracle.oci.intellij.ui.common.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.oracle.bmc.util.internal.FileUtils.expandUserHome;

public class OCIConfigurationPanel extends DialogWrapper {

  private static final String ADD_PROFILE = "Add profile";
  private static final Pattern PATTERN = Pattern.compile("\\$\\{(\\w+)\\}");
  private static final String DEFAULT_PROFILE_NAME = "DEFAULT";

  private static final String DEFAULT_REGION = "us-phoenix-1";
  private int defaultRegionIndex = 0;

  private JPanel mainPanel;
  private TextFieldWithBrowseButton configFileTxt;
  private JPanel configPanel;
  private JPanel parametersPanel;
  private JComboBox<String> selectCombo;
  private JComboBox<String> regionCombo;
  private JTextField ocidTxt;
  private JTextField tenantOcidTxt;
  private TextFieldWithBrowseButton privateKeyFileTxt;
  private JTextField fingerPrintTxt;
  private JTextField passPhraseTxt;
  private JButton saveProfileButton;
  private JTextField profileNameTxt;
  private JLabel profileNameLbl;
  private JLabel exampleConfigurationLabel;
  private JLabel ociFreeTrialLbl;
  private JPanel configurationFilePanel;
  private JPanel signupPanel;
  private final JFileChooser fileChooser = new JFileChooser();
  private final JFileChooser pemFileChooser = new JFileChooser();

  public static OCIConfigurationPanel newInstance() {
    return new OCIConfigurationPanel();
  }

  private OCIConfigurationPanel() {
    super(true);
    init();
    setTitle("Oracle Cloud Infrastructure configuration");
    setOKButtonText("Apply");
    mainPanel.setPreferredSize(new Dimension(650, 430));

    configFileTxt.addActionListener(event -> {
      fileChooser.setSelectedFile(new File(ServicePreferences.getConfigFileName()));

      fileChooser.setFileHidingEnabled(false);
      int state = fileChooser.showDialog(mainPanel, "Select Config File");

      if (state == JFileChooser.APPROVE_OPTION) {
        String configFile = fileChooser.getSelectedFile().getAbsolutePath();
        configFileTxt.setText(configFile);
        onConfigFileChange(configFile, ServicePreferences.getProfileName());
      }
    });

    pemFileChooser.setAcceptAllFileFilterUsed(false);

    final FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "PrivateKey File", "pem");
    pemFileChooser.addChoosableFileFilter(filter);
    pemFileChooser.setFileHidingEnabled(false);

    privateKeyFileTxt.addActionListener(event -> {
      int state = pemFileChooser.showDialog(mainPanel, "Select PrivateKey File");
      if (state == JFileChooser.APPROVE_OPTION) {
        privateKeyFileTxt
                .setText(pemFileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    profileNameTxt.setEditable(false);
    selectCombo.addItem(ADD_PROFILE);
    selectCombo.addItemListener((e) -> onProfileChange());

    Region[] regions = Region.values();
    for (int idx = 0; idx < regions.length; idx++) {
      String regionId = regions[idx].getRegionId();
      regionCombo.addItem(regionId);

      if (regionId.equals(DEFAULT_REGION)) {
        defaultRegionIndex = idx;
      }
    }

    regionCombo.setSelectedIndex(defaultRegionIndex);
    saveProfileButton.addActionListener(e -> saveProfile());

    UIUtil.makeWebLink(exampleConfigurationLabel,
            "https://docs.cloud.oracle.com/iaas/Content/API/Concepts/sdkconfig.htm");
    UIUtil.makeWebLink(ociFreeTrialLbl, "https://www.oracle.com/cloud/free/");

    onConfigFileChange(ServicePreferences.getConfigFileName(), ServicePreferences.getProfileName());
  }

  @Override
  public void doOKAction() {
    final String profileName = getSelectedProfile();
    final String configFile = getConfigFile();
    final String region = getSelectedRegion();

    if (configFile == null || configFile.trim().isEmpty()) {
      Messages.showErrorDialog("Invalid Config File", "Error");
    } else if (profileName == null || profileName.trim().isEmpty()) {
      Messages.showErrorDialog("Invalid Profile Name", "Error");
    } else if (region == null || region.trim().isEmpty()) {
      Messages.showErrorDialog("Invalid Region", "Error");
    } else {
      ServicePreferences.updateSettings(configFile, profileName, region);
      close(DialogWrapper.OK_EXIT_CODE);
    }
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    final JPanel panel = (JPanel) createPanel();
    panel.setPreferredSize(new Dimension(800, 500));
    return panel;
  }

  private void onConfigFileChange(String configFile, String givenProfileName) {

    String profileNameToSelect = givenProfileName;
    try {
      final ProfilesSet profilesSet = ConfigFileHandler.parse(configFile);
      final Set<String> profileNames = profilesSet.getProfileNames();

      if (!profileNames.contains(profileNameToSelect)) {
        profileNameToSelect = DEFAULT_PROFILE_NAME;
      }

      selectCombo.removeAllItems();
      configFileTxt.setText(configFile);
      selectCombo.addItem(ADD_PROFILE);

      /*
       ADD_PROFILE is the first item (index 0) in select combo.
       If there are one or more profiles in config file, add
       them to select combo from index 1. That means, the actual
       profiles start at index 1. */

      int idx = 1;
      // Select the first profile (at index 1).
      int selectedIndex = profileNames.size() > 0 ? 1 : 0;

      for (String profileName : profileNames) {
        selectCombo.addItem(profileName);

        if (profileName.equalsIgnoreCase(profileNameToSelect)) {
          selectedIndex = idx;
        }
        idx++;
      }

      selectCombo.setSelectedIndex(selectedIndex);
      if (selectCombo.getSelectedIndex() > 1) {
        onProfileChange();
      }
    } catch (IOException ioEx) {
      UIUtil.fireNotification(NotificationType.ERROR, ioEx.getMessage());
    }
  }

  private void onProfileChange() {
    if (selectCombo.getSelectedIndex() == 0) {
      prepareForAddProfile();
    } else if (selectCombo.getSelectedIndex() > 0) {
      final String profile = (String) selectCombo.getSelectedItem();
      populateProfileParams(profile);
    }
  }

  private void prepareForAddProfile(){
    ocidTxt.setText("");
    ocidTxt.setEditable(true);

    tenantOcidTxt.setText("");
    tenantOcidTxt.setEditable(true);

    privateKeyFileTxt.setText("");
    privateKeyFileTxt.setEnabled(true);

    fingerPrintTxt.setText("");
    fingerPrintTxt.setEditable(true);

    passPhraseTxt.setEditable(true);

    regionCombo.setSelectedIndex(defaultRegionIndex);
    regionCombo.setEnabled(true);

    profileNameLbl.setVisible(true);
    profileNameTxt.setVisible(true);
    saveProfileButton.setEnabled(true);
    myOKAction.setEnabled(false);
  }

  private void populateProfileParams(String profileName){
    final Profile profile = ProfilesSet.getInstance().get(profileName);
    final Properties profileProperties = profile.getProperties();

    ocidTxt.setText((String) profileProperties.get("user"));
    ocidTxt.setEditable(false);

    tenantOcidTxt.setText((String) profileProperties.get("tenancy"));
    tenantOcidTxt.setEditable(false);

    privateKeyFileTxt
            .setText(expandUser(expandVars((String) profileProperties.get("key_file"))));
    privateKeyFileTxt.setEnabled(false);

    fingerPrintTxt.setText((String) profileProperties.get("fingerprint"));
    fingerPrintTxt.setEditable(false);


    passPhraseTxt.setText(""); // Passphrase should not be shown in UI
    passPhraseTxt.setEditable(false);

    String regProfile = (String) profileProperties.get("region");
    if (regProfile != null) {
      int count = 0;
      regProfile = regProfile.trim();
      for (Region region : Region.values()) {
        if (region.getRegionId().equals(regProfile)) {
          regionCombo.setSelectedIndex(count);
          break;
        }
        count++;
      }
    }

    regionCombo.setEnabled(false);
    profileNameLbl.setVisible(false);
    profileNameTxt.setVisible(false);
    profileNameTxt.setEditable(true);
    saveProfileButton.setEnabled(false);
    myOKAction.setEnabled(true);
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

    Matcher matcher = PATTERN.matcher(path);

    while (matcher.find()) {
      String var = matcher.group(1);
      String value = System.getenv(var);

      if (value != null) {
        result = result.replace("${" + var + "}", value);
      }
    }

    return result;
  }

  public JComponent createPanel(){
    return mainPanel;
  }

  private void saveProfile() {
    try {
      validateAddProfileParams();
      ConfigFileHandler.parse(configFileTxt.getText());

      final Properties newProfileParams = new Properties();
      newProfileParams.put("user", ocidTxt.getText());
      newProfileParams.put("fingerprint", fingerPrintTxt.getText());
      newProfileParams.put("key_file", privateKeyFileTxt.getText());

      if (!passPhraseTxt.getText().isEmpty()) {
        newProfileParams.put("pass_phrase", passPhraseTxt.getText());
      }

      newProfileParams.put("tenancy", tenantOcidTxt.getText());

      for (Region region : Region.values()) {
        String regionName = region.toString();
        String selectedRegionName = Objects.requireNonNull(regionCombo.getSelectedItem()).toString();

        if (regionName.equals(selectedRegionName.replace('-', '_'))) {
          newProfileParams.put("region", selectedRegionName);
          break;
        }
      }

      final Profile newProfile = new Profile(profileNameTxt.getText(), newProfileParams);
      ConfigFileHandler.save(expandUserHome(configFileTxt.getText()), newProfile);

      Messages.showInfoMessage("Saved Successfully", "Success");
      onConfigFileChange(configFileTxt.getText(), profileNameTxt.getText());
    } catch (Exception ex) {
      LogHandler.error("Error", ex);
      Messages.showErrorDialog(ex.getMessage(), "Error");
    }
  }

  private void validateAddProfileParams() {
    if (profileNameTxt.getText().isEmpty()) {
      throw new IllegalStateException("Profile name cannot be empty.");
    }

    // Validate OCID of the user.
    final String userOcid = ocidTxt.getText();
    if (userOcid.isEmpty()) {
      throw new IllegalStateException("OCID of the user cannot be empty.");
    }

    if (!userOcid.startsWith("ocid") ||
            !userOcid.contains("user")) {
      throw new IllegalStateException("OCID of the user is invalid.");
    }

    // Validate Fingerprint for the public key.
    final String fingerPrint = fingerPrintTxt.getText();

    if (fingerPrint.isEmpty()) {
      throw new IllegalStateException("Fingerprint for the public key cannot be empty.");
    }

    final String fingerprintRegex = "([0-9a-f]{2}:){15}[0-9a-f]{2}";
    if (!fingerPrint.matches(fingerprintRegex)) {
      throw new IllegalStateException("Fingerprint for the public key is invalid.");
    }

    // Validate path and filename of the private key
    final String keyFile = privateKeyFileTxt.getText();
    if (keyFile.isEmpty()) {
      throw new IllegalStateException("Private key file path cannot be empty.");
    }

    final String tenantOcid = tenantOcidTxt.getText();
    if (tenantOcid.isEmpty()) {
      throw new IllegalStateException("OCID of the your tenancy cannot be empty.");
    }

    if (!tenantOcid.startsWith("ocid") ||
            !tenantOcid.contains("tenancy")) {
      throw new IllegalStateException("OCID of your tenancy is invalid.");
    }
  }

  public String getSelectedProfile(){
    if (selectCombo.getSelectedIndex() > 0) {
      return (String) selectCombo.getSelectedItem();
    }
    else {
      return null;
    }
  }

  public String getConfigFile(){
    return configFileTxt.getText();
  }

  public String getSelectedRegion(){
    return (String) regionCombo.getSelectedItem();
  }

}
