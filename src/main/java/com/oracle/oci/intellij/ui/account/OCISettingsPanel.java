package com.oracle.oci.intellij.ui.account;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.oracle.bmc.Region;
import com.oracle.oci.intellij.ErrorHandler;
import com.oracle.oci.intellij.account.ConfigFileOperations;
import com.oracle.oci.intellij.account.ConfigFileOperations.ConfigFile;
import com.oracle.oci.intellij.account.PreferencesWrapper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.oracle.bmc.util.internal.FileUtils.expandUserHome;

public class OCISettingsPanel
    implements Configurable, Configurable.VariableProjectAppLevel {

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
  private final JFileChooser fileChooser = new JFileChooser();
  private final JFileChooser pemFileChooser = new JFileChooser();

  private ConfigFile config;
  private int defaultRegionIndex;

  public OCISettingsPanel() {
    configFileTxt.setEditable(false);
    configFileTxt.addActionListener(event -> {
      fileChooser
          .setSelectedFile(new File(PreferencesWrapper.getConfigFileName()));
      int i = fileChooser.showDialog(mainPanel, "Select Config File");
      if (i == JFileChooser.APPROVE_OPTION) {
        configFileTxt.setText(fileChooser.getSelectedFile().getAbsolutePath());
        onConfigFileChange();
      }
    });

    pemFileChooser.setAcceptAllFileFilterUsed(false);
    final FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "PrivateKey File", "pem");
    pemFileChooser.addChoosableFileFilter(filter);
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
      if (regionId == DEFAULT_REGION)
        defaultRegionIndex = index;
      index++;
    }
    if (index > 0)
      regionCmb.setSelectedIndex(defaultRegionIndex);

    saveProfileButton.addActionListener(e -> saveProfile());

    configFileTxt.setText(PreferencesWrapper.getConfigFileName());
    onConfigFileChange();
  }

  private void onConfigFileChange() {
    final String configFileName = configFileTxt.getText();
    // Messages.showInfoMessage("Config File : " + configFileName, "Config");
    final File f = new File(configFileName);
    if (f.length() > 50000) {
      Messages.showErrorDialog("File is too large", "Error");
      return;
    }
    try {
      config = ConfigFileOperations
          .parse(configFileName, PreferencesWrapper.getProfile());
      Set<String> profileNames = config.getProfileNames();
      profileCmb.removeAllItems();
      profileCmb.addItem(ADD_PROFILE);
      profileNames.forEach(e -> profileCmb.addItem(e));
      if (profileNames.size() > 0) {
        profileCmb.setSelectedIndex(1);
        onProfileChange();
      }

    }
    catch (IOException e) {
      ErrorHandler.logErrorStack("Error", e);
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

    String passphrase = profileParams.get("pass_phrase");
    if (passphrase == null)
      passPhraseTxt.setText("");
    else
      passPhraseTxt.setText(passphrase);
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
  }//expandvars

  public JComponent createPanel() {
    return mainPanel;
  }

  @Nullable @Override public JComponent createComponent() {
    return mainPanel;
  }

  @Override public boolean isProjectLevel() {
    return false;
  }

  // TODO : Implement
  public boolean isModified() {
    System.out.println("Is Modified??");
    return false;
  }

  // TODO : Implement
  public void apply() {
    System.out.println("Setting Applied.");
  }

  // TODO : Implement
  public void reset() {
    System.out.println("Settings Reset.");
  }

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "OCI Settings";
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

      config.update(profileNameTxt.getText(), newProfileParams);
      ConfigFileOperations
          .save(configFileTxt.getText(), config, profileNameTxt.getText());
      Messages.showInfoMessage("Saved Successfully", "Success");
      onConfigFileChange();
    }
    catch (IOException e) {
      ErrorHandler.logErrorStack("Error", e);
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
