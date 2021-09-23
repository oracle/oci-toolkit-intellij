/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.model.*;
import com.oracle.bmc.database.requests.*;
import com.oracle.bmc.database.responses.*;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.CreateCompartmentDetails;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.bmc.identity.requests.CreateCompartmentRequest;
import com.oracle.bmc.identity.requests.DeleteCompartmentRequest;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.requests.ListRegionSubscriptionsRequest;
import com.oracle.bmc.identity.responses.CreateCompartmentResponse;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import com.oracle.bmc.identity.responses.ListRegionSubscriptionsResponse;
import com.oracle.oci.intellij.ui.common.AutonomousDatabaseConstants;
import com.oracle.oci.intellij.ui.database.AutonomousDatabasesDashboard;
import com.oracle.oci.intellij.util.LogHandler;
import org.apache.commons.io.FileUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.oracle.bmc.ClientRuntime.setClientUserAgent;
import static com.oracle.oci.intellij.account.SystemPreferences.getUserAgent;

/**
 * The Oracle Cloud account configurator and accessor.
 */
public class OracleCloudAccount {
  private static final OracleCloudAccount ORACLE_CLOUD_ACCOUNT_INSTANCE = new OracleCloudAccount();

  private AuthenticationDetailsProvider authenticationDetailsProvider = null;
  private final IdentityClientProxy identityClientProxy = new IdentityClientProxy();
  private final DatabaseClientProxy databaseClientProxy = new DatabaseClientProxy();

  private OracleCloudAccount() {
    // Add the property change listeners in the order they have to be notified.
    SystemPreferences.addPropertyChangeListener(identityClientProxy);
    SystemPreferences.addPropertyChangeListener(databaseClientProxy);
    SystemPreferences.addPropertyChangeListener(AutonomousDatabasesDashboard.getInstance());
  }

  /**
   * The singleton instance.
   * @return singleton instance.
   */
  public static OracleCloudAccount getInstance() {
    return ORACLE_CLOUD_ACCOUNT_INSTANCE;
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
    SystemPreferences.setConfigInfo(configFile, profile.getName(), profile.get("region"));
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

  private void reset() {
    authenticationDetailsProvider = null;
  }

  /**
   * The reference of {@link IdentityClient} changes whenever a new profile is loaded.
   * This proxy class holds the updated reference of {@link IdentityClient} object.
   */
  public class IdentityClientProxy implements PropertyChangeListener {
    private static final String ROOT_COMPARTMENT_NAME = "root";
    private IdentityClient identityClient;

    // Instance of this should be taken from the outer class factory method only.
    private IdentityClientProxy() {
    }

    void init(String region) {
      reset();
      identityClient = new IdentityClient(authenticationDetailsProvider);
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
      String tenantId = authenticationDetailsProvider.getTenantId();
      return Compartment.builder()
              .compartmentId(tenantId)
              .id(tenantId)
              .name(ROOT_COMPARTMENT_NAME)
              .lifecycleState(Compartment.LifecycleState.Active)
              .build();
    }

    public List<Compartment> getCompartmentList(Compartment compartment) {
      final List<Compartment> compartmentList = new ArrayList<>();

      final ListCompartmentsResponse response = identityClient.listCompartments(
              ListCompartmentsRequest.builder()
                      .compartmentId(compartment.getId())
                      .lifecycleState(Compartment.LifecycleState.Active)
                      .accessLevel(ListCompartmentsRequest.AccessLevel.Accessible)
                      .build());

      if (response != null) {
        compartmentList.addAll(response.getItems());
      }
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
      databaseClient = new DatabaseClient(authenticationDetailsProvider);
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

}
