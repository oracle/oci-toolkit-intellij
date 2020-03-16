/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import com.oracle.bmc.ClientRuntime;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.oci.intellij.ErrorHandler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class AuthProvider implements PropertyChangeListener {

  private static AuthProvider single_instance = null;

  private static AuthenticationDetailsProvider provider;
  private String currentProfileName = PreferencesWrapper.getProfile();
  private String currentConfigFileName = PreferencesWrapper.getConfigFileName();
  private String currentRegionName = PreferencesWrapper.getRegion();
  private String currentCompartmentId;

  public static AuthProvider getInstance() {
    if (single_instance == null) {
      single_instance = new AuthProvider();
    }
    return single_instance;
  }

  private AuthProvider() {
    GlobalEventHandler.getInstance().addPropertyChangeListener(this);
  }

  private AuthenticationDetailsProvider createProvider() {
    try {
      provider = new ConfigFileAuthenticationDetailsProvider(
          currentConfigFileName, currentProfileName);
    }
    catch (Exception e) {
      ErrorHandler.logErrorStack(e.getMessage(), e);
    }
    currentCompartmentId = provider.getTenantId();
    setClientUserAgent();
    return provider;
  }

  public AuthenticationDetailsProvider getProvider() {
    if (provider == null) {
      provider = createProvider();
    }
    else {
      String newProfile = PreferencesWrapper.getProfile();
      String newConfigFileName = PreferencesWrapper.getConfigFileName();
      currentRegionName = PreferencesWrapper.getRegion();

      if ((!newProfile.equals(currentProfileName)) || (!newConfigFileName
          .endsWith(currentConfigFileName))) {
        currentConfigFileName = newConfigFileName;
        currentProfileName = newProfile;
        try {
          provider = new ConfigFileAuthenticationDetailsProvider(
              currentConfigFileName, currentProfileName);
          ErrorHandler.logInfo(
              currentProfileName + " " + currentConfigFileName + " "
                  + currentRegionName + " " + currentCompartmentId);
        }
        catch (Exception e) {
          ErrorHandler.logInfo(
              "Error connecting to: " + currentProfileName + " " + e
                  .getMessage());
        }
      }
    }
    return provider;
  }

  public void setCompartmentId(String compartmentId) {
    final String oldValue = currentCompartmentId;
    currentCompartmentId = compartmentId;
    GlobalEventHandler.getInstance()
        .firePropertyChange(GlobalEventHandler.PROPERTY_COMPARTMENT_ID,
            oldValue, compartmentId);
  }

  public String getCompartmentId() {
    return currentCompartmentId;
  }

  public Region getRegion() {
    return Region.fromRegionId(currentRegionName);
  }

  // set the plug-in version into the SDK.
  private void setClientUserAgent() {
    ErrorHandler.logInfo(
        "Setting SDK ClientUserAgent to: " + PreferencesWrapper.getUserAgent());
    ClientRuntime.setClientUserAgent(PreferencesWrapper.getUserAgent());
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("CompartmentID".equals(evt.getPropertyName())) {
     currentCompartmentId = evt.getNewValue().toString();
    }
  }
}
