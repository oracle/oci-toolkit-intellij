package com.oracle.oci.intellij.ui.database;

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

import com.oracle.bmc.database.model.*;
import com.oracle.bmc.database.requests.*;
import com.oracle.bmc.database.responses.*;
import com.oracle.oci.intellij.ErrorHandler;
import com.oracle.oci.intellij.account.AuthProvider;
import com.oracle.oci.intellij.account.GlobalEventHandler;
import org.apache.commons.io.FileUtils;

import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.DbWorkload;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.LifecycleState;

public class ADBInstanceClient implements PropertyChangeListener {

  private static ADBInstanceClient single_instance = null;
  private static DatabaseClient databaseClient;
  private static Map<String, ADBInstanceWrapper> instancesMap = new LinkedHashMap<String, ADBInstanceWrapper>();

  private ADBInstanceClient() {
    if (databaseClient == null) {
      databaseClient = createADBInstanceClient();
      GlobalEventHandler.getInstance().addPropertyChangeListener(this);
    }
  }

  public static ADBInstanceClient getInstance() {
    if (single_instance == null) {
      single_instance = new ADBInstanceClient();
    }
    return single_instance;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("RegionID".equals(evt.getPropertyName())) {
      databaseClient.setRegion(evt.getNewValue().toString());
    }
  }

  private DatabaseClient createADBInstanceClient() {
    databaseClient = new DatabaseClient(
        AuthProvider.getInstance().getProvider());
    databaseClient.setRegion(AuthProvider.getInstance().getRegion());
    return databaseClient;
  }


  public void close() {
    try {
      if (databaseClient != null) {
        databaseClient.close();
      }
    }
    catch (Exception e) {
      ErrorHandler.logErrorStack(e.getMessage(), e);
    }
  }

  public List<AutonomousDatabaseSummary> getInstances(DbWorkload workloadType)
      throws Exception {
    ListAutonomousDatabasesRequest listInstancesRequest = ListAutonomousDatabasesRequest
        .builder().compartmentId(AuthProvider.getInstance().getCompartmentId())
        .dbWorkload(workloadType)
        .sortBy(ListAutonomousDatabasesRequest.SortBy.Timecreated)
        .sortOrder(ListAutonomousDatabasesRequest.SortOrder.Desc).build();
    List<AutonomousDatabaseSummary> instances = new ArrayList<>();

    if (databaseClient == null)
      return instances;

    ListAutonomousDatabasesResponse response = null;
    try {
      response = databaseClient.listAutonomousDatabases(listInstancesRequest);
    }
    catch (Throwable e) {
      // To handle forbidden error
      ErrorHandler
          .logError("Unable to list Autonomous Databases: " + e.getMessage());
    }

    if (response == null) {
      return instances;
    }

    instances = response.getItems();
    final Iterator<AutonomousDatabaseSummary> it = instances.iterator();
    while (it.hasNext()) {
      AutonomousDatabaseSummary instance = it.next();
      if (LifecycleState.Terminated.equals(instance.getLifecycleState())) {
        it.remove();
      }
      else {
        instancesMap.put(instance.getId(), new ADBInstanceWrapper(instance));
      }
    }

    return instances;
  }

  public void startInstance(final AutonomousDatabaseSummary instance) {
    databaseClient.startAutonomousDatabase(
        StartAutonomousDatabaseRequest.builder()
            .autonomousDatabaseId(instance.getId()).build())
        .getAutonomousDatabase();

  }

  public void stopInstance(final AutonomousDatabaseSummary instance) {
    databaseClient.stopAutonomousDatabase(
        StopAutonomousDatabaseRequest.builder()
            .autonomousDatabaseId(instance.getId()).build())
        .getAutonomousDatabase();

  }

  public ADBInstanceWrapper getInstanceDetails(final String instanceId) {
    return instancesMap.get(instanceId);
  }

  public void scaleUpDownInstance(final AutonomousDatabaseSummary instance,
      int cpuCoreCount, int dataStorageSizeInTBs,
      final Boolean isAutoScalingEnabled) {
    UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails
        .builder().cpuCoreCount(cpuCoreCount)
        .dataStorageSizeInTBs(dataStorageSizeInTBs)
        .isAutoScalingEnabled(isAutoScalingEnabled).build();

    databaseClient.updateAutonomousDatabase(
        UpdateAutonomousDatabaseRequest.builder()
            .updateAutonomousDatabaseDetails(updateRequest)
            .autonomousDatabaseId(instance.getId()).build());
  }

  public void changeAdminPassword(final AutonomousDatabaseSummary instance,
      String password) {
    UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails
        .builder().adminPassword(password).build();

    databaseClient.updateAutonomousDatabase(
        UpdateAutonomousDatabaseRequest.builder()
            .updateAutonomousDatabaseDetails(updateRequest)
            .autonomousDatabaseId(instance.getId()).build());
  }

  public void updateLicenseType(final AutonomousDatabaseSummary instance,
      final UpdateAutonomousDatabaseDetails.LicenseModel licenseModel) {
    UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails
        .builder().licenseModel(licenseModel).build();

    databaseClient.updateAutonomousDatabase(
        UpdateAutonomousDatabaseRequest.builder()
            .updateAutonomousDatabaseDetails(updateRequest)
            .autonomousDatabaseId(instance.getId()).build());
  }

  public void createClone(CreateAutonomousDatabaseCloneDetails cloneRequest) {
    CreateAutonomousDatabaseResponse response = databaseClient
        .createAutonomousDatabase(CreateAutonomousDatabaseRequest.builder()
            .createAutonomousDatabaseDetails(cloneRequest).build());

  }

  public void createInstance(final CreateAutonomousDatabaseDetails request) {
    CreateAutonomousDatabaseResponse response = databaseClient
        .createAutonomousDatabase(CreateAutonomousDatabaseRequest.builder()
            .createAutonomousDatabaseDetails(request).build());
  }

  public void terminate(final String databaseId) {
    databaseClient.deleteAutonomousDatabase(
        DeleteAutonomousDatabaseRequest.builder()
            .autonomousDatabaseId(databaseId).build());
  }

  public void downloadWallet(final AutonomousDatabaseSummary instance,
      final String walletType, final String password,
      final String walletDirectory) {
    final GenerateAutonomousDatabaseWalletDetails walletDetails;
    if ((instance.getIsDedicated() != null && instance.getIsDedicated())) {
      walletDetails = GenerateAutonomousDatabaseWalletDetails.builder()
          .password(password).build();
    }
    else {
      final GenerateAutonomousDatabaseWalletDetails.GenerateType type =
          ADBConstants.REGIONAL_WALLET.equalsIgnoreCase(walletType) ?
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

    final ZipInputStream zin = new ZipInputStream(
        adbWalletResponse.getInputStream());

    final File file = new File(walletDirectory);
    if (!file.exists()) {
      boolean isDirectoryCreated = file.mkdir();
      if (!isDirectoryCreated) {
        ErrorHandler
            .logError("Unable to create wallet directory : " + walletDirectory);
        return;
      }
    }
    else {
      ErrorHandler
          .logInfo("Wallet directory already exists : " + walletDirectory);
      try {
        FileUtils.cleanDirectory(file);
      }
      catch (IOException e) {
        ErrorHandler.logInfo(
            "Could not clean existing wallet directory : " + walletDirectory);
      }
    }

    final Path outDir = Paths.get(walletDirectory);
    final byte[] buffer = new byte[2048];
    ZipEntry entry;
    try {
      while ((entry = zin.getNextEntry()) != null) {

        Path filePath = outDir.resolve(entry.getName());

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
            BufferedOutputStream bos = new BufferedOutputStream(fos,
                buffer.length)) {

          int len;
          while ((len = zin.read(buffer)) > 0) {
            bos.write(buffer, 0, len);
          }
        }
      }
      ErrorHandler.logInfo(
          "Downloaded Client Credentials (Wallet) for database: " + instance
              .getDbName());
    }
    catch (Exception e) {
      ErrorHandler.logErrorStack(
          "Error occured while downloading wallet for ADB: " + instance
              .getDbName(), e);
    }
  }

  public Map<String, String> getContainerDatabaseMap(
      final String compartmentId) {
    final Map<String, String> containerDBMap = new TreeMap<String, String>();

    if (databaseClient == null)
      return containerDBMap;

    ListAutonomousContainerDatabasesRequest request = ListAutonomousContainerDatabasesRequest
        .builder().compartmentId(compartmentId).lifecycleState(
            AutonomousContainerDatabaseSummary.LifecycleState.Available)
        .build();
    ListAutonomousContainerDatabasesResponse response = null;
    try {
      response = databaseClient.listAutonomousContainerDatabases(request);
    }
    catch (Throwable e) {
      // To handle forbidden error
      ErrorHandler
          .logError("Unable to list Container Databases: " + e.getMessage());
    }

    if (response == null)
      return containerDBMap;

    List<AutonomousContainerDatabaseSummary> cdb = response.getItems();
    for (AutonomousContainerDatabaseSummary containerDB : cdb) {
      containerDBMap.put(containerDB.getDisplayName(), containerDB.getId());
    }
    return containerDBMap;
  }

  public List<AutonomousDatabaseBackupSummary> getBackupList(
      final AutonomousDatabaseSummary instance) {
    ListAutonomousDatabaseBackupsResponse response = null;
    try {
      final ListAutonomousDatabaseBackupsRequest request = ListAutonomousDatabaseBackupsRequest
          .builder().autonomousDatabaseId(instance.getId())
          .lifecycleState(AutonomousDatabaseBackupSummary.LifecycleState.Active)
          .sortBy(ListAutonomousDatabaseBackupsRequest.SortBy.Timecreated)
          .sortOrder(ListAutonomousDatabaseBackupsRequest.SortOrder.Desc)
          .build();
      response = databaseClient.listAutonomousDatabaseBackups(request);
    }
    catch (Throwable e) {
      ErrorHandler.logError(
          "Unable to get backup list for Database: " + e.getMessage());
    }
    if (response != null)
      return response.getItems();
    return Collections.emptyList();
  }

  public void restore(final String autonomousDatabaseId,
      final Date restoreTimestamp) {
    RestoreAutonomousDatabaseDetails restoreDetail = RestoreAutonomousDatabaseDetails
        .builder().timestamp(restoreTimestamp).build();
    RestoreAutonomousDatabaseRequest request = RestoreAutonomousDatabaseRequest
        .builder().restoreAutonomousDatabaseDetails(restoreDetail)
        .autonomousDatabaseId(autonomousDatabaseId).build();
    databaseClient.restoreAutonomousDatabase(request);
  }

  public void rotateWallet(final String instanceId, final String walletType) {
    final UpdateAutonomousDatabaseWalletDetails details = UpdateAutonomousDatabaseWalletDetails
        .builder().shouldRotate(Boolean.TRUE).build();
    if (ADBConstants.REGIONAL_WALLET.equalsIgnoreCase(walletType)) {
      final UpdateAutonomousDatabaseRegionalWalletRequest request = UpdateAutonomousDatabaseRegionalWalletRequest
          .builder().updateAutonomousDatabaseWalletDetails(details).build();
      databaseClient.updateAutonomousDatabaseRegionalWallet(request);
    }
    else if (ADBConstants.INSTANCE_WALLET.equalsIgnoreCase(walletType)) {
      final UpdateAutonomousDatabaseWalletRequest request = UpdateAutonomousDatabaseWalletRequest
          .builder().updateAutonomousDatabaseWalletDetails(details)
          .autonomousDatabaseId(instanceId).build();
      databaseClient.updateAutonomousDatabaseWallet(request);
    }
    else {
      ErrorHandler.logError("Unknown wallet type selected for rotation");
    }
  }

  public Map<String, AutonomousDatabaseWallet> getWalletType(
      final AutonomousDatabaseSummary instance) {
    final Map<String, AutonomousDatabaseWallet> walletTypeMap = new HashMap<String, AutonomousDatabaseWallet>();
    if (!(instance.getIsDedicated() != null && instance.getIsDedicated())) {
      try {
        final GetAutonomousDatabaseRegionalWalletRequest regionalWalletRequest = GetAutonomousDatabaseRegionalWalletRequest
            .builder().build();
        final GetAutonomousDatabaseRegionalWalletResponse regionalWalletResponse = databaseClient
            .getAutonomousDatabaseRegionalWallet(regionalWalletRequest);
        final AutonomousDatabaseWallet regionalWallet = regionalWalletResponse
            .getAutonomousDatabaseWallet();
        walletTypeMap.put(ADBConstants.REGIONAL_WALLET, regionalWallet);
      }
      catch (Throwable e) {
        ErrorHandler.logError("Unable to get Regional Wallet details");
      }

      try {
        final GetAutonomousDatabaseWalletRequest walletRequest = GetAutonomousDatabaseWalletRequest
            .builder().autonomousDatabaseId(instance.getId()).build();
        final GetAutonomousDatabaseWalletResponse walletResponse = databaseClient
            .getAutonomousDatabaseWallet(walletRequest);
        final AutonomousDatabaseWallet instanceWallet = walletResponse
            .getAutonomousDatabaseWallet();
        walletTypeMap.put(ADBConstants.INSTANCE_WALLET, instanceWallet);
      }
      catch (Throwable e) {
        ErrorHandler.logError("Unable to get Instance Wallet details");
      }
    }
    return walletTypeMap;
  }

}