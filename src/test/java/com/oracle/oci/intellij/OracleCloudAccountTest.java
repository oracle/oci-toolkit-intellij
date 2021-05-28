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

}
