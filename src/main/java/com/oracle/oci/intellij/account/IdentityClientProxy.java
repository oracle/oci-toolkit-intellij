package com.oracle.oci.intellij.account;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Compartment;
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

/**
 * The reference of {@link IdentityClient} changes whenever a new profile is loaded.
 * This proxy class holds the updated reference of {@link IdentityClient} object.
 */
public class IdentityClientProxy implements PropertyChangeListener {
  private static final String ROOT_COMPARTMENT_NAME = "root";
  private AuthenticationDetailsProvider authenticationDetailsProvider = null;
  private IdentityClient identityClient;

  // Instance of this should be taken from the outer class factory method only.
  IdentityClientProxy() {
  }

  void init(final AuthenticationDetailsProvider authenticationDetailsProvider, String region) {
    reset();

    this.authenticationDetailsProvider = authenticationDetailsProvider;
    identityClient = new IdentityClient(authenticationDetailsProvider);
    identityClient.setRegion(region);
  }

  private Compartment rootCompartment = null;

  /**
   * Returns the root compartment.
   * @return the root compartment.
   */
  public Compartment                                                                                                                                                                                                  getRootCompartment(boolean isRefresh) {
    if (rootCompartment == null || isRefresh) {
      String tenantId = authenticationDetailsProvider.getTenantId();
      rootCompartment =  Compartment.builder()
              .compartmentId(tenantId)
              .id(tenantId)
              .name(ROOT_COMPARTMENT_NAME)
              .lifecycleState(Compartment.LifecycleState.Active)
              .build();
    }
    return rootCompartment;
  }

  public List<Compartment> getCompartmentList(Compartment compartment) {
    final List<Compartment> compartmentList = new ArrayList<>();
    String nextPageToken = null;

    do {
      try {
        ListCompartmentsRequest listCompartmentsRequest =
                ListCompartmentsRequest.builder()
                        .limit(10)
                        .compartmentId(compartment.getId())
                        .page(nextPageToken)
                        .accessLevel(ListCompartmentsRequest.AccessLevel.Accessible)
                        .build();

        final ListCompartmentsResponse response =
                identityClient.listCompartments(listCompartmentsRequest);

        if (response != null) {
          compartmentList.addAll(response.getItems());
          nextPageToken = response.getOpcNextPage();
        }
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }  while (nextPageToken != null);

    return compartmentList;
  }

  /**
   * Returns the list of regions.
   *
   * @return the regions list.
   */
  public List<RegionSubscription> getRegionsList() {
    final ListRegionSubscriptionsResponse response = identityClient
            .listRegionSubscriptions(ListRegionSubscriptionsRequest
                    .builder()
                    .tenancyId(authenticationDetailsProvider.getTenantId())
                    .build());

    return response.getItems();
  }

  @Override
  public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
    LogHandler.info("IdentityClientProxy: Handling the event update : " + propertyChangeEvent.toString());
    switch (propertyChangeEvent.getPropertyName()) {
      case SystemPreferences.EVENT_REGION_UPDATE:
        identityClient.setRegion(propertyChangeEvent.getNewValue().toString());
        break;
    }
  }

  private void reset() {
    if (identityClient != null) {
      identityClient.close();
      identityClient = null;
      authenticationDetailsProvider = null;
      rootCompartment = null;
    }
  }

}
