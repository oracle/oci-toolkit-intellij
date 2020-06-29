/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import com.oracle.oci.intellij.LogHandler;
import com.oracle.oci.intellij.ui.database.ADBInstanceClient;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;

public class PreferencesWrapper {

  private final static String PREFERENCES_LOCATION = "oci-intelli-prefs";
  private final static String VERSION = "1.1.0";

  // Events
  public static final String EVENT_REGION_UPDATE = "Event-Region";
  public static final String EVENT_COMPARTMENT_UPDATE = "Event-Compartment";
  public static final String EVENT_SETTINGS_UPDATE = "Event-Settings";
  public static final String EVENT_ADBINSTANCE_UPDATE = "Event-ADBInstanceUpdate";

  private final static Preferences systemPrefs = Preferences.userRoot().node(PREFERENCES_LOCATION);
  private final static PropertyChangeSupport pcs = new PropertyChangeSupport(new PreferencesWrapper());

  public static void addPropertyChangeListener(PropertyChangeListener pcl) {
    pcs.addPropertyChangeListener(pcl);
  }

  public static void updateConfig(final String configFileName,
      final String profile, final String region) {
    systemPrefs.put("profile", profile);
    systemPrefs.put("region", region);
    systemPrefs.put("configfile", configFileName);
    // Always fire the event when updating the settings.
    pcs.firePropertyChange(EVENT_SETTINGS_UPDATE, "",
            configFileName);
  }

  public static void setRegion(final String regionId) {
    final String oldValue = getRegion();
    systemPrefs.put("region", regionId);
    pcs.firePropertyChange(EVENT_REGION_UPDATE, oldValue,
            regionId);
    LogHandler.info("Updating the region to: "+ regionId);
  }

  public static void setCompartment(final String compartmentId) {
    final String oldValue = getCompartment();
    systemPrefs.put("compartment", compartmentId);
    pcs.firePropertyChange(EVENT_COMPARTMENT_UPDATE, oldValue,
            compartmentId);
    LogHandler.info("Updating the compartment to: "+ compartmentId);
  }

  public static void fireADBInstanceUpdateEvent(final String updatedType) {
    pcs.firePropertyChange(EVENT_ADBINSTANCE_UPDATE, "", updatedType);
  }

  public static String getCompartment() {
    return systemPrefs.get("compartment", "");
  }

  public static String getRegion() {
    return systemPrefs.get("region", "us-phoenix-1");
  }

  public static String getProfile() {
    return systemPrefs.get("profile", "DEFAULT");
  }

  public static String getConfigFileName() {
    return systemPrefs.get("configfile", ConfigFileOperations.getConfigFilePath());
  }

  public static String getUserAgent() {
    return String.format("Oracle-IntelliJToolkit/%s", VERSION);
  }
}
