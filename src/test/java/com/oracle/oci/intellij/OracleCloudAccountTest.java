package com.oracle.oci.intellij;

import com.oracle.bmc.database.model.AutonomousDatabaseBackupSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseWallet;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;

import java.util.List;
import java.util.Map;

public class OracleCloudAccountTest {

  @BeforeAll
  @Test
  public void before() {
    OracleCloudAccount.getInstance()
            .configure(SystemPreferences.getConfigFilePath(), SystemPreferences.getProfileName());
  }

  @Test
  @Order(1)
  public void test_1() {
    try {
      final Compartment rootCompartment =
              OracleCloudAccount.getInstance().getIdentityClient().getRootCompartment();

      final List<Compartment> compartmentList =
              OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(rootCompartment);

      compartmentList.forEach(compartment -> {
        System.out.println("\t" + compartment.getName());
        final List<Compartment> subCompartmentList =
                OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(compartment);
        subCompartmentList.forEach((subCompartment)->{
          System.out.println("\t\t" + subCompartment.getName());
        });
      });
      assert(true);
    } catch (Exception ex) {
      assert(false);
    }
  }

  @Test
  @Order(2)
  public void test_2() {
    try {
      final List<RegionSubscription> regionsList =
              OracleCloudAccount.getInstance().getIdentityClient().getRegionsList();
      System.out.println("Fetched regions are: ");
      regionsList.forEach(region-> {
        System.out.println("\t" + region.getRegionName());
      });
      assert(true);
    } catch (Exception ex) {
      assert(false);
    }
  }

  @Test
  @Order(3)
  public void test_3() {
    final List<AutonomousDatabaseSummary> autonomousDatabaseInstances = OracleCloudAccount.getInstance().getDatabaseClient()
            .getAutonomousDatabaseInstances(AutonomousDatabaseSummary.DbWorkload.UnknownEnumValue);

    System.out.println("List of databases: ");
    autonomousDatabaseInstances.forEach(autonomousDatabaseSummary -> {
      final StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("\t").append(autonomousDatabaseSummary.getId())
              .append("\t").append(autonomousDatabaseSummary.getDbName())
              .append("\t").append(autonomousDatabaseSummary.getCompartmentId());
      System.out.println(stringBuilder);
    });
  }

  @Test
  @Order(4)
  public void test_4() {
    final OracleCloudAccount.DatabaseClientProxy databaseClientProxy =
            OracleCloudAccount.getInstance().getDatabaseClient();

    final List<AutonomousDatabaseSummary> autonomousDatabaseInstances = databaseClientProxy
            .getAutonomousDatabaseInstances(AutonomousDatabaseSummary.DbWorkload.UnknownEnumValue);

    System.out.println("Wallet type: ");
    autonomousDatabaseInstances.forEach(autonomousDatabaseSummary -> {
      final Map<String, AutonomousDatabaseWallet> walletType =
              databaseClientProxy.getWalletType(autonomousDatabaseSummary);

      System.out.println("Wallet details for " + autonomousDatabaseSummary.getDbName() + ": ");
      walletType.forEach((key, value) -> {
        System.out.println(key + "\t" + value);
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
      System.out.println("Backup list of " + autonomousDatabaseSummary.getDbName() + ": ");
      final List<AutonomousDatabaseBackupSummary> backupList =
              databaseClientProxy.getBackupList(autonomousDatabaseSummary);

      backupList.forEach(autonomousDatabaseBackupSummary -> {
        System.out.println("\t" + autonomousDatabaseBackupSummary.getDisplayName());
      });
    });
  }

  //@Test
  @Order(6)
  public void test_6() {
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
      try {
        final Compartment subCompartment =
                identityClientProxy.createCompartment(compartment.getId(), "childCompartment_"+depth, "A new compartment");
        System.out.println("Created a new compartment: " + "childCompartment_"+depth);
        Thread.sleep(3000);
        createSubCompartmentsTree(identityClientProxy, subCompartment, depth-1);
      } catch (Exception ex) {
        System.out.println(String.format("Failed to create new compartment under %s. The error is %s ", compartment.getName(), ex.getMessage()));
      }
    }
  }

  //@Test
  @Order(7)
  public void test_7() {
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
          System.out.println(String.format("Deleted compartment %s successfully.", compartment.getName()));
        } else {
          break;
        }
      } catch (Exception ex) {
        if (ex.getMessage().contains("Tenant has been throttled. Too Many Requests")) {
          System.out.println("Tenant has been throttled. Waiting for 3 seconds before retry...");
          // Wait 3 seconds and retry.
          try {
            Thread.sleep(3000);
          } catch (InterruptedException e) {}
        } else {
          System.out.println(String.format("Failed to delete compartment under %s. The error is %s ",
                  compartment.getName(), ex.getMessage()));
          break;
        }
      }
    }
  }
}
