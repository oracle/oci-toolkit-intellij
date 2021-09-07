/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.intellij.util.LogHandler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Container of system and live preferences.
 */
public class SystemPreferences {

  private final static String VERSION = "0.1.0";
  private static final Preferences preferences =
          Preferences.userRoot().node("oci-intellij-preferences");
  private static final String DEFAULT_CONFIG_FILE_PATH =
          System.getProperty("user.home") + File.separator + ".oci" + File.separator + "config";
  public static final String DEFAULT_PROFILE_NAME = "DEFAULT";
  public static final String ROOT_COMPARTMENT_NAME = "root";
  public static final String DEFAULT_REGION = "us-phoenix-1";

  private static final String LAST_READ_CONFIG_FILE_PATH_KEY = "lastReadConfigFilePath";
  private static final String LAST_READ_PROFILE_NAME_KEY = "lastReadProfileName";

  // Live user selections.
  private static String currentRegionName = DEFAULT_REGION;
  private static Compartment currentCompartment = null;

  // Properties that fire change notifications.
  public static final String EVENT_REGION_UPDATE = "Event-Region";
  public static final String EVENT_COMPARTMENT_UPDATE = "Event-Compartment";
  public static final String EVENT_SETTINGS_UPDATE = "Event-Settings";
  public static final String EVENT_ADB_INSTANCE_UPDATE = "Event-ADBInstancesUpdate";

  // Dispatcher that notifies the property change event to all listeners.
  private final static PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(new Object());

  public static void setConfigInfo(String configFilePath, String profileName, String regionName, Compartment compartment) {
    preferences.put(LAST_READ_CONFIG_FILE_PATH_KEY, configFilePath);
    preferences.put(LAST_READ_PROFILE_NAME_KEY, profileName);
    currentRegionName = regionName;
    currentCompartment = compartment;

    propertyChangeSupport.firePropertyChange(EVENT_SETTINGS_UPDATE, "", configFilePath);
  }

  public static void setCompartment(Compartment compartment) {
    currentCompartment = compartment;

    LogHandler.info("Setting the compartment name with : "+ compartment.getName());
    propertyChangeSupport.firePropertyChange(EVENT_COMPARTMENT_UPDATE, "", compartment.getName());
  }

  public static void setRegionName(String regionName) {
    final String oldRegionName = currentRegionName;
    currentRegionName = regionName;

    propertyChangeSupport.firePropertyChange(EVENT_REGION_UPDATE, oldRegionName, regionName);
  }

  /**
   * Returns the last read configuration path or default.
   * @return the last used config file path or default path.
   */
  public static String getConfigFilePath() {
    return preferences.get(LAST_READ_CONFIG_FILE_PATH_KEY, DEFAULT_CONFIG_FILE_PATH);
  }

  /**
   * Returns the last read profile name or default.
   * @return the last read profile key or the default.
   */
  public static String getProfileName() {
    return preferences.get(LAST_READ_PROFILE_NAME_KEY, DEFAULT_PROFILE_NAME);
  }

  public static String getCompartmentName() {
    if (currentCompartment == null) {
      return ROOT_COMPARTMENT_NAME;
    }
    return currentCompartment.getName();
  }

  public static String getCompartmentId() {
    if (currentCompartment != null) {
      return currentCompartment.getId();
    }
    return null;
  }

  public static String getRegionName() {
    return currentRegionName;
  }

  /**
   * Adds the property change listener
   * @param propertyChangeListener property change listener.
   */
  public static void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
  }

  public static String getUserAgent() {
    return String.format("Oracle-IntelliJToolkit/%s", VERSION);
  }

  public static void fireADBInstanceUpdateEvent(String updateType){
    propertyChangeSupport.firePropertyChange(EVENT_ADB_INSTANCE_UPDATE, "", updateType);
  }
}
