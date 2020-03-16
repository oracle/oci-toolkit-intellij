package com.oracle.oci.intellij.account;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class GlobalEventHandler {
  private static final GlobalEventHandler SINGLETON_INSTANCE = new GlobalEventHandler();

  private final PropertyChangeSupport pcs;
  public static final String PROPERTY_REGION_ID = "RegionID";
  public static final String PROPERTY_COMPARTMENT_ID = "CompartmentID";
  public static final String PROPERTY_CONFIG_FILE = "ConfigFile";
  public static final String PROPERTY_PROFILE = "Profile";

  private GlobalEventHandler() {
    pcs = new PropertyChangeSupport(this);
  }

  public static GlobalEventHandler getInstance() {
    return SINGLETON_INSTANCE;
  }

  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    pcs.addPropertyChangeListener(pcl);
  }

  public void firePropertyChange(final String propName, final String oldValue,
      final String newValue) {
    pcs.firePropertyChange(propName, oldValue, newValue);
  }

}
