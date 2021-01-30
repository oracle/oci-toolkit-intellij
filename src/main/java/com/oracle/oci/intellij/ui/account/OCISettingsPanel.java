/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.account;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.oracle.bmc.Region;
import com.oracle.oci.intellij.LogHandler;
import com.oracle.oci.intellij.account.ConfigFileOperations;
import com.oracle.oci.intellij.account.ConfigFileOperations.ConfigFile;
import com.oracle.oci.intellij.account.PreferencesWrapper;
import com.oracle.oci.intellij.ui.common.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.oracle.bmc.util.internal.FileUtils.expandUserHome;

public class OCISettingsPanel extends DialogWrapper {

  private static final String ADD_PROFILE = "Add Profile";
  private static Pattern pv = Pattern.compile("\\$\\{(\\w+)\\}");
  private static final String DEFAULT_REGION = "us-phoenix-1";

  private JPanel mainPanel;
  private TextFieldWithBrowseButton configFileTxt;
  private JPanel configPanel;
  private JPanel clientOptionsPanel;
  private JPanel parametersPanel;
  private JComboBox profileCmb;
  private JComboBox regionCmb;
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

  private ConfigFile config;
  private int defaultRegionIndex;

  public OCISettingsPanel() {
    super(true);
    init();
    setTitle("OCI Settings");
    setOKButtonText("Apply");
    configFileTxt.setEditable(false);

    configFileTxt.addActionListener(event -> {
      fileChooser
          .setSelectedFile(new File(PreferencesWrapper.getConfigFileName()));
      fileChooser.setFileHidingEnabled(false);
      int i = fileChooser.showDialog(mainPanel, "Select Config File");
      if (i == JFileChooser.APPROVE_OPTION) {
        final File selectedFile = new File(fileChooser.getSelectedFile().getAbsolutePath());
        if(selectedFile.exists()) {
          onConfigFileChange(fileChooser.getSelectedFile().getAbsolutePath(), "DEFAULT");
        }
        else {
          UIUtil.fireErrorNotification("Config file not exist.");
        }
      }
    });

    pemFileChooser.setAcceptAllFileFilterUsed(false);
    final FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "PrivateKey File", "pem");
    pemFileChooser.addChoosableFileFilter(filter);
    pemFileChooser.setFileHidingEnabled(false);
    privateKeyFileTxt.addActionListener(event -> {
      int i = pemFileChooser.showDialog(mainPanel, "Select PrivateKey File");
      if (i == JFileChooser.APPROVE_OPTION) {
        privateKeyFileTxt
            .setText(pemFileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    profileNameTxt.setEditable(false);
    profileCmb.addItem(ADD_PROFILE);
    profileCmb.addItemListener((e) -> onProfileChange());
    connTimeoutTxt.setText("3000");
    readTimeoutTxt.setText("60000");

    int index = 0;
    for (Region region : Region.values()) {
      String regionId = region.getRegionId();
      regionCmb.addItem(regionId);
      if (regionId.equals(DEFAULT_REGION))
        defaultRegionIndex = index;
      index++;
    }
    if (index > 0)
      regionCmb.setSelectedIndex(defaultRegionIndex);

    saveProfileButton.addActionListener(e -> saveProfile());

    UIUtil.makeWebLink(ociConfigInstrcutionLbl,
        "https://docs.cloud.oracle.com/iaas/Content/API/Concepts/sdkconfig.htm");
    UIUtil.makeWebLink(ociFreeTrialLbl, "https://www.oracle.com/cloud/free/");
    onConfigFileChange(PreferencesWrapper.getConfigFileName(), PreferencesWrapper.getProfile());
  }

  @Override
  public void doOKAction() {
    final String profileName = getSelectedProfile();
    final String configFile = getConfigFile();
    final String region = getSelectedRegion();
    if(configFile == null || configFile.trim().isEmpty()) {
      Messages.showErrorDialog("Invalid Config File", "Error");
    }
    else if(profileName == null || profileName.trim().isEmpty()) {
      Messages.showErrorDialog("Invalid Profile Name", "Error");
    }
    else if(region == null || region.trim().isEmpty()) {
      Messages.showErrorDialog("Invalid Region", "Error");
    }
    else {
      PreferencesWrapper.updateConfig(configFile, profileName, region);
      UIUtil.fireSuccessNotification("<html>OCI configuration updated.<br>Config file Path : " + configFile +
          "<br>Profile : " + profileName +
          "<br>Region : " + region +"</html>");
      close(DialogWrapper.OK_EXIT_CODE);
    }
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    final JPanel panel = (JPanel)createPanel();
    panel.setPreferredSize(new Dimension(800, 500));
    return panel;
  }


  private void onConfigFileChange(final String newConfigFile, final String profile) {
    final String configFileName = configFileTxt.getText();
    final File f = new File(newConfigFile);
    if (f.length() > 50000) {
      Messages.showErrorDialog("File is too large", "Error");
      return;
    }
    try {
      config = ConfigFileOperations.parse(newConfigFile, profile);
      final Set<String> profileNames = config.getProfileNames();
      profileCmb.removeAllItems();
      configFileTxt.setText(newConfigFile);
      profileCmb.addItem(ADD_PROFILE);
      int index = 1;
      int selectedIndex = profileNames.size() > 0 ? 1 : 0;
      for(String e: profileNames) {
        profileCmb.addItem(e);
        if(e.equals(profile))
          selectedIndex = index;
        index++;
      }
      profileCmb.setSelectedIndex(selectedIndex);
      if (profileCmb.getSelectedIndex() > 1) {
        onProfileChange();
      }
    }
    catch (IOException e) {
      UIUtil.fireErrorNotification("<html>Unable to parse the config file.<br>File : "
          + newConfigFile + "<br>Error : " + e.getMessage() + "</html>");
      LogHandler.error("Error", e);
    }

  }

  private void onProfileChange() {
    if (profileCmb.getSelectedIndex() == 0) {
      prepareForAddProfile();
    }
    else if (profileCmb.getSelectedIndex() > 0) {
      final String profile = (String) profileCmb.getSelectedItem();
      populateProfileParams(profile);
    }
  }

  private void prepareForAddProfile() {
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

    regionCmb.setSelectedIndex(defaultRegionIndex);
    regionCmb.setEnabled(true);

    profileNameLbl.setVisible(true);
    profileNameTxt.setVisible(true);
    saveProfileButton.setEnabled(true);
    myOKAction.setEnabled(false);
  }

  private void populateProfileParams(String profile) {
    //Messages.showInfoMessage("Setting Profile : " + profile, "Info");
    final Map<String, String> profileParams = config.getProfile(profile);
    ocidTxt.setText(profileParams.get("user"));
    ocidTxt.setEditable(false);

    tenantOcidTxt.setText(profileParams.get("tenancy"));
    tenantOcidTxt.setEditable(false);

    privateKeyFileTxt
        .setText(expanduser(expandvars(profileParams.get("key_file"))));
    privateKeyFileTxt.setEnabled(false);

    fingerPrintTxt.setText(profileParams.get("fingerprint"));
    fingerPrintTxt.setEditable(false);


    passPhraseTxt.setText(""); // Passphrase should not be shown in UI
    passPhraseTxt.setEditable(false);

    String regProfile = profileParams.get("region");
    if (regProfile != null) {
      int count = 0;
      regProfile = regProfile.trim();
      for (Region region : Region.values()) {
        if (region.getRegionId().equals(regProfile)) {
          regionCmb.setSelectedIndex(count);
          break;
        }
        count++;
      }
    }

    regionCmb.setEnabled(false);
    profileNameLbl.setVisible(false);
    profileNameTxt.setVisible(false);
    profileNameTxt.setEditable(true);
    saveProfileButton.setEnabled(false);
    myOKAction.setEnabled(true);
  }

  /*
   * os.path.expanduser
   */
  public static String expanduser(String path) {
    String user = System.getProperty("user.home");

    return path.replaceFirst("~", user);
  }//expanduser

  /*
   * os.path.expandvars
   */
  public static String expandvars(String path) {
    String result = new String(path);

    Matcher m = pv.matcher(path);
    while (m.find()) {
      String var = m.group(1);
      String value = System.getenv(var);
      if (value != null)
        result = result.replace("${" + var + "}", value);
    }
    return result;
  }

  public JComponent createPanel() {
    return mainPanel;
  }

  private void saveProfile() {
    try {
      if (!validateValues(profileNameTxt.getText(), ocidTxt.getText(),
          privateKeyFileTxt.getText(), fingerPrintTxt.getText(),
          passPhraseTxt.getText(), tenantOcidTxt.getText()))
        return;

      config = ConfigFileOperations.parse(configFileTxt.getText());

      // Profile Name does not already exist
      if (config.getProfileNames().contains(profileNameTxt.getText())) {
        Messages.showErrorDialog(
            "Profile already exists " + profileNameTxt.getText(), "Error");
        return;
      }

      final Map<String, String> newProfileParams = new HashMap<String, String>();
      newProfileParams.put("user", ocidTxt.getText());
      newProfileParams.put("key_file", privateKeyFileTxt.getText());
      newProfileParams.put("fingerprint", fingerPrintTxt.getText());
      if (!passPhraseTxt.getText().isEmpty())
        newProfileParams.put("pass_phrase", passPhraseTxt.getText());
      newProfileParams.put("tenancy", tenantOcidTxt.getText());

      for (Region region : Region.values()) {
        if (region.toString().equals(
            regionCmb.getSelectedItem().toString().toUpperCase()
                .replace('-', '_'))) {
          newProfileParams.put("region", regionCmb.getSelectedItem().toString());
          break;
        }
      }

      final File f = new File(expandUserHome(configFileTxt.getText()));
      if (!f.exists()) {
        config.update("DEFAULT", newProfileParams);
        ConfigFileOperations.save(configFileTxt.getText(), config, "DEFAULT");
      }
      else {
        config.update(profileNameTxt.getText(), newProfileParams);
        ConfigFileOperations
            .save(configFileTxt.getText(), config, profileNameTxt.getText());
      }
      Messages.showInfoMessage("Saved Successfully", "Success");
      onConfigFileChange(configFileTxt.getText(), "DEFAULT");
    }
    catch (IOException e) {
      LogHandler.error("Error", e);
    }
  }

  private boolean validateValues(String profileName, String userOcidText,
      String keyFileText, String fingerprintText, String passphraseText,
      String tenancyOcidText) {

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

  public String getSelectedProfile() {
    if (profileCmb.getSelectedIndex() > 0)
      return (String) profileCmb.getSelectedItem();
    else
      return null;
  }

  public String getConfigFile() {
    return configFileTxt.getText();
  }

  public String getSelectedRegion() {
    return (String) regionCmb.getSelectedItem();
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }
}
