/*
  Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij;

import com.oracle.bmc.database.model.AutonomousDatabaseBackupSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseWallet;
import com.oracle.bmc.database.model.DbVersionSummary;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.util.LogHandler;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OracleCloudAccountTest {

  @BeforeAll
  @Test
  public void before() {
    try {
      OracleCloudAccount.getInstance()
              .configure(SystemPreferences.getConfigFilePath(), SystemPreferences.getProfileName());
    } catch (Exception ioException) {
      /*
      Configuring cloud account is sufficient for testing the APIs. Since
      the UI isn't instantiated, any exception thrown from UI is discarded.
      */
    }
  }

  @Test
  @Order(1)
  public void test_1() {
    assertDoesNotThrow(() -> {
      final Compartment rootCompartment =
              OracleCloudAccount.getInstance().getIdentityClient().getRootCompartment();

      final List<Compartment> compartmentList =
              OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(rootCompartment);

      compartmentList.forEach(compartment -> {
        LogHandler.info("\t" + compartment.getName());
        final List<Compartment> subCompartmentList =
                OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(compartment);
        subCompartmentList.forEach((subCompartment)->{
          LogHandler.info("\t\t" + subCompartment.getName());
        });
      });
    });
  }
  @Test
  @Order(2)
  public void test_2() {
    assertDoesNotThrow(() -> {
      final List<RegionSubscription> regionsList =
              OracleCloudAccount.getInstance().getIdentityClient().getRegionsList();
      LogHandler.info("Fetched regions are: ");
      regionsList.forEach(region-> {
        LogHandler.info("\t" + region.getRegionName());
      });
    });
  }

  @Test
  @Order(3)
  public void test_3() {
    final List<AutonomousDatabaseSummary> autonomousDatabaseInstances =
            OracleCloudAccount.getInstance().getDatabaseClient()
                    .getAutonomousDatabaseInstances(
                            AutonomousDatabaseSummary.DbWorkload.UnknownEnumValue);

    LogHandler.info("List of databases: ");
    autonomousDatabaseInstances.forEach(autonomousDatabaseSummary -> {
      final StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("\t").append(autonomousDatabaseSummary.getId())
              .append("\t").append(autonomousDatabaseSummary.getDbName())
              .append("\t").append(autonomousDatabaseSummary.getCompartmentId());
      LogHandler.info(stringBuilder.toString());
    });
  }

  @Test
  @Order(4)
  public void test_4() {
    final OracleCloudAccount.DatabaseClientProxy databaseClientProxy =
            OracleCloudAccount.getInstance().getDatabaseClient();

    final List<AutonomousDatabaseSummary> autonomousDatabaseInstances = databaseClientProxy
            .getAutonomousDatabaseInstances(AutonomousDatabaseSummary.DbWorkload.UnknownEnumValue);

    LogHandler.info("Wallet type: ");
    autonomousDatabaseInstances.forEach(autonomousDatabaseSummary -> {
      final Map<String, AutonomousDatabaseWallet> walletType =
              databaseClientProxy.getWalletType(autonomousDatabaseSummary);

      LogHandler.info("Wallet details for " + autonomousDatabaseSummary.getDbName() + ": ");
      walletType.forEach((key, value) -> {
        LogHandler.info(key + "\t" + value);
      });
    });
  }

  @Test
  @Order(5)
  public void test_5() {
    final OracleCloudAccount.DatabaseClientProxy databaseClientProxy =
            OracleCloudAccount.getInstance().getDatabaseClient();

    final List<AutonomousDatabaseSummary> autonomousDatabaseInstances = databaseClientProxy
            .getAutonomousDatabaseInstances(AutonomousDatabaseSummary.DbWorkload.UnknownEnumValue);

    autonomousDatabaseInstances.forEach(autonomousDatabaseSummary -> {
      LogHandler.info("Backup list of " + autonomousDatabaseSummary.getDbName() + ": ");
      final List<AutonomousDatabaseBackupSummary> backupList =
              databaseClientProxy.getBackupList(autonomousDatabaseSummary);

      backupList.forEach(autonomousDatabaseBackupSummary -> {
        LogHandler.info("\t" + autonomousDatabaseBackupSummary.getDisplayName());
      });
    });
  }

  @Test
  @Order(6)
  public void test_6() {
    final OracleCloudAccount.DatabaseClientProxy databaseClientProxy =
            OracleCloudAccount.getInstance().getDatabaseClient();

    final List<DbVersionSummary> databaseVersions = databaseClientProxy.getDatabaseVersions(
            OracleCloudAccount.getInstance().getIdentityClient().getRootCompartment().getId());

    LogHandler.info("The supported database versions are: ");
    databaseVersions.forEach((dbVersionSummary)-> {
      LogHandler.info("\t" + dbVersionSummary.getVersion());
    });
  }

  //@Test
  @Order(7)
  @Disabled
  public void test_7() {
    final OracleCloudAccount.IdentityClientProxy identityClient =
            OracleCloudAccount.getInstance().getIdentityClient();

    final List<Compartment> childCompartments =
            identityClient.getCompartmentList(identityClient.getRootCompartment());

    for(Compartment childCompartment: childCompartments) {
      createSubCompartmentsTree(identityClient, childCompartment, 100);
    }
  }

  private void createSubCompartmentsTree(OracleCloudAccount.IdentityClientProxy identityClientProxy,
                                         Compartment compartment, int depth) {
    if (depth > 0) {
      assertDoesNotThrow(() -> {
        final Compartment subCompartment =
                identityClientProxy.createCompartment(compartment.getId(), "childCompartment_"+depth, "A new compartment");
        LogHandler.info("Created a new compartment: " + "childCompartment_"+depth);
        Thread.sleep(3000);
        createSubCompartmentsTree(identityClientProxy, subCompartment, depth-1);
      });
    }
  }

  //@Test
  @Order(8)
  @Disabled
  public void test_8() {
    final OracleCloudAccount.IdentityClientProxy identityClientProxy =
            OracleCloudAccount.getInstance().getIdentityClient();

    deleteSubCompartments(identityClientProxy, identityClientProxy.getRootCompartment(), "childCompartment_");
  }

  private void deleteSubCompartments(OracleCloudAccount.IdentityClientProxy identityClientProxy,
                                     Compartment compartment, String matchingPattern) {
    final List<Compartment> childCompartments =
            identityClientProxy.getCompartmentList(compartment);

    for(Compartment childCompartment: childCompartments) {
      deleteSubCompartments(identityClientProxy, childCompartment, matchingPattern);
    }

    while (true) {
      try {
        if (compartment.getName().contains(matchingPattern)) {
          identityClientProxy.deleteCompartment(compartment.getId());
          LogHandler.info(String.format("Deleted compartment %s successfully.", compartment.getName()));
        } else {
          break;
        }
      } catch (IllegalStateException ex) {
        if (ex.getMessage().contains("Tenant has been throttled. Too Many Requests")) {
          LogHandler.info("Tenant has been throttled. Waiting for 3 seconds before retry...");
          // Wait 3 seconds and retry.
          try {
            Thread.sleep(3000);
          } catch (InterruptedException e) {}
        } else {
          LogHandler.info(String.format("Failed to delete compartment under %s. The error is %s ",
                  compartment.getName(), ex.getMessage()));
          break;
        }
      }
    }
  }
}
