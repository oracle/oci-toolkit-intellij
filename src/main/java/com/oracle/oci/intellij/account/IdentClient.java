package com.oracle.oci.intellij.account;

import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.AvailabilityDomain;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.Compartment.LifecycleState;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.bmc.identity.requests.ListAvailabilityDomainsRequest;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.requests.ListRegionSubscriptionsRequest;
import com.oracle.bmc.identity.responses.ListAvailabilityDomainsResponse;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import com.oracle.bmc.identity.responses.ListRegionSubscriptionsResponse;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class IdentClient implements PropertyChangeListener {
  private static IdentClient single_instance = null;
  private static IdentityClient identityClient;
  private List<Compartment> compartmentList = new ArrayList<Compartment>();
  private final String ROOT_COMPARTMENT_NAME = "[Root Compartment]";
  private String currentCompartmentName = "[Root Compartment]";

  private IdentClient() {
    if (identityClient == null) {
      identityClient = createIdentityClient();
    }
  }

  public static IdentClient getInstance() {
    if (single_instance == null) {
      single_instance = new IdentClient();
    }
    return single_instance;
  }

  @Override public void propertyChange(PropertyChangeEvent evt) {
    if ("RegionID".equals(evt.getPropertyName())) {
      identityClient.setRegion(evt.getNewValue().toString());
    }
  }

  /*
      @Override
      public void updateClient() {
          close();
          createIdentityClient();
      }

      @Override
      public void close() {
          try {
              if (identityClient != null) {
                  identityClient.close();
              }
          } catch (Exception e) {
              ErrorHandler.logErrorStack(e.getMessage(), e);
          }
      }
  */
  private IdentityClient createIdentityClient() {
    identityClient = new IdentityClient(
        AuthProvider.getInstance().getProvider());
    identityClient.setRegion(AuthProvider.getInstance().getRegion());
    return identityClient;
  }

  @Override public void finalize() throws Throwable {
    identityClient.close();
    single_instance = null;
  }

  public Compartment getRootCompartment() {
    String compartmentId = AuthProvider.getInstance().getProvider()
        .getTenantId();
    Compartment rootComp = Compartment.builder().compartmentId(compartmentId)
        .id(compartmentId).name(ROOT_COMPARTMENT_NAME)
        .lifecycleState(LifecycleState.Active).build();
    return rootComp;
  }

  public List<Compartment> getCompartmentList(Compartment compartment) {
    List<Compartment> compartmentList = new ArrayList<Compartment>();
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
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    while (nextPageToken != null);

    return compartmentList;
  }

  public List<Compartment> getCompartmentList() {
    String nextPageToken = null;
    String compartmentId = AuthProvider.getInstance().getProvider()
        .getTenantId();
    compartmentList.clear();

    Compartment rootComp = Compartment.builder().compartmentId(compartmentId)
        .id(compartmentId).name(ROOT_COMPARTMENT_NAME)
        .lifecycleState(LifecycleState.Active).build();
    compartmentList.add(rootComp);

    do {
      try {
        ListCompartmentsResponse response = identityClient.listCompartments(
            ListCompartmentsRequest.builder().limit(10)
                .compartmentId(compartmentId)
                .compartmentIdInSubtree(Boolean.TRUE).page(nextPageToken)
                .accessLevel(ListCompartmentsRequest.AccessLevel.Accessible)
                .build());
        if (response != null) {
          compartmentList.addAll(response.getItems());
          nextPageToken = response.getOpcNextPage();
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    while (nextPageToken != null);

    return compartmentList;
  }

  public void setCurrentCompartmentName(String compartmentName) {
    this.currentCompartmentName = compartmentName;
  }

  public String getCurrentCompartmentName() {
    return currentCompartmentName;
        /*
        getCompartmentList();
        for(Compartment compartment : compartmentList) {
            System.out.println("Current Compartment ID  : " + compartment.getId());
            System.out.println("AuthPro Compartment ID  : " + AuthProvider.getInstance().getCompartmentId());
            if (compartment.getId().equals(AuthProvider.getInstance().getCompartmentId())) {
                return compartment.getName();
            }
        }
        System.out.println("Unable to get the compartment id , returning the root compartment name.");
        return ROOT_COMPARTMENT_NAME;
         */
  }

  public List<RegionSubscription> getRegionsList() {
    ListRegionSubscriptionsResponse response = identityClient
        .listRegionSubscriptions(ListRegionSubscriptionsRequest.builder()
            .tenancyId(AuthProvider.getInstance().getProvider().getTenantId())
            .build());
    return response.getItems();
  }

  public List<AvailabilityDomain> getAvailabilityDomains(
      AuthenticationDetailsProvider provider, String compartmentId,
      com.oracle.bmc.Region region) throws Exception {

    List<AvailabilityDomain> domains = new ArrayList<AvailabilityDomain>();

    ListAvailabilityDomainsResponse listAvailabilityDomainsResponse = identityClient
        .listAvailabilityDomains(ListAvailabilityDomainsRequest.builder()
            .compartmentId(compartmentId).build());
    if (listAvailabilityDomainsResponse != null)
      domains = listAvailabilityDomainsResponse.getItems();

    return domains;
  }

}
