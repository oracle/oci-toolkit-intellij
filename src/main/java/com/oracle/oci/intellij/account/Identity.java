/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.Compartment.LifecycleState;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.requests.ListRegionSubscriptionsRequest;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import com.oracle.bmc.identity.responses.ListRegionSubscriptionsResponse;
import com.oracle.oci.intellij.util.LogHandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class Identity implements PropertyChangeListener {

  private static final Identity instance = new Identity();
  private IdentityClient identityClient;

  static final String ROOT_COMPARTMENT_NAME = "[Root Compartment]";
  private String currentCompartmentName = ROOT_COMPARTMENT_NAME;

  /**
   * Returns the singleton instance.
   *
   * @return the singleton instance.
   */
  public static Identity getInstance() {
    instance.init();
    return instance;
  }

  /**
   * Initialize the fields.
   */
  private void init() {
    if (identityClient == null) {
      identityClient = new IdentityClient(AuthenticationDetails.getInstance().getProvider());
      identityClient.setRegion(AuthenticationDetails.getInstance().getRegion());
    }
  }

  /**
   * Property change event listener.
   * @param event the event.
   */
  @Override
  public void propertyChange(PropertyChangeEvent event) {
    LogHandler.info("IdentClient: Handling the Event Update : " + event.toString());

    switch (event.getPropertyName()) {
      case ServicePreferences.EVENT_COMPARTMENT_UPDATE:
        // Nothing
        break;
      case ServicePreferences.EVENT_REGION_UPDATE:
        if (identityClient != null) {
          identityClient.setRegion(event.getNewValue().toString());
        }
        break;
      case ServicePreferences.EVENT_SETTINGS_UPDATE:
        reset();
        currentCompartmentName = getRootCompartment().getName();
        break;
    }
  }

  /**
   * Reset fields.
   */
  private void reset() {
    if (identityClient != null) {
      identityClient.close();
    }

    identityClient = null;
    currentCompartmentName = ROOT_COMPARTMENT_NAME;
  }

  /**
   * Returns root compartment.
   *
   * @return root compartment.
   */
  public Compartment getRootCompartment(){
    String compartmentId = AuthenticationDetails.getInstance().getProvider().getTenantId();
    return Compartment.builder()
            .compartmentId(compartmentId)
            .id(compartmentId).name(ROOT_COMPARTMENT_NAME)
            .lifecycleState(LifecycleState.Active)
            .build();
  }

  public List<Compartment> getCompartmentList(Compartment compartment){
    List<Compartment> compartmentList = new ArrayList<>();
    String nextPageToken = null;

    do {
      try {
        ListCompartmentsResponse response = identityClient.listCompartments(
                ListCompartmentsRequest.builder().limit(10)
                        .compartmentId(compartment.getId()).page(nextPageToken)
                        .accessLevel(ListCompartmentsRequest.AccessLevel.Accessible)
                        .build());
        if (response != null) {
          compartmentList.addAll(response.getItems());
          nextPageToken = response.getOpcNextPage();
        }
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
    while (nextPageToken != null);

    return compartmentList;
  }

  /**
   * Sets the current compartment name.
   *
   * @param compartmentName current compartment name.
   */
  public void setCurrentCompartmentName(String compartmentName){
    this.currentCompartmentName = compartmentName;
  }

  /**
   * Returns the current compartment name.
   *
   * @return current compartment name.
   */
  public String getCurrentCompartmentName(){
    return currentCompartmentName;
  }

  /**
   * Gets the regions list.
   *
   * @return the regions list.
   */
  public List<RegionSubscription> getRegionsList(){
    final ListRegionSubscriptionsResponse response = identityClient
            .listRegionSubscriptions(ListRegionSubscriptionsRequest
                    .builder()
                    .tenancyId(AuthenticationDetails.getInstance().getProvider().getTenantId())
                    .build());

    return response.getItems();
  }

}
