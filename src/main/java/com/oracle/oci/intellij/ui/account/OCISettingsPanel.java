/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.account;

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

public class OCISettingsPanel extends DialogWrapper {

  private static final String ADD_PROFILE = "Add Profile";
  private static final Pattern PATTERN = Pattern.compile("\\$\\{(\\w+)\\}");
  private static final String DEFAULT_PROFILE_NAME = "DEFAULT";

  private static final String DEFAULT_REGION = "us-phoenix-1";
  private int defaultRegionIndex = 0;

  private JPanel mainPanel;
  private TextFieldWithBrowseButton configFileTxt;
  private JPanel configPanel;
  private JPanel clientOptionsPanel;
  private JPanel parametersPanel;
  private JComboBox<String> profileCombo;
  private JComboBox<String> regionCombo;
  private JTextField ocidTxt;
  private JTextField tenantOcidTxt;
  private TextFieldWithBrowseButton privateKeyFileTxt;
  private JTextField fingerPrintTxt;
  private JTextField passPhraseTxt;
  private JTextField connTimeoutTxt;
  private JTextField readTimeoutTxt;
  private JButton saveProfileButton;
  private JTextField profileNameTxt;
  private JLabel profileNameLbl;
  private JLabel ociConfigInstrcutionLbl;
  private JLabel ociFreeTrialLbl;
  private final JFileChooser fileChooser = new JFileChooser();
  private final JFileChooser pemFileChooser = new JFileChooser();

  public OCISettingsPanel(){
    super(true);
    init();
    setTitle("OCI Settings");
    setOKButtonText("Apply");
    configFileTxt.setEditable(false);

    configFileTxt.addActionListener(event -> {
      fileChooser.setSelectedFile(new File(ServicePreferences.getConfigFileName()));

      fileChooser.setFileHidingEnabled(false);
      int state = fileChooser.showDialog(mainPanel, "Select Config File");

      if (state == JFileChooser.APPROVE_OPTION) {
        onConfigFileChange(fileChooser.getSelectedFile().getAbsolutePath());
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
    profileCombo.addItem(ADD_PROFILE);
    profileCombo.addItemListener((e) -> onProfileChange());
    connTimeoutTxt.setText("3000");
    readTimeoutTxt.setText("60000");

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

    UIUtil.makeWebLink(ociConfigInstrcutionLbl,
            "https://docs.cloud.oracle.com/iaas/Content/API/Concepts/sdkconfig.htm");
    UIUtil.makeWebLink(ociFreeTrialLbl, "https://www.oracle.com/cloud/free/");

    onConfigFileChange(ServicePreferences.getConfigFileName());
  }

  @Override
  public void doOKAction(){
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
      UIUtil.fireSuccessNotification("<html>OCI configuration updated.<br>Config file Path : " + configFile +
              "<br>Profile : " + profileName +
              "<br>Region : " + region + "</html>");
      close(DialogWrapper.OK_EXIT_CODE);
    }
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel(){
    final JPanel panel = (JPanel) createPanel();
    panel.setPreferredSize(new Dimension(800, 500));
    return panel;
  }

  private void onConfigFileChange(String configFile){
    try {
      final ProfilesSet profilesSet = ConfigFileHandler.parse(configFile);
      final Set<String> profileNames = profilesSet.getProfileNames();

      profileCombo.removeAllItems();
      configFileTxt.setText(configFile);
      profileCombo.addItem(ADD_PROFILE);

      int selectedIndex = 0;
      int idx = 0;
      for (String profileName : profileNames) {
        profileCombo.addItem(profileName);
        if (profileName.equals(DEFAULT_PROFILE_NAME)) {
          selectedIndex = idx;
        }
        idx++;
      }

      profileCombo.setSelectedIndex(selectedIndex);
      if (profileCombo.getSelectedIndex() > 1) {
        onProfileChange();
      }
    } catch (IOException ioEx) {
      UIUtil.fireErrorNotification(ioEx.getMessage());
    }
  }

  private void onProfileChange(){
    if (profileCombo.getSelectedIndex() == 0) {
      prepareForAddProfile();
    } else if (profileCombo.getSelectedIndex() > 0) {
      final String profile = (String) profileCombo.getSelectedItem();
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

    passPhraseTxt.setText("");
    passPhraseTxt.setEditable(true);

    regionCombo.setSelectedIndex(defaultRegionIndex);
    regionCombo.setEnabled(true);

    profileNameLbl.setVisible(true);
    profileNameTxt.setVisible(true);
    saveProfileButton.setEnabled(true);
    myOKAction.setEnabled(false);
  }

  private void populateProfileParams(String profileName){
    final Profile profile = ProfilesSet.instance().get(profileName);
    final Properties profileProperties = profile.getProperties();

    ocidTxt.setText((String) profileProperties.get("user"));
    ocidTxt.setEditable(false);

    tenantOcidTxt.setText((String) profileProperties.get("tenancy"));
    tenantOcidTxt.setEditable(false);

    privateKeyFileTxt
            .setText(expanduser(expandvars((String) profileProperties.get("key_file"))));
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
   * os.path.expanduser
   */
  public static String expanduser(String path){
    String user = System.getProperty("user.home");

    return path.replaceFirst("~", user);
  }

  /*
   * os.path.expandvars
   */
  public static String expandvars(String path){
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

  private void saveProfile(){
    try {
      if (!validateValues(ocidTxt.getText(), privateKeyFileTxt.getText(),
              fingerPrintTxt.getText(), tenantOcidTxt.getText()))
        return;

      try {
        ConfigFileHandler.parse(configFileTxt.getText());
      } catch (IllegalStateException illegalStateException) {
        Messages.showErrorDialog(illegalStateException.getMessage(), "Error");
        return;
      }

      final Properties newProfileParams = new Properties();
      newProfileParams.put("user", ocidTxt.getText());
      newProfileParams.put("key_file", privateKeyFileTxt.getText());
      newProfileParams.put("fingerprint", fingerPrintTxt.getText());

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

      final Profile profile = new Profile(profileNameTxt.getText(), newProfileParams);
      ConfigFileHandler.save(expandUserHome(configFileTxt.getText()), profile);

      Messages.showInfoMessage("Saved Successfully", "Success");
      onConfigFileChange(configFileTxt.getText());
    } catch (IOException e) {
      LogHandler.error("Error", e);
    }
  }

  private boolean validateValues(String userOcidText, String keyFileText,
                                 String fingerprintText, String tenancyOcidText) {

    // Validate the following:
    // 2. Valid Ocids (userOcid, tenancyOcid)
    if (!(userOcidText.startsWith("ocid") && (userOcidText.contains("user")))) {
      Messages.showErrorDialog(
              "Enter valid User OCID. Not a valid User OCID: " + userOcidText,
              "Error");
      return false;
    }

    if (!(tenancyOcidText.startsWith("ocid") && (tenancyOcidText
            .contains("tenancy")))) {
      Messages.showErrorDialog(
              "Enter valid Tenancy OCID. Not a valid Tenancy OCID: "
                      + tenancyOcidText, "Error");
      return false;
    }

    // 5. Fingerprint is in valid format
    String fingerprintRegex = "([0-9a-f]{2}:){15}[0-9a-f]{2}";
    if (!fingerprintText.matches(fingerprintRegex)) {
      Messages.showErrorDialog(
              "Enter valid fingerprint. Not a valid fingerprint: "
                      + fingerprintRegex, "Error");
      return false;
    }

    // 3. Key file name is a valid file
    File keyFile = new File(keyFileText);
    if (!keyFile.exists()) {
      Messages.showErrorDialog(
              "Enter valid key file. The provided file does not exist: "
                      + keyFileText, "Error");
      return false;
    }
    return true;
  }

  public String getSelectedProfile(){
    if (profileCombo.getSelectedIndex() > 0)
      return (String) profileCombo.getSelectedItem();
    else
      return null;
  }

  public String getConfigFile(){
    return configFileTxt.getText();
  }

  public String getSelectedRegion(){
    return (String) regionCombo.getSelectedItem();
  }

  private void createUIComponents(){
    // TODO: place custom component creation code here
  }
}
