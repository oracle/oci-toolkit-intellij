/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import com.oracle.oci.intellij.util.SystemProperties;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static com.oracle.bmc.util.internal.FileUtils.expandUserHome;

/**
 * Implementation to read OCI configuration file. Note, config files MUST contain
 * a "DEFAULT" profile,else validation fails. Additional profiles are optional.
 */
public final class ConfigFileHandler {

  public static final String DEFAULT_CONFIG_FOLDER = ".oci";
  public static final String DEFAULT_CONFIG_FILE_NAME = "config";

  /**
   * Returns absolute path of Oracle cloud service config file.
   *
   * @return absolute path of Oracle cloud service config file.
   */
  static String getConfigFilePath() {
    return SystemProperties.userHome() + File.separator
            + DEFAULT_CONFIG_FOLDER + File.separator
            + DEFAULT_CONFIG_FILE_NAME;
  }

  /**
   * Parses the given config file.
   *
   * @param configFile path of config file.
   * @return the {@link ProfilesSet}
   * @throws IOException is thrown if I/O fails.
   */
  public static ProfilesSet parse(String configFile) throws IOException {
    final File file = new File(expandUserHome(configFile));
    if (file.length() > 50000) {
      throw new IllegalStateException("File too large : " + configFile);
    }

    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
      return ProfilesSet.getInstance().populate(bufferedReader);
    }
  }

  /**
   * Set the file permissions to 600
   * @param path file path.
   * @throws IOException thrown if file read fails.
   */
  private static void setPermissions(Path path) throws IOException {
    if (SystemProperties.isWindows()) {
      File file = path.toFile();
      if (!file.setReadable(true, true) ||
              !file.setWritable(true, true) ||
              !file.setExecutable(false)) {
        throw new RuntimeException("Failed to set permissions on file : " + file.getName());
      }
    } else if (SystemProperties.isLinux() || SystemProperties.isMac()) {
      Set<PosixFilePermission> perms = new HashSet<>();
      perms.add(PosixFilePermission.OWNER_READ);
      perms.add(PosixFilePermission.OWNER_WRITE);
      Files.setPosixFilePermissions(path, perms);
    }
  }

  /**
   * Save the given profile (name and its properties) in the config file.
   *
   * @param configurationFilePath the configuration file.
   * @param profile the profile.
   * @throws IOException thrown if file I/O fails.
   */
  public static void save(String configurationFilePath, Profile profile) throws IOException {
    final StringBuffer profileBuffer = read(profile);

    String expandedPath = expandUserHome(configurationFilePath);
    final File file = new File(expandedPath);
    File directory = file.getParentFile();

    if (!directory.exists() && !directory.mkdirs()) {
      throw new IOException("Cannot create directory : " + directory.getName());
    }

    StandardOpenOption openOption = StandardOpenOption.APPEND;
    if (!file.exists()) {
      openOption = StandardOpenOption.CREATE;
    }

    final Path configPath = Paths.get(expandedPath);
    Files.write(configPath, profileBuffer.toString().getBytes(), openOption);

    if (openOption == StandardOpenOption.CREATE) {
      setPermissions(configPath);
    }
  }

  /**
   * Reads the given profile in to {@link StringBuffer}.
   *
   * @param profile the profile to read.
   * @return the profile as {@link StringBuffer}.
   */
  private static StringBuffer read(Profile profile) {
    final String lineSep = SystemProperties.lineSeparator();

    final StringBuffer profileBuffer = new StringBuffer();
    profileBuffer
            .append(lineSep)
            .append("[")
            /* Convert the user given name to upper case
             before saving the new profile in config file.*/
            .append(profile.getName().toUpperCase())
            .append("]");

    final Enumeration<Object> keys = profile.getProperties().keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      String value = (String) profile.getProperties().get(key);

      profileBuffer
              .append(lineSep).append(key).append(" = ").append(value);
    }

    profileBuffer.append(lineSep);
    return profileBuffer;
  }

}
