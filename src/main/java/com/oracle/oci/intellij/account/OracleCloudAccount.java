/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import static com.oracle.bmc.ClientRuntime.setClientUserAgent;
import static com.oracle.oci.intellij.account.SystemPreferences.getRegionName;
import static com.oracle.oci.intellij.account.SystemPreferences.getUserAgent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.resourcemanager.model.*;
import com.oracle.bmc.resourcemanager.requests.*;
import com.oracle.bmc.resourcemanager.responses.*;
import com.oracle.oci.intellij.ui.appstack.exceptions.JobRunningException;
import org.apache.commons.io.FileUtils;

import com.oracle.bmc.artifacts.ArtifactsClient;
import com.oracle.bmc.artifacts.model.GenericArtifact;
import com.oracle.bmc.artifacts.model.GenericArtifactSummary;
import com.oracle.bmc.artifacts.requests.DeleteGenericArtifactRequest;
import com.oracle.bmc.artifacts.requests.DeleteRepositoryRequest;
import com.oracle.bmc.artifacts.requests.ListGenericArtifactsRequest;
import com.oracle.bmc.artifacts.responses.DeleteGenericArtifactResponse;
import com.oracle.bmc.artifacts.responses.ListGenericArtifactsResponse;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.certificatesmanagement.CertificatesManagementClient;
import com.oracle.bmc.certificatesmanagement.model.CertificateSummary;
import com.oracle.bmc.certificatesmanagement.requests.ListCertificatesRequest;
import com.oracle.bmc.certificatesmanagement.responses.ListCertificatesResponse;
import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.NetworkSecurityGroup;
import com.oracle.bmc.core.model.Subnet;
import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.core.requests.GetVcnRequest;
import com.oracle.bmc.core.requests.ListNetworkSecurityGroupsRequest;
import com.oracle.bmc.core.requests.ListSubnetsRequest;
import com.oracle.bmc.core.requests.ListVcnsRequest;
import com.oracle.bmc.core.responses.GetVcnResponse;
import com.oracle.bmc.core.responses.ListNetworkSecurityGroupsResponse;
import com.oracle.bmc.core.responses.ListSubnetsResponse;
import com.oracle.bmc.core.responses.ListVcnsResponse;
import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.model.AutonomousDatabase;
import com.oracle.bmc.database.model.AutonomousDatabaseBackupSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseWallet;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseCloneDetails;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseDetails;
import com.oracle.bmc.database.model.DbVersionSummary;
import com.oracle.bmc.database.model.GenerateAutonomousDatabaseWalletDetails;
import com.oracle.bmc.database.model.RestoreAutonomousDatabaseDetails;
import com.oracle.bmc.database.model.UpdateAutonomousDatabaseDetails;
import com.oracle.bmc.database.model.UpdateAutonomousDatabaseWalletDetails;
import com.oracle.bmc.database.requests.CreateAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.DeleteAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.GenerateAutonomousDatabaseWalletRequest;
import com.oracle.bmc.database.requests.GetAutonomousDatabaseRegionalWalletRequest;
import com.oracle.bmc.database.requests.GetAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.GetAutonomousDatabaseWalletRequest;
import com.oracle.bmc.database.requests.ListAutonomousDatabaseBackupsRequest;
import com.oracle.bmc.database.requests.ListAutonomousDatabasesRequest;
import com.oracle.bmc.database.requests.ListDbVersionsRequest;
import com.oracle.bmc.database.requests.RestoreAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.StartAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.StopAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.UpdateAutonomousDatabaseRegionalWalletRequest;
import com.oracle.bmc.database.requests.UpdateAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.UpdateAutonomousDatabaseWalletRequest;
import com.oracle.bmc.database.responses.GenerateAutonomousDatabaseWalletResponse;
import com.oracle.bmc.database.responses.GetAutonomousDatabaseRegionalWalletResponse;
import com.oracle.bmc.database.responses.GetAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.GetAutonomousDatabaseWalletResponse;
import com.oracle.bmc.database.responses.ListAutonomousDatabaseBackupsResponse;
import com.oracle.bmc.database.responses.ListAutonomousDatabasesResponse;
import com.oracle.bmc.database.responses.ListDbVersionsResponse;
import com.oracle.bmc.devops.DevopsClient;
import com.oracle.bmc.devops.model.RepositorySummary;
import com.oracle.bmc.devops.requests.ListRepositoriesRequest;
import com.oracle.bmc.devops.responses.ListRepositoriesResponse;
import com.oracle.bmc.dns.DnsClient;
import com.oracle.bmc.dns.model.ZoneSummary;
import com.oracle.bmc.dns.requests.ListZonesRequest;
import com.oracle.bmc.dns.responses.ListZonesResponse;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.AuthToken;
import com.oracle.bmc.identity.model.AvailabilityDomain;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.Compartment.LifecycleState;
import com.oracle.bmc.identity.model.CreateCompartmentDetails;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.bmc.identity.requests.CreateCompartmentRequest;
import com.oracle.bmc.identity.requests.DeleteCompartmentRequest;
import com.oracle.bmc.identity.requests.GetCompartmentRequest;
import com.oracle.bmc.identity.requests.ListAuthTokensRequest;
import com.oracle.bmc.identity.requests.ListAvailabilityDomainsRequest;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.requests.ListRegionSubscriptionsRequest;
import com.oracle.bmc.identity.responses.CreateCompartmentResponse;
import com.oracle.bmc.identity.responses.GetCompartmentResponse;
import com.oracle.bmc.identity.responses.ListAuthTokensResponse;
import com.oracle.bmc.identity.responses.ListAvailabilityDomainsResponse;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import com.oracle.bmc.identity.responses.ListRegionSubscriptionsResponse;
import com.oracle.bmc.keymanagement.KmsManagementClient;
import com.oracle.bmc.keymanagement.KmsVaultClient;
import com.oracle.bmc.keymanagement.model.KeySummary;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.bmc.keymanagement.requests.ListKeysRequest;
import com.oracle.bmc.keymanagement.requests.ListVaultsRequest;
import com.oracle.bmc.keymanagement.responses.ListKeysResponse;
import com.oracle.bmc.keymanagement.responses.ListVaultsResponse;
import com.oracle.bmc.resourcemanager.ResourceManagerClient;
import com.oracle.oci.intellij.ui.appstack.AppStackDashboard;
import com.oracle.oci.intellij.ui.common.AutonomousDatabaseConstants;
import com.oracle.oci.intellij.ui.database.AutonomousDatabasesDashboard;
import com.oracle.oci.intellij.util.BundleUtil;
import com.oracle.oci.intellij.util.LogHandler;

/**
 * The Oracle Cloud account configurator and accessor.
 */
public class OracleCloudAccount {
  public static final String ROOT_COMPARTMENT_NAME = "[Root Compartment]";
  private static final AtomicReference<OracleCloudAccount> ORACLE_CLOUD_ACCOUNT_INSTANCE = new AtomicReference<>();

  private AuthenticationDetailsProvider authenticationDetailsProvider = null;
  private final IdentityClientProxy identityClientProxy = new IdentityClientProxy();
  private final DatabaseClientProxy databaseClientProxy = new DatabaseClientProxy();
  private final VirtualNetworkClientProxy virtualNetworkClientProxy = new VirtualNetworkClientProxy();
  private final ResourceManagerClientProxy resourceManagerClientProxy = new ResourceManagerClientProxy();

  private OracleCloudAccount() {
    // Add the property change listeners in the order they have to be notified.
    SystemPreferences.addPropertyChangeListener(identityClientProxy);
    SystemPreferences.addPropertyChangeListener(databaseClientProxy);
    SystemPreferences.addPropertyChangeListener(AutonomousDatabasesDashboard.getInstance());
    SystemPreferences.addPropertyChangeListener(AppStackDashboard.getInstance());
    // TODO: property change listener for resource manager
  }

  /**
   * The singleton instance.
   * @return singleton instance.
   */
  public synchronized static OracleCloudAccount getInstance() {
      OracleCloudAccount value = ORACLE_CLOUD_ACCOUNT_INSTANCE.get();
      BundleUtil.withContextCL(OracleCloudAccount.class.getClassLoader(),
        new Runnable() {
          @Override
          public void run() {
            if (value == null) {
              ORACLE_CLOUD_ACCOUNT_INSTANCE.set(new OracleCloudAccount());
            }
          }
      });
      return ORACLE_CLOUD_ACCOUNT_INSTANCE.get();
    }

  public void configure(String configFile, String givenProfile) throws IOException {
    reset();

    final String fallbackProfileName = SystemPreferences.DEFAULT_PROFILE_NAME;
    final ConfigFileHandler.ProfileSet profileSet = ConfigFileHandler.parse(configFile);

    ConfigFileHandler.Profile profile;
    if (profileSet.containsKey(givenProfile)) {
      profile = profileSet.get(givenProfile);
      authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(configFile, givenProfile);
    } else {
      // Last used profile is not found. Use the default profile.
      if (profileSet.containsKey(fallbackProfileName)) {
        LogHandler.warn(String.format("The profile %s isn't found in config file %s. Switched to profile %s.",
                givenProfile,
                configFile,
                fallbackProfileName));
        profile = profileSet.get(fallbackProfileName);
        authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(configFile, fallbackProfileName);
      } else {
        // Even the default profile is not found in the given config.
        throw new IllegalStateException(
                String.format("The profile %s isn't found in the config file %s.", givenProfile, configFile));
      }
    }
    setClientUserAgent(getUserAgent());

    identityClientProxy.init(profile.get("region"));
    databaseClientProxy.init(profile.get("region"));
    virtualNetworkClientProxy.init(profile.get("region"));
    resourceManagerClientProxy.init(profile.get("region"));
    SystemPreferences.setConfigInfo(configFile, profile.getName(),
            profile.get("region"), identityClientProxy.getRootCompartment());
  }

  private void validate() {
    if (authenticationDetailsProvider == null) {
      throw new IllegalStateException("Configure Oracle Cloud account first.");
    }
  }

  public IdentityClientProxy getIdentityClient() {
    validate();
    return identityClientProxy;
  }

  public DatabaseClientProxy getDatabaseClient() {
    validate();
    return databaseClientProxy;
  }


  public VirtualNetworkClientProxy getVirtualNetworkClientProxy() {
    validate();
    return virtualNetworkClientProxy;
  }

  public ResourceManagerClientProxy getResourceManagerClientProxy() {
    validate();
    return resourceManagerClientProxy;
  }
  public String  getCurrentUserId(){
    return authenticationDetailsProvider.getUserId();
  }
  public String getCurrentTenancy(){
    return authenticationDetailsProvider.getTenantId();
  }


  private void reset() {
    authenticationDetailsProvider = null;
  }

  /**
   * The reference of {@link IdentityClient} changes whenever a new profile is loaded.
   * This proxy class holds the updated reference of {@link IdentityClient} object.
   */
  public class IdentityClientProxy implements PropertyChangeListener {
    private IdentityClient identityClient;

    // Instance of this should be taken from the outer class factory method only.
    private IdentityClientProxy() {
    }

    void init(String region) {
      reset();
      identityClient = IdentityClient.builder().build(authenticationDetailsProvider);
      identityClient.setRegion(region);
    }

    /**
     * Creates a new compartment under the given parent compartment.
     * @param parentCompartmentId the id of parent compartment
     * @param compartmentName the name to be given to new compartment.
     * @param description the description of new compartment.
     * @return the new compartment.
     */
    public Compartment createCompartment(String parentCompartmentId, String compartmentName, String description) {
      final CreateCompartmentRequest.Builder createCompartmentRequestBuilder =
              CreateCompartmentRequest.builder();

      createCompartmentRequestBuilder.createCompartmentDetails(CreateCompartmentDetails.builder()
              .compartmentId(parentCompartmentId)
              .name(compartmentName)
              .description(description)
              .build()
      );
      final CreateCompartmentResponse createCompartmentResponse =
              identityClient.createCompartment(createCompartmentRequestBuilder.build());

      return createCompartmentResponse.getCompartment();
    }
    public List<RepositorySummary> getRepoList(String compartmentId){
      /* Create a service client */
      DevopsClient client = DevopsClient.builder().build(authenticationDetailsProvider);

      /* Create a request and dependent object(s). */

      ListRepositoriesRequest listRepositoriesRequest = ListRepositoriesRequest.builder()
              .compartmentId(compartmentId)
              .build();

      /* Send request to the Client */
      ListRepositoriesResponse response = client.listRepositories(listRepositoriesRequest);
      return response.getRepositoryCollection().getItems();

    }
    public List<AuthToken> getAuthTokenList(){
      ListAuthTokensRequest listAuthTokensRequest = ListAuthTokensRequest.builder().userId(authenticationDetailsProvider.getUserId()).build();
      ListAuthTokensResponse listAuthTokensResponse = identityClient.listAuthTokens(listAuthTokensRequest);
      return listAuthTokensResponse.getItems();
    }

    public List<CertificateSummary>getAllCertificates(String compartmentId){
      CertificatesManagementClient client = CertificatesManagementClient.builder().build(authenticationDetailsProvider);

      /* Create a request and dependent object(s). */

      ListCertificatesRequest listCertificatesRequest = ListCertificatesRequest.builder()
              .compartmentId(compartmentId)
             .build();

      /* Send request to the Client */
      ListCertificatesResponse response = client.listCertificates(listCertificatesRequest);
      return response.getCertificateCollection().getItems();
    }

    public List<ZoneSummary> getAllDnsZone(String compartmentId){
      DnsClient client = DnsClient.builder().build(authenticationDetailsProvider);

      /* Create a request and dependent object(s). */

      ListZonesRequest listZonesRequest = ListZonesRequest.builder()
              .compartmentId(compartmentId)
              .build();

      /* Send request to the Client */
      ListZonesResponse response = client.listZones(listZonesRequest);
      return response.getItems() ;
    }

    /**
     * Deletes the given compartment.
     * @param compartmentId the id of compartment to be deleted.
     */
    public void deleteCompartment(String compartmentId) {
      identityClient.deleteCompartment(
              DeleteCompartmentRequest.builder()
                      .compartmentId(compartmentId).build());
    }

    /**
     * Returns the root compartment.
     * @return the root compartment.
     */
    public Compartment getRootCompartment() {
      String compartmentId = authenticationDetailsProvider.getTenantId();
      Compartment rootComp = Compartment.builder()
              .compartmentId(compartmentId)
              .id(compartmentId)
              .name(ROOT_COMPARTMENT_NAME)
              .lifecycleState(LifecycleState.Active)
              .build();
      return rootComp;
    }

    public List<Compartment> getCompartmentList(Compartment compartment) {
      List<Compartment> compartmentList = new ArrayList<Compartment>();

      try {
          ListCompartmentsResponse response = identityClient
                  .listCompartments(ListCompartmentsRequest.builder().compartmentId(compartment.getId())
                          .accessLevel(ListCompartmentsRequest.AccessLevel.Accessible).build());
          if (response != null) {
              compartmentList = response.getItems()
                      .stream()
                      .filter(predicate -> !predicate.getLifecycleState().equals(LifecycleState.Deleted))
                      .filter(predicate -> !predicate.getLifecycleState().equals(LifecycleState.Deleting))
                      .sorted(Comparator.comparing(Compartment::getName))
                      .collect(Collectors.toList());
          }
      } catch (Exception ex) {
          ex.printStackTrace();
      }


      return compartmentList;
  }

//
//    public List<Compartment> getCompartmentList(String compartmentId) {
//      final List<Compartment> compartmentList = new ArrayList<>();
//
//      final ListCompartmentsResponse response = identityClient.listCompartments(
//              ListCompartmentsRequest.builder()
//                      .compartmentId(compartmentId)
//                      .lifecycleState(Compartment.LifecycleState.Active)
//                      .accessLevel(ListCompartmentsRequest.AccessLevel.Accessible)
//                      .build());
//
//      if (response != null) {
//        compartmentList.addAll(response.getItems());
//      }
//      return compartmentList;
//    }

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

    public Compartment getCompartment(String compartmentId) {
      final GetCompartmentRequest request = GetCompartmentRequest.builder().compartmentId(compartmentId).build();
      final GetCompartmentResponse response = identityClient.getCompartment(request);
      return response.getCompartment();
    }

    public List<AvailabilityDomain> getAvailabilityDomainsList(String compartmentId){
      ListAvailabilityDomainsRequest listAvailabilityDomainsRequest = ListAvailabilityDomainsRequest.builder()
              .compartmentId(compartmentId).build();

      /* Send request to the Client */
      ListAvailabilityDomainsResponse response = identityClient.listAvailabilityDomains(listAvailabilityDomainsRequest);
      return response.getItems();
    }

    public List<VaultSummary> getVaultsList(String compartmentId){
      if (authenticationDetailsProvider != null) {
        KmsVaultClient client = KmsVaultClient.builder().build(authenticationDetailsProvider);

        ListVaultsRequest listVaultsRequest = ListVaultsRequest.builder()
                .compartmentId(compartmentId)
                .sortBy(ListVaultsRequest.SortBy.Timecreated)
                .sortOrder(ListVaultsRequest.SortOrder.Desc).build();

        /* Send request to the Client */
        ListVaultsResponse response = client.listVaults(listVaultsRequest);
        return response.getItems();
      }
      return null;
    }

    public List<KeySummary> getKeyList(String compartmentId,VaultSummary vault){
      if (authenticationDetailsProvider != null) {
        KmsManagementClient kmsManagementClient = KmsManagementClient.builder().
        vaultSummary(vault)
        .build(authenticationDetailsProvider);


        ListKeysRequest listKeysRequest = ListKeysRequest.builder()
                .compartmentId(compartmentId)
                .build();
        ListKeysResponse response = kmsManagementClient.listKeys(listKeysRequest);

        List<KeySummary> keys = response.getItems();
        List<KeySummary> vaultKeys = new ArrayList<>();
        for (KeySummary key : keys){
          if (key.getVaultId().equals(vault.getId())){
              vaultKeys.add(key);
          }
        }

        return vaultKeys;
        }
      return null;
      }



    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
      LogHandler.info("IdentityClientProxy: Handling the event update : " + propertyChangeEvent.toString());
      if (propertyChangeEvent.getPropertyName()
              .equals(SystemPreferences.EVENT_REGION_UPDATE)) {
        identityClient.setRegion(propertyChangeEvent.getNewValue().toString());
      }
    }


    private void reset() {
      if (identityClient != null) {
        identityClient.close();
        identityClient = null;
      }
    }

  }

  /**
   * The reference of {@link DatabaseClient} changes whenever a new profile is loaded.
   * This proxy class holds the updated reference of {@link DatabaseClient} object.
   */
  public class DatabaseClientProxy implements PropertyChangeListener {
    private final Map<String, AutonomousDatabaseSummary> instancesMap = new LinkedHashMap<>();
    private DatabaseClient databaseClient;

    // Instance of this should be taken from the outer class factory method only.
    private DatabaseClientProxy() {
    }

    void init(String region) {
      reset();
      databaseClient = DatabaseClient.builder().build(authenticationDetailsProvider);
      databaseClient.setRegion(region);
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
      LogHandler.info("DatabaseClientProxy: Handling the event update : " + propertyChangeEvent.toString());

      if (propertyChangeEvent.getPropertyName().equals(SystemPreferences.EVENT_REGION_UPDATE)) {
        databaseClient.setRegion(propertyChangeEvent.getNewValue().toString());
      }
    }

    public List<AutonomousDatabaseSummary> getAutonomousDatabaseInstances(AutonomousDatabaseSummary.DbWorkload workloadType) {
      LogHandler.info("Fetching Autonomous Database instances.");

      String currentCompartmentId = SystemPreferences.getCompartmentId() == null ?
              authenticationDetailsProvider.getTenantId() : SystemPreferences.getCompartmentId();

      final ListAutonomousDatabasesRequest listAutonomousDatabasesRequest =
              ListAutonomousDatabasesRequest.builder()
                      .compartmentId(currentCompartmentId)
                      .dbWorkload(workloadType)
                      .sortBy(ListAutonomousDatabasesRequest.SortBy.Timecreated)
                      .sortOrder(ListAutonomousDatabasesRequest.SortOrder.Desc)
                      .build();

      final ListAutonomousDatabasesResponse response =
              databaseClient.listAutonomousDatabases(listAutonomousDatabasesRequest);

      List<AutonomousDatabaseSummary> instances = new ArrayList<>();

      if (response == null || response.getItems() == null) {
        LogHandler.warn("Received null response for Autonomous Database instances.");
      } else {
        instances = response.getItems();
        LogHandler.info("Number of Autonomous Database instances received = " + instances.size());

        final Iterator<AutonomousDatabaseSummary> iterator = instances.iterator();
        while (iterator.hasNext()) {
          final AutonomousDatabaseSummary instance = iterator.next();
          if (instance.getLifecycleState().equals(AutonomousDatabaseSummary.LifecycleState.Terminated)) {
            iterator.remove();
          } else {
            instancesMap.put(instance.getId(), instance);
          }
        }
      }
      return instances;
    }

    public AutonomousDatabase getDatabaseInfo(final AutonomousDatabaseSummary instance) {
        GetAutonomousDatabaseRequest request = GetAutonomousDatabaseRequest.builder().
                autonomousDatabaseId(instance.getId()).build();
        GetAutonomousDatabaseResponse autonomousDatabase = databaseClient.getAutonomousDatabase(request);
        return autonomousDatabase.getAutonomousDatabase();
    }

    public void startInstance(final AutonomousDatabaseSummary instance) {
      LogHandler.info("Starting the Autonomous Database instance : " + instance.getId());
      databaseClient.startAutonomousDatabase(StartAutonomousDatabaseRequest.builder()
              .autonomousDatabaseId(instance.getId())
              .build())
              .getAutonomousDatabase();
      LogHandler.info("Started Autonomous Database instance : " + instance.getId());
    }

    public void stopInstance(final AutonomousDatabaseSummary instance){
      LogHandler.info("Stopping the Autonomous Database instance : " + instance.getId());
      databaseClient.stopAutonomousDatabase(
              StopAutonomousDatabaseRequest.builder()
                      .autonomousDatabaseId(instance.getId()).build())
              .getAutonomousDatabase();
      LogHandler.info("Stopped Autonomous Database instance : " + instance.getId());
    }

    public void changeAdminPassword(final AutonomousDatabaseSummary instance, String password) {
      final UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails.builder()
              .adminPassword(password).build();

      LogHandler.info("Change Admin password Autonomous Database instance : " + instance.getId());
      databaseClient.updateAutonomousDatabase(UpdateAutonomousDatabaseRequest.builder()
              .updateAutonomousDatabaseDetails(updateRequest)
              .autonomousDatabaseId(instance.getId())
              .build());
      LogHandler.info("ChangeAdminPassword ADB Instance : " + instance.getId() + " success.");
    }

    public void scaleUpDownInstance(final AutonomousDatabaseSummary instance,
                                    int cpuCoreCount, int dataStorageSizeInTBs,
                                    final Boolean isAutoScalingEnabled){
      final UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails.builder()
              .cpuCoreCount(cpuCoreCount)
              .dataStorageSizeInTBs(dataStorageSizeInTBs)
              .isAutoScalingEnabled(isAutoScalingEnabled)
              .build();

      LogHandler.info("Scale Up / Down Autonomous Database instance : " + instance.getId());

      databaseClient.updateAutonomousDatabase(
              UpdateAutonomousDatabaseRequest.builder()
                      .updateAutonomousDatabaseDetails(updateRequest)
                      .autonomousDatabaseId(instance.getId()).build());

      LogHandler.info("ScaleUpDown ADB Instance : " + instance.getId() + " success.");
    }

    public void updateLicenseType(final AutonomousDatabaseSummary instance,
                                  final UpdateAutonomousDatabaseDetails.LicenseModel licenseModel) {
      final UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails.builder()
              .licenseModel(licenseModel)
              .build();

      LogHandler.info("Update License Type Autonomous Database instance : " + instance.getId());
      databaseClient.updateAutonomousDatabase(
              UpdateAutonomousDatabaseRequest.builder()
                      .updateAutonomousDatabaseDetails(updateRequest)
                      .autonomousDatabaseId(instance.getId()).build());
      LogHandler.info("UpdateLicenseType ADB Instance : " + instance.getId() + " success.");
    }

    public void changeWorkloadTypeToOLTP(final String instanceId) {
      final UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails.builder()
              .dbWorkload(UpdateAutonomousDatabaseDetails.DbWorkload.Oltp)
              .build();
      LogHandler.info("Change workload type for Autonomous Database instance : " + instanceId);
      databaseClient.updateAutonomousDatabase(UpdateAutonomousDatabaseRequest.builder()
              .updateAutonomousDatabaseDetails(updateRequest).autonomousDatabaseId(instanceId).build());
      LogHandler.info("Update License Type Autonomous Database instance : " + instanceId + " success.");
    }

    public void createClone(CreateAutonomousDatabaseCloneDetails cloneRequest) {
      LogHandler.info("Creating clone source ID : " + cloneRequest.getSourceId());
      databaseClient.createAutonomousDatabase(CreateAutonomousDatabaseRequest.builder()
              .createAutonomousDatabaseDetails(cloneRequest)
              .build());

      LogHandler.info("Creating clone source ID : " + cloneRequest.getSourceId() + " success.");
    }

    public void createInstance(final CreateAutonomousDatabaseDetails request) {
      LogHandler.info("Creating Autonomous Database instance");
      databaseClient.createAutonomousDatabase(
              CreateAutonomousDatabaseRequest.builder()
                      .createAutonomousDatabaseDetails(request).build());

      LogHandler.info("Autonomous Database created successfully.");
    }

    public void terminate(final String databaseId) {
      LogHandler.info("Terminating Autonomous Database instance : " + databaseId);
      databaseClient.deleteAutonomousDatabase(
              DeleteAutonomousDatabaseRequest.builder()
                      .autonomousDatabaseId(databaseId).build());
      LogHandler.info("Autonomous Database instance : " + databaseId + " is terminated successfully.");
    }

    public void downloadWallet(final AutonomousDatabaseSummary instance,
                               final String walletType, final String password,
                               final String walletDirectory) {

      final GenerateAutonomousDatabaseWalletDetails walletDetails;
      if ((instance.getIsDedicated() != null && instance.getIsDedicated())) {
        walletDetails = GenerateAutonomousDatabaseWalletDetails.builder()
                .password(password).build();
      } else {
        final GenerateAutonomousDatabaseWalletDetails.GenerateType type =
                AutonomousDatabaseConstants.REGIONAL_WALLET.equalsIgnoreCase(walletType) ?
                        GenerateAutonomousDatabaseWalletDetails.GenerateType.All :
                        GenerateAutonomousDatabaseWalletDetails.GenerateType.Single;
        walletDetails = GenerateAutonomousDatabaseWalletDetails.builder()
                .password(password).generateType(type).build();
      }

      final GenerateAutonomousDatabaseWalletResponse adbWalletResponse = databaseClient
              .generateAutonomousDatabaseWallet(
                      GenerateAutonomousDatabaseWalletRequest.builder()
                              .generateAutonomousDatabaseWalletDetails(walletDetails)
                              .autonomousDatabaseId(instance.getId()).build());

      final File directory = new File(walletDirectory);
      if (!directory.exists()) {
        if (!directory.mkdir()) {
          LogHandler.error("Cannot create wallet directory : " + walletDirectory);
          return;
        }
      } else {
        LogHandler.info("Wallet directory already exists : " + walletDirectory);
        try {
          FileUtils.cleanDirectory(directory);
        } catch (IOException e) {
          LogHandler.info("Failed to clean the existing wallet directory : " + walletDirectory);
        }
      }

      final Path outDir = Paths.get(walletDirectory);
      final byte[] buffer = new byte[2048];
      try (ZipInputStream zipInputStream = new ZipInputStream(adbWalletResponse.getInputStream())) {
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
          final Path filePath = outDir.resolve(entry.getName());
          final File file = filePath.toFile();

          try (FileOutputStream fos = new FileOutputStream(file);
               BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length)) {

            int len;
            while ((len = zipInputStream.read(buffer)) > 0) {
              bos.write(buffer, 0, len);
            }
          }
        }
        LogHandler.info("Downloaded Client Credentials (Wallet) for database: " + instance.getId());
      } catch (Exception ex) {
        LogHandler.error("Error occurred while downloading wallet for ADB: " + instance.getId(), ex);
        throw new RuntimeException(ex);
      }
    }

    public List<AutonomousDatabaseBackupSummary> getBackupList(final AutonomousDatabaseSummary instance) {
      ListAutonomousDatabaseBackupsResponse response;
      LogHandler.info("Getting backup list for Autonomous Database instance : " + instance.getId());

      final ListAutonomousDatabaseBackupsRequest request = ListAutonomousDatabaseBackupsRequest
              .builder().autonomousDatabaseId(instance.getId())
              .lifecycleState(AutonomousDatabaseBackupSummary.LifecycleState.Active)
              .sortBy(ListAutonomousDatabaseBackupsRequest.SortBy.Timecreated)
              .sortOrder(ListAutonomousDatabaseBackupsRequest.SortOrder.Desc)
              .build();
      response = databaseClient.listAutonomousDatabaseBackups(request);

      if (response != null) {
        final List<AutonomousDatabaseBackupSummary> autonomousDatabaseBackupSummaryList = response.getItems();
        LogHandler.info("Got backup list. Size : " + autonomousDatabaseBackupSummaryList.size());
        return autonomousDatabaseBackupSummaryList;
      } else {
        LogHandler.info("No backup list found for ADBInstance : " + instance.getId());
        return Collections.emptyList();
      }
    }

    public void restore(final String autonomousDatabaseId, final Date restoreTimestamp) {
      LogHandler.info("Restoring ADBInstance " + autonomousDatabaseId + " to the timestamp : "
              + restoreTimestamp.toString());

      final RestoreAutonomousDatabaseDetails restoreDetail = RestoreAutonomousDatabaseDetails
              .builder().timestamp(restoreTimestamp).build();
      final RestoreAutonomousDatabaseRequest request = RestoreAutonomousDatabaseRequest
              .builder().restoreAutonomousDatabaseDetails(restoreDetail)
              .autonomousDatabaseId(autonomousDatabaseId).build();
      databaseClient.restoreAutonomousDatabase(request);
      LogHandler.info("Restoring ADBInstance " + autonomousDatabaseId + " to the timestamp : "
              + restoreTimestamp + " is successful.");
    }

    public void rotateWallet(final String instanceId, final String walletType){
      try {
        LogHandler.info("Rotating wallet for the ADBInstance " + instanceId + ", wallet type " + walletType);
        final UpdateAutonomousDatabaseWalletDetails details = UpdateAutonomousDatabaseWalletDetails
                .builder().shouldRotate(Boolean.TRUE).build();
        if (AutonomousDatabaseConstants.REGIONAL_WALLET.equalsIgnoreCase(walletType)) {
          final UpdateAutonomousDatabaseRegionalWalletRequest request = UpdateAutonomousDatabaseRegionalWalletRequest
                  .builder().updateAutonomousDatabaseWalletDetails(details).build();
          databaseClient.updateAutonomousDatabaseRegionalWallet(request);
          LogHandler.info("Wallet rotation successful for the ADBInstance " + instanceId
                  + ", wallet type " + walletType);
        } else if (AutonomousDatabaseConstants.INSTANCE_WALLET.equalsIgnoreCase(walletType)) {
          final UpdateAutonomousDatabaseWalletRequest request = UpdateAutonomousDatabaseWalletRequest
                  .builder().updateAutonomousDatabaseWalletDetails(details)
                  .autonomousDatabaseId(instanceId).build();
          databaseClient.updateAutonomousDatabaseWallet(request);
          LogHandler.info("Wallet rotation successful for the ADBInstance " + instanceId
                  + ", wallet type " + walletType);
        } else {
          LogHandler.error("Unknown wallet type selected for rotation");
        }
      } catch (Exception ex) {
        LogHandler.error("Wallet rotation failed for the ADBInstance " + instanceId
                + ", wallet type " + walletType, ex);
        throw ex;
      }
    }

    public Map<String, AutonomousDatabaseWallet> getWalletType(
            final AutonomousDatabaseSummary instance) {
      final Map<String, AutonomousDatabaseWallet> walletTypeMap = new HashMap<>();
      if (!(instance.getIsDedicated() != null && instance.getIsDedicated())) {
        try {
          final GetAutonomousDatabaseRegionalWalletRequest regionalWalletRequest =
                  GetAutonomousDatabaseRegionalWalletRequest.builder().build();
          final GetAutonomousDatabaseRegionalWalletResponse regionalWalletResponse =
                  databaseClient.getAutonomousDatabaseRegionalWallet(regionalWalletRequest);
          final AutonomousDatabaseWallet regionalWallet =
                  regionalWalletResponse.getAutonomousDatabaseWallet();
          walletTypeMap.put(AutonomousDatabaseConstants.REGIONAL_WALLET, regionalWallet);
        } catch (Exception ex) {
          LogHandler.error("Failed to fetch Regional Wallet details. " + ex.getMessage());
          throw ex;
        }

        try {
          final GetAutonomousDatabaseWalletRequest walletRequest = GetAutonomousDatabaseWalletRequest
                  .builder().autonomousDatabaseId(instance.getId()).build();
          final GetAutonomousDatabaseWalletResponse walletResponse = databaseClient
                  .getAutonomousDatabaseWallet(walletRequest);
          final AutonomousDatabaseWallet instanceWallet = walletResponse
                  .getAutonomousDatabaseWallet();
          walletTypeMap.put(AutonomousDatabaseConstants.INSTANCE_WALLET, instanceWallet);
        } catch (Exception ex) {
          LogHandler.error("Failed to fetch wallet details. " + ex.getMessage());
          throw ex;
        }
      }

      return walletTypeMap;
    }

    public List<DbVersionSummary> getDatabaseVersions(String compartmentId) {
      final ListDbVersionsRequest listDbVersionsRequest =
              ListDbVersionsRequest.builder()
                      .compartmentId(compartmentId)
                      .build();

      ListDbVersionsResponse listDbVersionsResponse =
              databaseClient.listDbVersions(listDbVersionsRequest);

      return listDbVersionsResponse.getItems();
    }

    public void updateRequiresMTLS(AutonomousDatabaseSummary autonomousDatabaseSummary, boolean requireMTLS) {
        UpdateAutonomousDatabaseDetails updateRequest = 
                UpdateAutonomousDatabaseDetails.builder().isMtlsConnectionRequired(Boolean.valueOf(requireMTLS))
                    .build();
        databaseClient.updateAutonomousDatabase(UpdateAutonomousDatabaseRequest.builder()
                .updateAutonomousDatabaseDetails(updateRequest).autonomousDatabaseId(autonomousDatabaseSummary.getId()).build());
    }

    public void updateAcl(AutonomousDatabaseSummary autonomousDatabaseSummary, List<String> whitelistIps) {
        // Per UpdateAutonomousDatabaseDetails.whitelistedIps, if you want to clear all
        // then set a single empty string.
        if (whitelistIps.isEmpty())
        {
            whitelistIps = new ArrayList<>();
            whitelistIps.add("");
        }
        UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails.builder()
            .whitelistedIps(whitelistIps).build();
        databaseClient.updateAutonomousDatabase(UpdateAutonomousDatabaseRequest.builder()
                .updateAutonomousDatabaseDetails(updateRequest).autonomousDatabaseId(autonomousDatabaseSummary.getId()).build());
    }

    public List<AutonomousDatabaseSummary> getAutonomousDatabaseList(String compartementId){
      LogHandler.info("Fetching Autonomous Database instances.");


      final ListAutonomousDatabasesRequest listAutonomousDatabasesRequest =
              ListAutonomousDatabasesRequest.builder()
                      .compartmentId(compartementId)
                      .sortBy(ListAutonomousDatabasesRequest.SortBy.Timecreated)
                      .sortOrder(ListAutonomousDatabasesRequest.SortOrder.Desc)
                      .build();

      final ListAutonomousDatabasesResponse response =
              databaseClient.listAutonomousDatabases(listAutonomousDatabasesRequest);
      return response.getItems();
    }

    public AutonomousDatabase getAutonomousDatabaseSummary(String compartmentId, String dbId) {
      final GetAutonomousDatabaseRequest getAutonomousDatabaseRequest =
            GetAutonomousDatabaseRequest.builder().autonomousDatabaseId(dbId).build();

      GetAutonomousDatabaseResponse autonomousDatabase =
        databaseClient.getAutonomousDatabase(getAutonomousDatabaseRequest);
      return autonomousDatabase.getAutonomousDatabase();
    }

    public AutonomousDatabaseSummary getAutonomousDatabaseSummary(String instanceId) {
      return instancesMap.get(instanceId);
    }

    private void reset() {
      if (databaseClient != null) {
        databaseClient.close();
        databaseClient = null;
        instancesMap.clear();
      }
    }

  }

  public class VirtualNetworkClientProxy {
    private VirtualNetworkClient virtualNetworkClient;

    // Instance of this should be taken from the outer class factory method only.
    private VirtualNetworkClientProxy() {
    }

    void init(String region) {
      reset();
      virtualNetworkClient = VirtualNetworkClient.builder().build(authenticationDetailsProvider);
      virtualNetworkClient.setRegion(region);
    }

    public List<Vcn> listVcns(String compartmentId) {
      final ListVcnsRequest listVcnsRequest =
              ListVcnsRequest.builder()
                      .compartmentId(compartmentId)
                      .lifecycleState(Vcn.LifecycleState.Available)
                      .build();
      final ListVcnsResponse listVcnsResponse = virtualNetworkClient.listVcns(listVcnsRequest);
      return listVcnsResponse.getItems();
    }
    
    public Vcn getVcn(String vcnId)
    {
        if (virtualNetworkClient == null)
        {
            return null;
        }
        GetVcnRequest request = GetVcnRequest.builder().vcnId(vcnId).build();
        
        GetVcnResponse response = null;
        try {
            response = this.virtualNetworkClient.getVcn(request);
        } catch(Throwable e) {
            // To handle forbidden error
            // TODO: ErrorHandler.logError("Unable to list Autonomous Databases: "+e.getMessage());
        }

        if (response == null) {
            return null;
        }
        return response.getVcn();
    }

    public List<Subnet> listSubnets(String compartmentId) {
      final ListSubnetsRequest listSubnetsRequest =
              ListSubnetsRequest.builder()
                      .compartmentId(compartmentId)
                      .lifecycleState(Subnet.LifecycleState.Available)
                      .build();
      final ListSubnetsResponse listSubnetsResponse = virtualNetworkClient.listSubnets(listSubnetsRequest);
      return listSubnetsResponse.getItems();
    }

    public List<Subnet> listSubnets(String compartmentId,String vcnId,boolean hidePublicSubnet) {
      final ListSubnetsRequest listSubnetsRequest =
              ListSubnetsRequest.builder()
                      .compartmentId(compartmentId)
                      .vcnId(vcnId)

                      .lifecycleState(Subnet.LifecycleState.Available)
                      .build();
      final ListSubnetsResponse listSubnetsResponse = virtualNetworkClient.listSubnets(listSubnetsRequest);
      return listSubnetsResponse.getItems().stream().filter((e)->e.getProhibitPublicIpOnVnic() == hidePublicSubnet).collect(Collectors.toList());
    }

    public List<NetworkSecurityGroup> listNetworkSecurityGroups(String compartmentId) {
      final ListNetworkSecurityGroupsRequest listNetworkSecurityGroupsRequest =
              ListNetworkSecurityGroupsRequest.builder()
                      .compartmentId(compartmentId)
                      .lifecycleState(NetworkSecurityGroup.LifecycleState.Available)
                      .build();
      final ListNetworkSecurityGroupsResponse listNetworkSecurityGroupsResponse =
              virtualNetworkClient.listNetworkSecurityGroups(listNetworkSecurityGroupsRequest);
      return listNetworkSecurityGroupsResponse.getItems();
    }

    private void reset() {
      if (virtualNetworkClient != null) {
        virtualNetworkClient.close();
        virtualNetworkClient = null;
      }
    }
  }

  public class ResourceManagerClientProxy {
    private ResourceManagerClient resourceManagerClient;

    // Instance of this should be taken from the outer class factory method only.
    private ResourceManagerClientProxy() {
    }

    void init(String region) {
      reset();
      resourceManagerClient = ResourceManagerClient.builder().build(authenticationDetailsProvider);
      resourceManagerClient.setRegion(region);
    }

    public void deleteStack(String stackId) throws Exception{
      // todo before deleting check if there is no job is applying right now
      // Delete Stack
      final DeleteStackRequest deleteStackRequest = DeleteStackRequest.builder().stackId(stackId).build();
      final DeleteStackResponse deleteStackResponse = resourceManagerClient.deleteStack(deleteStackRequest);
      System.out.println("Deleted Stack : " + deleteStackResponse.toString());
    }

    public CreateJobResponse destroyStack(String stackId) {
      //    remove the artifact before destroying resources ....
      ListStackAssociatedResourcesRequest resourcesRequest = ListStackAssociatedResourcesRequest.builder()
                      .stackId(stackId)
                              .build();
      ListStackAssociatedResourcesResponse response = resourceManagerClient.listStackAssociatedResources(resourcesRequest);
      AssociatedResourcesCollection associatedResourcesCollection = response.getAssociatedResourcesCollection();
      AssociatedResourceSummary artifactRepo = null ;
      for (AssociatedResourceSummary associatedResourceSummary :associatedResourcesCollection.getItems()){
        if (associatedResourceSummary.getResourceType().equals("oci_artifacts_repository")){
          artifactRepo = associatedResourceSummary;
          break;
        }
      }
      if (artifactRepo != null){
        String artifactRegistryId = artifactRepo.getResourceId();
        String compartment_id = artifactRepo.getAttributes().get("compartment_id");
        String status = deleteArtifact(artifactRegistryId, compartment_id.substring(1,compartment_id.length()-1));
        System.out.println("Delete Artifact:" + status);
      }

      // Destroy resources but don't remove stack defn
      CreateJobOperationDetails operationDetails =
        CreateDestroyJobOperationDetails.builder()
                                        .executionPlanStrategy(DestroyJobOperationDetails.ExecutionPlanStrategy.AutoApproved)
                                        .build();
      CreateJobDetails createDestroyJobDetails =
        CreateJobDetails.builder()
                        .stackId(stackId)
                        .jobOperationDetails(operationDetails)
                        .build();
      CreateJobRequest createPlanJobRequest =
        CreateJobRequest.builder()
                        .createJobDetails(createDestroyJobDetails)
                        .build();
      return resourceManagerClient.createJob(createPlanJobRequest);
    }

    private String deleteArtifact(String artifactRegistryId, String compartmentId) {
      ArtifactsClient client = ArtifactsClient.builder().region(getRegionName()).build(authenticationDetailsProvider);
      ListGenericArtifactsRequest listGenericArtifactsRequest = ListGenericArtifactsRequest.builder()
              .compartmentId(compartmentId)
              .repositoryId(artifactRegistryId)
              .build();
      ListGenericArtifactsResponse response = null;
      try {
        response = client.listGenericArtifacts(listGenericArtifactsRequest);
      } catch (BmcException e) {
        if (e.getStatusCode() == 404 && e.getServiceCode().equals("REPOSITORY_NOT_FOUND")) {
          return "FAILED";
        } else {
          System.out.println("hi");
        }
      } catch (Exception e) {
        System.out.println("hi");
      }

      for (GenericArtifactSummary item : response.getGenericArtifactCollection().getItems()) {
        if (!item.getLifecycleState().equals(GenericArtifact.LifecycleState.Available))
            continue;
        DeleteGenericArtifactRequest deleteGenericArtifactRequest = DeleteGenericArtifactRequest.builder()
                .artifactId(item.getId())
                .opcRequestId(UUID.randomUUID().toString()).build();
        //todo see if the artifact in the available mode
        DeleteGenericArtifactResponse deleteResponse = client.deleteGenericArtifact(deleteGenericArtifactRequest);
        int statusCode = deleteResponse.get__httpStatusCode__();
        if (statusCode < 200 || statusCode > 300) {
          return "FAILED";
        }
      }


      DeleteRepositoryRequest deleteRepositoryRequest = DeleteRepositoryRequest.builder()
              .repositoryId(artifactRegistryId)
              .build();

      /* Send request to the Client */
      client.deleteRepository(deleteRepositoryRequest);
      return "SUCCEDED";
    }

    public List<StackSummary> listStacks(String compartmentId) {
      ListStacksRequest listStackRequest = ListStacksRequest.builder().compartmentId(compartmentId).build();
      ListStacksResponse listStacks = this.resourceManagerClient.listStacks(listStackRequest);
      List<StackSummary> items = listStacks.getItems();
      return items;
    }
    public Stack getStackDetails(String stackId){
      GetStackRequest getStackRequest = GetStackRequest.builder()
              .stackId(stackId)
              .build();

      GetStackResponse response = this.resourceManagerClient.getStack(getStackRequest);
      return response.getStack();
    }
    
    public ListJobsResponse listJobs(String compartmentId, String stackId) {
      ListJobsRequest request = ListJobsRequest.builder().compartmentId(compartmentId)
              .sortBy(ListJobsRequest.SortBy.Timecreated)
              .sortOrder(ListJobsRequest.SortOrder.Desc)
              .stackId(stackId).build();
      return resourceManagerClient.listJobs(request);
    }

    public List<JobSummary> listRunningJobs(String stackId) {
      ListJobsRequest request = ListJobsRequest.builder()
              .stackId(stackId).build();

      ListJobsResponse listJobsResponse =  resourceManagerClient.listJobs(request);
      List<JobSummary> runningJobs = new ArrayList<>();
      for (JobSummary job:listJobsResponse.getItems()){
        if (job.getLifecycleState().equals(Job.LifecycleState.Accepted) ||
            job.getLifecycleState().equals(Job.LifecycleState.Canceling) ||
            job.getLifecycleState().equals(Job.LifecycleState.InProgress)) {

          runningJobs.add(job);
        }
      }
      return runningJobs;
    }

    public GetJobLogsResponse getJobLogs(String planJobId) {
      return getJobLogs(planJobId, 100);
    }

    public GetJobLogsResponse getJobLogs(String planJobId, int limit) {
      GetJobLogsRequest getJobLogsRequest = 
        GetJobLogsRequest.builder().jobId(planJobId).limit(limit).build();
      return resourceManagerClient.getJobLogs(getJobLogsRequest);
      
    }

    public Job getJobDetails (String jobId){

      /* Create a request and dependent object(s). */

      GetJobRequest getJobRequest = GetJobRequest.builder()
              .jobId(jobId)
              .build();

      /* Send request to the Client */
      GetJobResponse response = resourceManagerClient.getJob(getJobRequest);
      return response.getJob();

    }
    
    public GetJobLogsResponse getJobLogs(String jobId, int limit, String opcNextPage) {
      GetJobLogsRequest getJobLogsRequest = 
        GetJobLogsRequest.builder().page(opcNextPage).jobId(jobId).opcRequestId(opcNextPage).limit(limit).build();
      return resourceManagerClient.getJobLogs(getJobLogsRequest);
    }


    public GetJobTfStateResponse getJobTfState(String applyJobId) {
      GetJobTfStateRequest getJobTfStateRequest =
        GetJobTfStateRequest.builder().jobId(applyJobId).build();
      return resourceManagerClient.getJobTfState(getJobTfStateRequest);
    }

    public ListJobOutputsResponse listJobOutputs(String jobId) {
      ListJobOutputsRequest request =
        ListJobOutputsRequest.builder().jobId(jobId).build();
      return resourceManagerClient.listJobOutputs(request);
    }

    public CreateStackResponse createStack(Map<String, String> variables) throws IOException {
      return createStack(SystemPreferences.getCompartmentId(), variables);
    }
    
    public CreateStackResponse createStack(String compartmentId, Map<String, String> variables) throws IOException {
      CreateZipUploadConfigSourceDetails zipUploadConfigSourceDetails =
        CreateZipUploadConfigSourceDetails.builder()
        .zipFileBase64Encoded(getBase64EncodingForAFile("/Users/aallali/Downloads/appstackforjava.zip"))
        .build();
      String uuid = UUID.randomUUID().toString();
      String displayName = variables.get("appstack_name") == null ? "New App Stack "+uuid:variables.get("appstack_name");
      String description = variables.get("appstack_description") == null ? "New App Stack "+uuid:variables.get("appstack_description");
      CreateStackDetails stackDetails =
        CreateStackDetails.builder()
                          .compartmentId(compartmentId)
                          .configSource(zipUploadConfigSourceDetails)
                          .displayName(displayName)
                          .description(description)
                          .variables(variables == null ? Collections.emptyMap() : variables)
                          .build();
      CreateStackRequest createStackRequest =
        CreateStackRequest.builder().createStackDetails(stackDetails)
//        .opcRequestId("app-stack-test-create-stack-request-"
//          + UUID.randomUUID()
//              .toString())
//        .opcRetryToken("app-stack-test-create-stack-retry-token-" + UUID.randomUUID().toString())
          .build();
      CreateStackResponse createStackResponse =
        resourceManagerClient.createStack(createStackRequest);
      System.out.println("Created Stack : " + createStackResponse.getStack());
      final Stack stack = createStackResponse.getStack();

      System.out.println(stack.getId());
      return createStackResponse;
    }
    private String getBase64EncodingForAFile(String filePath) throws IOException {
      byte[] fileData = Files.readAllBytes(Paths.get(filePath));
      byte[] fileDataBase64Encoded = Base64.getEncoder().encode(fileData);
      return new String(fileDataBase64Encoded, StandardCharsets.UTF_8);
    }

    public CreateJobResponse submitJob(CreateJobRequest createPlanJobRequest) {
      String stackId =  createPlanJobRequest.getCreateJobDetails().getStackId();
      if (!listRunningJobs(stackId).isEmpty()){
        throw new JobRunningException(stackId);
      }
      return resourceManagerClient.createJob(createPlanJobRequest);

    }

    private void reset() {
      if (resourceManagerClient != null) {
        resourceManagerClient.close();
        resourceManagerClient = null;
      }
    }

  }
}
