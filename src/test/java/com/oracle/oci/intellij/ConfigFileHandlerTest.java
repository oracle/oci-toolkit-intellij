/*
  Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij;

import com.oracle.oci.intellij.account.ConfigFileHandler;
import com.oracle.oci.intellij.account.SystemPreferences;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigFileHandlerTest {

  /**
   * Negative test.
   * Tests ConfigFileHandler.parse(String) method by supplying an empty config file name.
   */
  @Test
  public void parseFile_1() {
    final String emptyConfigFilePath = "";

    final Exception exception =
            assertThrows(IOException.class, ()->ConfigFileHandler.parse(emptyConfigFilePath));
    final String expected = " (No such file or directory)";
    assertEquals(expected, exception.getMessage());
  }

  /**
   * Negative test.
   * Tests ConfigFileHandler.parse(String) method by supplying a file of size more than 50k lines.
   */
  @Test
  public void parseFile_2() {
    // Create a fat file.
    final String fatFileName = "fat_config_file.txt";
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fatFileName))) {
      writer.write(getBuffer(600));
    } catch (IOException ioException) {
      assertEquals("This exception is not expected", ioException.getMessage());
    }

    final File file = new File(fatFileName);
    final Exception exception =
            assertThrows(IllegalStateException.class, ()->ConfigFileHandler.parse(file.getPath()));
    final String expected = "File too large : " + file.getPath();
    assertEquals(expected, exception.getMessage());

    assertEquals(true, (new File(fatFileName).delete()));
  }

  private String getBuffer(int lengthInMultiplesOfHundred) {
    final String hundredCharsBuffer =
            "0123456789" +
            "0123456789" +
            "0123456789" +
            "0123456789" +
            "0123456789" +
            "0123456789" +
            "0123456789" +
            "0123456789" +
            "0123456789" +
            "0123456789";

    final StringBuilder stringBuilder = new StringBuilder();
    for (int i = 1; i <= lengthInMultiplesOfHundred; i++) {
      stringBuilder.append(hundredCharsBuffer);
    }
    return stringBuilder.toString();
  }

  /**
   * Negative test.
   * Tests ConfigFileHandler.parse(String) method by supplying a file with invalid config.
   */
  @Test
  public void parseFile_3() {
    final String invalidConfigFileName = "invalid_config_file.txt";

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(invalidConfigFileName))) {
      writer.write(getBuffer(1));
    } catch (IOException ioException) {
      assertEquals("This exception is not expected", ioException.getMessage());
    }

    final File file = new File(invalidConfigFileName);
    final Exception exception =
            assertThrows(IllegalStateException.class, ()->ConfigFileHandler.parse(file.getPath()));
    final String expected = "Invalid file format. Profile name is not specified correctly in the config file.";
    assertEquals(expected, exception.getMessage());
    assertEquals(true, (new File(invalidConfigFileName).delete()));
  }

  /**
   * Positive test.
   * Reads the last used config file.
   */
  @Test
  public void parseFile_4() {
    assertDoesNotThrow(()->{ConfigFileHandler.parse(SystemPreferences.getConfigFilePath());});
  }

  /**
   * Positive test.
   * Tests saving new profile in the given config file.
   * To keep the test simple, we read all profiles from last used config file
   * and add them to a new config file. This new config is parsed again and
   * discarded at last.
   */
  @Test
  public void saveProfile_1() {
    final String newConfigFileName = "new_config_file";

    assertDoesNotThrow(() -> {
      final ConfigFileHandler.ProfileSet profileSet =
              ConfigFileHandler.parse(SystemPreferences.getConfigFilePath());

      final File newFile = new File(newConfigFileName);
      if (newFile.createNewFile()) {
        final Iterator<String> iterator = profileSet.getProfileNames().iterator();
        iterator.forEachRemaining((profileName) -> {
          final ConfigFileHandler.Profile profile = profileSet.get(profileName);
          assertDoesNotThrow(() -> {ConfigFileHandler.save(newFile.getAbsolutePath(), profile);});
        });
      }
      assertDoesNotThrow(()->{ConfigFileHandler.parse((new File(newConfigFileName).getPath()));});
      assertEquals(true, (new File(newConfigFileName).delete()));
    });
  }

}
