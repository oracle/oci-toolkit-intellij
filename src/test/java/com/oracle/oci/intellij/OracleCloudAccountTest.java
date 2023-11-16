/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;

import com.oracle.bmc.Region;
import com.oracle.bmc.database.model.AutonomousDatabaseBackupSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseWallet;
import com.oracle.bmc.database.model.DbVersionSummary;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
import com.oracle.oci.intellij.ui.account.RegionAction;
import com.oracle.oci.intellij.util.LogHandler;

public class OracleCloudAccountTest {

  public static final String COMPARTMENT_ID = "ocid1.compartment.oc1..aaaaaaaasrbmmnzhuhtcutbfnn52pswbxwao5n7x7zkpg52eklahfcgbtw6q"; 

  @SuppressWarnings("static-method")
  @Before
  public void before() {
    SystemPreferences.clearUserPreferences();
    try {
      File configFile = new File("./tests/resources/internal/config");
      assertTrue(configFile.exists());
      OracleCloudAccount.getInstance()
        .configure(configFile.getAbsolutePath()
               , SystemPreferences.getProfileName());
    } catch (Exception ioException) {
      /*
      Configuring cloud account is sufficient for testing the APIs. Since
      the UI isn't instantiated, any exception thrown from UI is discarded.
      */
    }
  }

//  @Test
//  @Order(1)
  public void test_1() {
    assertDoesNotThrow(() -> {
      final Compartment rootCompartment =
              OracleCloudAccount.getInstance().getIdentityClient().getRootCompartment();

      final List<Compartment> compartmentList =
              OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(rootCompartment.getId());

      compartmentList.forEach(compartment -> {
        LogHandler.info("\t" + compartment.getName());
        final List<Compartment> subCompartmentList =
                OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(compartment.getId());
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

//  @Test
//  @Order(6)
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


  /*
  *this test tells if there is some new regions add in the hardcoded map
   */
  @Test
  public void isAllCurrentRegionsAreSupportedWithIcons(){
    Region[] regions = Region.values();
    Map<String,String> map = RegionAction.getIcons();

    Map<String, String> testMap = new HashMap<>(map);

    for (Region region : regions) {
      String remove = testMap.remove(region.getRegionId());
      if (remove == null) {
        System.out.println("map doesn't contain "+region.getRegionId());
      }
    }
    Assert.assertEquals(regions.length, map.size());

  }
  /*
  this method prints the new added regions that we don't support in our map
   */
  @Test
  public void whichRegionsAreAdded(){
    Region[] regions = Region.values();
    Map<String,String> map = RegionAction.getIcons();
    for (Region region:regions
         ) {
      if (map.get(region.getRegionId())==null ){
        System.out.println(region.getRegionId());
      }
    }

  }
  /*
   * if  isAllCurrentRegionsAreSupportedWithIcons test failed this is also will fail because
   * the expect icons array and actual icon array don't have the same size  */
// JDBC-2119 disabled for now. @Test
  public void isImageIconsLoadedAreRight() throws IOException {
    Region[] regions = Region.values();
    /* the order matters of this icons
    and each time new region has been added we need to modify this expected list based on the order of the api above
     */
    String[] expectedIconPath = {
            "south-korea-flag.png","australia-flag.png","india-flag.png","india-flag.png","japan-flag.png","south-korea-flag.png","australia-flag.png","japan-flag.png","canada-flag.png","canada-flag.png","netherlands.png","germany-orb.png","switzerland-flag.png","saudi_arabia.png","united_arab_emirates.png","brazil-flag.png","uk-orb.png","us-orb.png","us-orb.png","us-orb.png","wales.png","chile.png","brazil-flag.png","israel.png","france.png","singapore.png","united_arab_emirates.png","italy.png","sweden.png","south_africa.png","france.png","mexico.png","spain.png","us-orb.png","canada-flag.png","brazil-flag.png","us-orb.png","us-orb.png","us-orb.png","uk-orb.png","wales.png","japan-flag.png","japan-flag.png","oman.png","australia-flag.png","italy.png","italy.png","ireland.png","germany-orb.png","germany-orb.png","ireland.png", "mexico.png"
    };
    String preffix = "/icons/regions/";
    int index =0;
    for (Region region: regions) {
      ImageIcon actualIcon = RegionAction.getCurrentRegionIcon(region.getRegionId());
      ImageIcon expectedIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource(preffix+expectedIconPath[index])));


      byte [] expectedImgBytes = ImageUtils.imageToBytes(expectedIcon.getImage());
      byte[] actualImgBytes = ImageUtils.imageToBytes(actualIcon.getImage());

      
      Assert.assertArrayEquals("the loaded icon doesn't match the expected icon: "+region+ "index: "+index,
        expectedImgBytes,actualImgBytes);
      index++;
    }

  }
  @Test
  public void WhenThereIsNewRegionShouldReturnDefaultImage() throws IOException {
    String expectedIconPath = "/icons/regions/default-flag.png";


    ImageIcon actualIcon = RegionAction.getCurrentRegionIcon("anyString");
    ImageIcon expectedIcon = new ImageIcon(getClass().getResource(expectedIconPath));

    byte[] expectedImgBytes = ImageUtils.imageToBytes(expectedIcon.getImage()) ;
    byte[] actualImgBytes =ImageUtils.imageToBytes(actualIcon.getImage()) ;

    Assert.assertArrayEquals(expectedImgBytes,actualImgBytes);
  }

   static class ImageUtils {

    public static byte[] imageToBytes(Image image) throws IOException {
      BufferedImage bufferedImage = toBufferedImage(image);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(bufferedImage, "png", baos);
      return baos.toByteArray();
    }

    private static BufferedImage toBufferedImage(Image image) {
      if (image instanceof BufferedImage) {
        return (BufferedImage) image;
      }

      BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
      bufferedImage.getGraphics().drawImage(image, 0, 0, null);
      return bufferedImage;
    }
  }

}