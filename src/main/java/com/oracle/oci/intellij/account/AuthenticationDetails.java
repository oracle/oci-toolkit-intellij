/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.oci.intellij.util.LogHandler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;

import static com.oracle.bmc.ClientRuntime.setClientUserAgent;
import static com.oracle.oci.intellij.account.ServicePreferences.*;

/**
 * Implementation to read config parameters of Oracle cloud service.
 */
public class AuthenticationDetails implements PropertyChangeListener {

  // Singleton instance.
  private static final AuthenticationDetails instance = new AuthenticationDetails();

  private AuthenticationDetailsProvider provider = null;
  private String currentProfileName;
  private String currentConfigFileName;
  private String currentRegionName;
  private volatile String currentCompartmentId = null;

  /**
   * Returns the singleton instance of this class.
   *
   * @return AuthenticationDetails
   */
  public static AuthenticationDetails getInstance(){
    return instance;
  }

  /**
   * Instantiates and returns {@link AuthenticationDetailsProvider}.
   * If there is change in profile parameters, {@link AuthenticationDetailsProvider}
   * is re-instantiated before returning.
   *
   * @return instance of {@link AuthenticationDetailsProvider}
   */
  public AuthenticationDetailsProvider getProvider() {
    if (provider == null || isNewProfileParams()) {
      init();
    }

    return provider;
  }

  /**
   * Checks if there is a change in profile parameters.
   *
   * @return {@code boolean}.
   */
  private boolean isNewProfileParams() {
    return ((!getProfileName().equals(currentProfileName)) ||
            (!getConfigFileName().endsWith(currentConfigFileName)));
  }

  /**
   * Initializes the profile parameter fields of this class.
   */
  private void init() {
    try {
      provider = new ConfigFileAuthenticationDetailsProvider(
              getConfigFileName(), getProfileName());

      currentConfigFileName = getConfigFileName();
      currentProfileName = getProfileName();
      currentRegionName = ServicePreferences.getRegion();
      currentCompartmentId = provider.getTenantId();

      setClientUserAgent(getUserAgent());
    } catch (IOException ioEx) {
      throw new RuntimeException(ioEx);
    }
  }

  /**
   * Returns the compartment id.
   *
   * @return compartment id.
   */
  public String getCompartmentId(){
    return currentCompartmentId;
  }

  /**
   * Returns the {@link Region}.
   *
   * @return {@link Region}.
   */
  public Region getRegion(){
    return Region.fromRegionId(currentRegionName);
  }

  /**
   * Property change event listener.
   * @param event the event.
   */
  @Override
  public void propertyChange(PropertyChangeEvent event) {
    LogHandler.info("AuthProvider: Handling the Event Update : " + event.toString());

    switch (event.getPropertyName()) {
      case EVENT_COMPARTMENT_UPDATE:
        currentCompartmentId = event.getNewValue().toString();
        break;
      case EVENT_REGION_UPDATE:
        // Nothing to update here.
        break;
      case EVENT_SETTINGS_UPDATE:
        reset();
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + event.getPropertyName());
    }
  }

  /**
   * Clear the present parameters.
   */
  private void reset() {
    provider = null;
    currentConfigFileName = null;
    currentProfileName = null;
    currentRegionName = null;
    currentCompartmentId = null;
  }
}
