package com.oracle.oci.intellij.ui.account;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.oci.intellij.account.PreferencesWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class OCISettingsAction extends AnAction {

  public OCISettingsAction() {
    super("Settings", "OCI Explorer Settings", new ImageIcon(
        OCISettingsAction.class.getResource("/icons/toolbar-login.png")));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    SettingDialog settings = new SettingDialog();
    settings.showAndGet();
  }

  class SettingDialog extends DialogWrapper {
    OCISettingsPanel settingsPanel = null;

    public SettingDialog() {
      super(true);
      init();
      setTitle("OCI Settings");
      setOKButtonText("Apply");
    }

    @Override
    public void doOKAction() {
      final String profileName = settingsPanel.getSelectedProfile();
      final String configFile = settingsPanel.getConfigFile();
      final String region = settingsPanel.getSelectedRegion();

      if (profileName != null)
        PreferencesWrapper.setProfile(profileName);
      if (configFile != null && !configFile.isEmpty())
        PreferencesWrapper.setConfigFileName(configFile);
      if (region != null) {
        PreferencesWrapper.setRegion(region);
      }

      close(DialogWrapper.OK_EXIT_CODE);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
      settingsPanel = new OCISettingsPanel();
      final JPanel panel = (JPanel) settingsPanel.createPanel();
      panel.setPreferredSize(new Dimension(800, 500));
      return panel;
    }
  }
}
