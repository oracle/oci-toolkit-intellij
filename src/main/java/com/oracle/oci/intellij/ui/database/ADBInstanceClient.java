/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

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

import com.oracle.bmc.Region;
import com.oracle.bmc.database.model.*;
import com.oracle.bmc.database.requests.*;
import com.oracle.bmc.database.responses.*;
import com.oracle.oci.intellij.LogHandler;
import com.oracle.oci.intellij.account.AuthProvider;
import com.oracle.oci.intellij.account.GlobalEventHandler;
import com.oracle.oci.intellij.account.PreferencesWrapper;
import org.apache.commons.io.FileUtils;

import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.DbWorkload;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.LifecycleState;

public class ADBInstanceClient implements PropertyChangeListener {

  private static final ADBInstanceClient single_instance = new ADBInstanceClient();
  private DatabaseClient databaseClient;
  private Map<String, ADBInstanceWrapper> instancesMap = new LinkedHashMap<String, ADBInstanceWrapper>();

  public final static ADBInstanceClient getInstance() {
    if (single_instance.databaseClient == null) {
      single_instance.createADBInstanceClient();
    }
    return single_instance;
  }

  public void reset() {
    try{
      databaseClient.close();
    }
    catch (Exception e) {}
    databaseClient = null;
    instancesMap.clear();
  }

  public void setRegion(final String regionId) {
    databaseClient.setRegion(Region.fromRegionId(regionId));
  }

  private void createADBInstanceClient() {
    try{
      LogHandler.info("Creating ADBInstanceClient..");
      databaseClient = new DatabaseClient(AuthProvider.getInstance().getProvider());
      databaseClient.setRegion(AuthProvider.getInstance().getRegion());
    }
    catch(Exception e) {
      LogHandler.error("Unable to create ADBInstanceClient", e);
      throw e;
    }
  }

  public void close() {
    try {
      if (databaseClient != null) {
        LogHandler.info("Closing ADBInstanceClient..");
        databaseClient.close();
      }
    }
    catch (Exception e) {
      LogHandler.error("Unable to close ADBInstanceClient", e);
    }
  }

  public List<AutonomousDatabaseSummary> getInstances(DbWorkload workloadType)
      throws Exception {
    LogHandler.info("Fetching ADB Instance details from the server..");
    ListAutonomousDatabasesRequest listInstancesRequest = ListAutonomousDatabasesRequest
        .builder().compartmentId(AuthProvider.getInstance().getCompartmentId())
        .dbWorkload(workloadType)
        .sortBy(ListAutonomousDatabasesRequest.SortBy.Timecreated)
        .sortOrder(ListAutonomousDatabasesRequest.SortOrder.Desc).build();
    List<AutonomousDatabaseSummary> instances = new ArrayList<>();

    if (databaseClient == null) {
      LogHandler.warn("ADBInstance client not initialized, returning empty list.");
      return instances;
    }

    ListAutonomousDatabasesResponse response = null;
    try {
      response = databaseClient.listAutonomousDatabases(listInstancesRequest);
    }
    catch (Exception e) {
      LogHandler.error("Unable to get Autonomous Databases Instances : " + e.getMessage());
      throw e;
    }

    if (response == null) {
      LogHandler.error("Unable to get Autonomous Databases Instances. Got null response from the server");
      return instances;
    }

    instances = response.getItems();
    if(instances != null)
      LogHandler.info("Got " + instances.size() + " ADB Instance details.");
    else
      LogHandler.warn("ADB Instance details is null");

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
    LogHandler.info("Starting ADB Instance : " + instance.getId());
    try{
      databaseClient.startAutonomousDatabase(
          StartAutonomousDatabaseRequest.builder()
              .autonomousDatabaseId(instance.getId()).build())
          .getAutonomousDatabase();
      LogHandler.info("ADB Instance : " + instance.getId() + " started.");
    }
    catch (Exception ex) {
      LogHandler.error("Failed to start ADB Instance : " + instance.getId(), ex);
      throw ex;
    }
  }

  public void stopInstance(final AutonomousDatabaseSummary instance) {
    LogHandler.info("Stopping ADB Instance : " + instance.getId());
    try{
      databaseClient.stopAutonomousDatabase(
          StopAutonomousDatabaseRequest.builder()
              .autonomousDatabaseId(instance.getId()).build())
          .getAutonomousDatabase();
      LogHandler.info("ADB Instance : " + instance.getId() + " stopped.");
    }
    catch (Exception ex) {
      LogHandler.error("Failed to stop ADB Instance : " + instance.getId(), ex);
      throw ex;
    }
  }

  public ADBInstanceWrapper getInstanceDetails(final String instanceId) {
    ADBInstanceWrapper wrapper = instancesMap.get(instanceId);
    LogHandler.info("Got ADBInstanceWrapper " + wrapper + " for the ADBInstance : " + instanceId);
    return wrapper;
  }

  public void scaleUpDownInstance(final AutonomousDatabaseSummary instance,
      int cpuCoreCount, int dataStorageSizeInTBs,
      final Boolean isAutoScalingEnabled) {
    UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails
        .builder().cpuCoreCount(cpuCoreCount)
        .dataStorageSizeInTBs(dataStorageSizeInTBs)
        .isAutoScalingEnabled(isAutoScalingEnabled).build();

    LogHandler.info("ScaleUpDown ADB Instance : " + instance.getId());
    try{
      databaseClient.updateAutonomousDatabase(
          UpdateAutonomousDatabaseRequest.builder()
              .updateAutonomousDatabaseDetails(updateRequest)
              .autonomousDatabaseId(instance.getId()).build());
      LogHandler.info("ScaleUpDown ADB Instance : " + instance.getId() + " success.");
    }
    catch (Exception ex) {
      LogHandler.error("Failed to ScaleUpDown ADB Instance : " + instance.getId(), ex);
      throw ex;
    }

  }

  public void changeAdminPassword(final AutonomousDatabaseSummary instance,
      String password) {
    UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails
        .builder().adminPassword(password).build();

    LogHandler.info("ChangeAdminPassword ADB Instance : " + instance.getId());
    try{
      databaseClient.updateAutonomousDatabase(
          UpdateAutonomousDatabaseRequest.builder()
              .updateAutonomousDatabaseDetails(updateRequest)
              .autonomousDatabaseId(instance.getId()).build());
      LogHandler.info("ChangeAdminPassword ADB Instance : " + instance.getId() + " success.");
    }
    catch (Exception ex) {
      LogHandler.error("Failed to ChangeAdminPassword for the ADB Instance : " + instance.getId(), ex);
      throw ex;
    }


  }

  public void updateLicenseType(final AutonomousDatabaseSummary instance,
      final UpdateAutonomousDatabaseDetails.LicenseModel licenseModel) {
    UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails
        .builder().licenseModel(licenseModel).build();

    LogHandler.info("UpdateLicenseType ADB Instance : " + instance.getId());
    try{
      databaseClient.updateAutonomousDatabase(
          UpdateAutonomousDatabaseRequest.builder()
              .updateAutonomousDatabaseDetails(updateRequest)
              .autonomousDatabaseId(instance.getId()).build());
      LogHandler.info("UpdateLicenseType ADB Instance : " + instance.getId() + " success.");
    }
    catch (Exception ex) {
      LogHandler.error("Failed to UpdateLicenseType for the ADB Instance : " + instance.getId(), ex);
      throw ex;
    }
  }

  public void changeWorkloadTypeToOLTP(final String instanceId) {
    UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails.builder()
        .dbWorkload(UpdateAutonomousDatabaseDetails.DbWorkload.Oltp).build();
    LogHandler.info("Change workload type for ADB Instance : " + instanceId);
    try {
      databaseClient.updateAutonomousDatabase(UpdateAutonomousDatabaseRequest.builder()
          .updateAutonomousDatabaseDetails(updateRequest).autonomousDatabaseId(instanceId).build());
    }
    catch(Exception ex) {
      LogHandler.error("Failed to change the workload type for the ADB Instance : " + instanceId, ex);
      throw ex;
    }

  }

  public void createClone(CreateAutonomousDatabaseCloneDetails cloneRequest) {
    LogHandler.info("Creating clone source ID : " + cloneRequest.getSourceId());
    try {
      CreateAutonomousDatabaseResponse response = databaseClient
          .createAutonomousDatabase(CreateAutonomousDatabaseRequest.builder()
              .createAutonomousDatabaseDetails(cloneRequest).build());
      LogHandler.info("Creating clone source ID : " + cloneRequest.getSourceId() + " success.");
    }
    catch(Exception e) {
      LogHandler.error("Failed to CreateClone for the ADB Instance : " + cloneRequest.getSourceId(), e);
      throw e;
    }


  }

  public void createInstance(final CreateAutonomousDatabaseDetails request) {
    LogHandler.info("Creating ADBInstance..");
    try {
      CreateAutonomousDatabaseResponse response = databaseClient
          .createAutonomousDatabase(CreateAutonomousDatabaseRequest.builder()
              .createAutonomousDatabaseDetails(request).build());
      LogHandler.info("ADBInstance created successfully.");
    }
    catch(Exception ex) {
      LogHandler.error("Failed to Create ADB Instance", ex);
      throw ex;
    }

  }

  public void terminate(final String databaseId) {
    LogHandler.info("Terminating ADBInstance : " + databaseId);
    try {
      databaseClient.deleteAutonomousDatabase(
          DeleteAutonomousDatabaseRequest.builder()
              .autonomousDatabaseId(databaseId).build());
      LogHandler.info("Terminating ADBInstance : " + databaseId + " is successful.");
    }
    catch(Exception ex) {
      LogHandler.error("Failed to terminate ADB Instance : " + databaseId, ex);
      throw ex;
    }

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
        LogHandler.error("Unable to create wallet directory : " + walletDirectory);
        return;
      }
    }
    else {
      LogHandler.info("Wallet directory already exists : " + walletDirectory);
      try {
        FileUtils.cleanDirectory(file);
      }
      catch (IOException e) {
        LogHandler.info("Could not clean existing wallet directory : " + walletDirectory);
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
      LogHandler.info("Downloaded Client Credentials (Wallet) for database: " + instance.getId());
    }
    catch (Exception e) {
      LogHandler.error("Error occured while downloading wallet for ADB: " + instance.getId(), e);
      throw new RuntimeException(e);
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
    catch (Exception e) {
      LogHandler.error("Unable to list Container Databases: " + e.getMessage());
      throw e;
    }

    if (response == null)
      return containerDBMap;

    List<AutonomousContainerDatabaseSummary> cdb = response.getItems();
    for (AutonomousContainerDatabaseSummary containerDB : cdb) {
      containerDBMap.put(containerDB.getDisplayName(), containerDB.getId());
    }
    return containerDBMap;
  }

  public List<AutonomousDatabaseBackupSummary> getBackupList(final AutonomousDatabaseSummary instance) {
    ListAutonomousDatabaseBackupsResponse response = null;
    try {
      LogHandler.info("Getting backup list for ADBInstance : " + instance.getId());
      final ListAutonomousDatabaseBackupsRequest request = ListAutonomousDatabaseBackupsRequest
          .builder().autonomousDatabaseId(instance.getId())
          .lifecycleState(AutonomousDatabaseBackupSummary.LifecycleState.Active)
          .sortBy(ListAutonomousDatabaseBackupsRequest.SortBy.Timecreated)
          .sortOrder(ListAutonomousDatabaseBackupsRequest.SortOrder.Desc)
          .build();
      response = databaseClient.listAutonomousDatabaseBackups(request);
    }
    catch (Exception e) {
      LogHandler.error("Unable to get backup list for Database", e);
      throw e;
    }
    if (response != null) {
      final List<AutonomousDatabaseBackupSummary> adbsList = response.getItems();
      LogHandler.info("Got backup list. Size : " + adbsList.size());
      return adbsList;
    }
    else {
      LogHandler.info("No backup list found for ADBInstance : " + instance.getId());
      return Collections.emptyList();
    }

  }

  public void restore(final String autonomousDatabaseId, final Date restoreTimestamp) {
    LogHandler.info("Restoring ADBInstance " + autonomousDatabaseId + " to the timestamp : " + restoreTimestamp.toString());
    try {
      RestoreAutonomousDatabaseDetails restoreDetail = RestoreAutonomousDatabaseDetails
          .builder().timestamp(restoreTimestamp).build();
      RestoreAutonomousDatabaseRequest request = RestoreAutonomousDatabaseRequest
          .builder().restoreAutonomousDatabaseDetails(restoreDetail)
          .autonomousDatabaseId(autonomousDatabaseId).build();
      databaseClient.restoreAutonomousDatabase(request);
      LogHandler.info("Restoring ADBInstance " + autonomousDatabaseId + " to the timestamp : " + restoreTimestamp.toString() + " is successful.");
    }
    catch (Exception ex) {
      LogHandler.error("Restore failed for the ADBInstance " + autonomousDatabaseId, ex);
      throw ex;
    }

  }

  public void rotateWallet(final String instanceId, final String walletType) {
    try {
      LogHandler.info("Rotating wallet for the ADBInstance " + instanceId + ", wallet type " + walletType);
      final UpdateAutonomousDatabaseWalletDetails details = UpdateAutonomousDatabaseWalletDetails
          .builder().shouldRotate(Boolean.TRUE).build();
      if (ADBConstants.REGIONAL_WALLET.equalsIgnoreCase(walletType)) {
        final UpdateAutonomousDatabaseRegionalWalletRequest request = UpdateAutonomousDatabaseRegionalWalletRequest
            .builder().updateAutonomousDatabaseWalletDetails(details).build();
        databaseClient.updateAutonomousDatabaseRegionalWallet(request);
        LogHandler.info("Wallet rotation successful for the ADBInstance " + instanceId + ", wallet type " + walletType);
      }
      else if (ADBConstants.INSTANCE_WALLET.equalsIgnoreCase(walletType)) {
        final UpdateAutonomousDatabaseWalletRequest request = UpdateAutonomousDatabaseWalletRequest
            .builder().updateAutonomousDatabaseWalletDetails(details)
            .autonomousDatabaseId(instanceId).build();
        databaseClient.updateAutonomousDatabaseWallet(request);
        LogHandler.info("Wallet rotation successful for the ADBInstance " + instanceId + ", wallet type " + walletType);
      }
      else {
        LogHandler.error("Unknown wallet type selected for rotation");
      }
    }
    catch(Exception ex) {
      LogHandler.error("Wallet rotation failed for the ADBInstance " + instanceId + ", wallet type " + walletType, ex);
      throw ex;
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
      catch (Exception ex) {
        LogHandler.error("Unable to get Regional Wallet details");
        throw ex;
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
      catch (Exception ex) {
        LogHandler.error("Unable to get Instance Wallet details");
        throw ex;
      }
    }
    return walletTypeMap;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    LogHandler.info("ADBInstanceClient: Handling the Event Update : " + evt.toString());
    switch (evt.getPropertyName()) {
    case PreferencesWrapper.EVENT_COMPARTMENT_UPDATE:
      break;
    case PreferencesWrapper.EVENT_REGION_UPDATE:
      databaseClient.setRegion(Region.fromRegionId(evt.getNewValue().toString()));
      break;
    case PreferencesWrapper.EVENT_SETTINGS_UPDATE:
      // reset the state.
      reset();
      break;
    }
  }

}