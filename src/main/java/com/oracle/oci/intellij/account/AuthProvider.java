/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import com.oracle.bmc.ClientRuntime;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.oci.intellij.LogHandler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;

public class AuthProvider implements PropertyChangeListener {

  private static final AuthProvider single_instance = new AuthProvider();
  private AuthenticationDetailsProvider provider;
  private String currentProfileName;
  private String currentConfigFileName;
  private String currentRegionName;
  private volatile String currentCompartmentId = null;

  public final static AuthProvider getInstance() {
    if(single_instance.provider == null) {
      single_instance.createProvider();
    }
    return single_instance;
  }

  public final static boolean isInitialized() {
    return (single_instance.provider != null);
  }

  private void createProvider() {
    try {
      final File configFile = new File(PreferencesWrapper.getConfigFileName());
      if(!configFile.exists())
        LogHandler.error("Unable to find the config file : " + PreferencesWrapper.getConfigFileName());
      provider = new ConfigFileAuthenticationDetailsProvider(
          PreferencesWrapper.getConfigFileName(), PreferencesWrapper.getProfile());
      currentConfigFileName = PreferencesWrapper.getConfigFileName();
      currentProfileName = PreferencesWrapper.getProfile();
      currentRegionName = PreferencesWrapper.getRegion();
      currentCompartmentId = provider.getTenantId();
      setClientUserAgent();
    }
    catch (RuntimeException re) {
      LogHandler.error(re.getMessage(), re);
      //throw re;
    }
    catch (Exception e) {
      LogHandler.error(e.getMessage(), e);
      //throw new RuntimeException(e);
    }

  }

  public AuthenticationDetailsProvider getProvider() {
    if (provider == null) {
      createProvider();
    }
    else {
      // Check if Preference is changed, if yes then create the AuthProvider with
      // new preferences.
      final String newProfile = PreferencesWrapper.getProfile();
      final String newConfigFileName = PreferencesWrapper.getConfigFileName();
      if ((!newProfile.equals(currentProfileName)) || (!newConfigFileName
          .endsWith(currentConfigFileName))){
        createProvider();
      }
    }
    return provider;
  }

  /*
  public void setCompartmentId(String compartmentId) {
    final String oldValue = currentCompartmentId;
    currentCompartmentId = compartmentId;
    GlobalEventHandler.getInstance()
        .firePropertyChange(GlobalEventHandler.PROPERTY_COMPARTMENT_ID,
            oldValue, compartmentId);
  }
*/
  public String getCompartmentId() {
    return currentCompartmentId;
  }

  public Region getRegion() {
    return Region.fromRegionId(currentRegionName);
  }

  // set the plug-in version into the SDK.
  private void setClientUserAgent() {
    ClientRuntime.setClientUserAgent(PreferencesWrapper.getUserAgent());
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    LogHandler.info("AuthProvider: Handling the Event Update : " + evt.toString());
    switch (evt.getPropertyName()) {
      case PreferencesWrapper.EVENT_COMPARTMENT_UPDATE:
        currentCompartmentId = evt.getNewValue().toString();
        break;
      case PreferencesWrapper.EVENT_REGION_UPDATE:
        // Nothing to update here.
        break;
      case PreferencesWrapper.EVENT_SETTINGS_UPDATE:
        // reset the state.
        reset();
        break;

    }
  }

  private void reset() {
    provider = null;
    currentConfigFileName = null;
    currentProfileName = null;
    currentRegionName = null;
    currentCompartmentId = null;
    //createProvider();
  }
}
