/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

public class PreferencesWrapper implements PropertyChangeListener {

  private final static String PREFERENCES_LOCATION = "oci-intelli-prefs";
  private static Preferences systemPrefs = Preferences.userRoot()
      .node(PREFERENCES_LOCATION);
  private final static String VERSION = "1.1.0";

  public static void setRegion(String regionId) {
    final String oldValue = getRegion();
    systemPrefs.put("region", regionId);
    GlobalEventHandler.getInstance()
        .firePropertyChange(GlobalEventHandler.PROPERTY_REGION_ID, oldValue,
            regionId);

    //ErrorHandler.logInfo("Setting the region to: "+ regionId);
  }

  public static void setProfile(String profileName) {
    final String oldValue = getProfile();
    systemPrefs.put("profile", profileName);
    GlobalEventHandler.getInstance()
        .firePropertyChange(GlobalEventHandler.PROPERTY_PROFILE, oldValue,
            profileName);
    //ErrorHandler.logInfo("Setting the profile to: "+ profileName);
  }

  public static void setConfigFileName(String configFileName) {
    final String oldValue = getConfigFileName();
    systemPrefs.put("configfile", configFileName);
    GlobalEventHandler.getInstance()
        .firePropertyChange(GlobalEventHandler.PROPERTY_CONFIG_FILE, oldValue,
            configFileName);
    //ErrorHandler.logInfo("Setting the config file to: "+ configFileName);
  }

  public static String getRegion() {
    return systemPrefs.get("region", "us-phoenix-1");
  }

  public static String getProfile() {
    return systemPrefs.get("profile", "DEFAULT");
  }

  public static String getConfigFileName() {
    return systemPrefs
        .get("configfile", ConfigFileOperations.getConfigFilePath());
  }

  public static String getUserAgent() {
    return String.format("Oracle-IntelliJToolkit/%s", VERSION);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("RegionID".equals(evt.getPropertyName())) {
      setRegion(evt.getNewValue().toString());
    }
  }
}
