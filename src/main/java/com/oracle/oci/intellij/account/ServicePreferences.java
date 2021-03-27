/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import com.oracle.oci.intellij.util.LogHandler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;

/**
 * Container of preferences.
 */
public class ServicePreferences {

  private final static String PREFERENCES_LOCATION = "oci-intellij-prefs";
  private final static String VERSION = "1.1.0";

  public static final String EVENT_REGION_UPDATE = "Event-Region";
  public static final String EVENT_COMPARTMENT_UPDATE = "Event-Compartment";
  public static final String EVENT_SETTINGS_UPDATE = "Event-Settings";
  public static final String EVENT_ADB_INSTANCE_UPDATE = "Event-ADBInstanceUpdate";

  private final static Preferences preferences = Preferences.userRoot().node(PREFERENCES_LOCATION);
  private final static PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(new ServicePreferences());

  /**
   * Adds the property change listener
   * @param propertyChangeListener property change listner.
   */
  public static void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Update the settings.
   * @param configFileName config file name
   * @param profileName profile name
   * @param regionName region name
   */
  public static void updateSettings(final String configFileName,
                                    final String profileName, final String regionName) {
    preferences.put("profile", profileName);
    preferences.put("region", regionName);
    preferences.put("configfile", configFileName);

    propertyChangeSupport.firePropertyChange(EVENT_SETTINGS_UPDATE, "", configFileName);
  }

  /**
   * Update with new region.
   *
   * @param regionId new region id.
   */
  public static void updateRegion(final String regionId) {
    final String oldValue = getRegion();
    preferences.put("region", regionId);

    propertyChangeSupport.firePropertyChange(EVENT_REGION_UPDATE, oldValue, regionId);
    LogHandler.info("Updating the region to: "+ regionId);
  }

  /**
   * Update with new compartment.
   *
   * @param compartmentId new compartment id.
   */
  public static void updateCompartment(final String compartmentId) {
    final String oldValue = getCompartment();
    preferences.put("compartment", compartmentId);

    propertyChangeSupport.firePropertyChange(EVENT_COMPARTMENT_UPDATE, oldValue, compartmentId);
    LogHandler.info("Updated the compartment to: "+ compartmentId);
  }

  /**
   * Fires an update event of Autonomous Database.
   *
   * @param updateType The type of update.
   */
  public static void fireADBInstanceUpdateEvent(final String updateType) {
    propertyChangeSupport.firePropertyChange(EVENT_ADB_INSTANCE_UPDATE, "", updateType);
  }

  /**
   * Returns the compartment name.
   *
   * @return the compartment name.
   */
  public static String getCompartment() {
    return preferences.get("compartment", "");
  }

  /**
   * Returns the region name.
   *
   * @return the region name.
   */
  public static String getRegion() {
    return preferences.get("region", "us-phoenix-1");
  }

  /**
   * Returns the profile name.
   *
   * @return the profile name.
   */
  public static String getProfile() {
      return preferences.get("profile", "DEFAULT");
  }

  /**
   * Returns the name of configuration file.
   *
   * @return the name of configuration file.
   */
  public static String getConfigFileName() {
    return preferences.get("configfile", ConfigFileHandler.getConfigFilePath());
  }

  /**
   * Returns user agent.
   *
   * @return user agent name.
   */
  public static String getUserAgent() {
    return String.format("Oracle-IntelliJToolkit/%s", VERSION);
  }
}
