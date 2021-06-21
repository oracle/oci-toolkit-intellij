/*
  Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import com.intellij.openapi.util.SystemInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

import static com.oracle.bmc.util.internal.FileUtils.expandUserHome;

/**
 * Implementation to read OCI configuration file. The config files MUST contain
 * a "DEFAULT" profile, else validation fails. Additional profiles are optional.
 */
public final class ConfigFileHandler {
  /**
   * Parses the given config file.
   *
   * @param configFile path of config file.
   * @return the ProfilesSet.
   * @throws IOException is thrown if I/O fails.
   */
  public static ProfileSet parse(String configFile) throws IOException {
    final File file = new File(expandUserHome(configFile));
    if (file.length() > 50000) {
      throw new IllegalStateException("File too large : " + configFile);
    }

    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
      return populate(bufferedReader);
    }
  }

  /**
   * Populates ProfilesSet from the given Reader.
   *
   * @param bufferedReader the config file
   * @return ProfilesSet.
   * @throws IOException throws if file read fails.
   */
  private static ProfileSet populate(BufferedReader bufferedReader) throws IOException {
    final Map<String, Properties> mapOfProfiles = new LinkedHashMap<>();

    String line;
    String currentReadingProfile = null;

    while ((line = bufferedReader.readLine()) != null) {
      final String trimmedLine = line.trim();

      if (trimmedLine.isEmpty() || trimmedLine.charAt(0) == '#') {
        continue;
      }

      // If the line is profile name.
      if (trimmedLine.charAt(0) == '['
              && trimmedLine.charAt(trimmedLine.length() - 1) == ']') {
        // This is a profile name.
        final String profileName = trimmedLine.substring(1, trimmedLine.length() - 1);

        if (profileName.isEmpty()) {
          throw new IllegalStateException(
                  "Profile name cannot be empty: " + line);
        }

        if (mapOfProfiles.containsKey(profileName)) {
          throw new IllegalStateException("Profile exists already : " + profileName);
        }
        mapOfProfiles.put(profileName, new Properties());
        currentReadingProfile = profileName;
      } else if (currentReadingProfile != null) {
        // Read a property of current reading profile and put it in properties map.
        final int splitIndex = trimmedLine.indexOf('=');

        if (splitIndex == -1) {
          throw new IllegalStateException(
                  "Found line with no key-value pair: " + line);
        }

        // Trim the key.
        final String key = trimmedLine.substring(0, splitIndex).trim();
        // Trim the value.
        final String value = trimmedLine.substring(splitIndex + 1).trim();
        // Key cannot be empty.
        if (key.isEmpty()) {
          throw new IllegalStateException("Found line with no key: " + line);
        }

        // Add the key-value pair to properties of the current reading profile.
        final Properties currentReadingProfileProperties = mapOfProfiles.get(currentReadingProfile);
        currentReadingProfileProperties.put(key, value);
      } else {
        throw new IllegalStateException(
                "Invalid file format. Profile name is not specified correctly in the config file.");
      }
    }
    return new ProfileSet(mapOfProfiles);
  }

  /**
   * Set the file permissions to 600
   * @param path file path.
   * @throws IOException thrown if file read fails.
   */
  private static void setPermissions(Path path) throws IOException {
    if (SystemInfo.isWindows) {
      File file = path.toFile();
      if (!file.setReadable(true, true) ||
              !file.setWritable(true, true) ||
              !file.setExecutable(false)) {
        throw new RuntimeException("Failed to set permissions on file : " + file.getName());
      }
    } else if (SystemInfo.isLinux || SystemInfo.isMac) {
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
    final String lineSep = System.getProperty("line.separator");

    final StringBuffer profileBuffer = new StringBuffer();
    profileBuffer
            .append(lineSep)
            .append("[")
            /* Convert the user given name to upper case
             before saving the new profile in config file.*/
            .append(profile.getName().toUpperCase())
            .append("]");

    final Enumeration<Object> keys = profile.getEntries().keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      String value = (String) profile.getEntries().get(key);

      profileBuffer
              .append(lineSep).append(key).append("=").append(value);
    }

    profileBuffer.append(lineSep);
    return profileBuffer;
  }

  /**
   * A Set of all profiles read from config file.
   */
  public static class ProfileSet {
    private final Map<String, Properties> mapOfProfiles;

    private ProfileSet(Map<String, Properties> mapOfProfiles) {
      this.mapOfProfiles = mapOfProfiles;
    }

    public Profile get(String profileName) {
      if (mapOfProfiles.containsKey(profileName)) {
        return new Profile(profileName, mapOfProfiles.get(profileName));
      }
      return null;
    }

    public boolean containsKey(String profileName) {
      return mapOfProfiles.containsKey(profileName);
    }

    public Set<String> getProfileNames() {
      return mapOfProfiles.keySet();
    }
  }

  /**
   * The profile name and properties key-value pair.
   */
  public static class Profile {
    private final String name;
    private final Properties entries;

    public Profile(String name) {
      this.name = name;
      entries = new Properties();
    }

    public Profile(String name, Properties entries) {
      this.name = name;
      this.entries = entries;
    }

    /**
     * Adds new parameter name-value pair to the profile.
     * @param key the parameter key
     * @param value the parameter value
     * @return The updated profile
     */
    public Profile add(String key, String value) {
      entries.put(key, value);
      return this;
    }

    /**
     * Return the name of the profile.
     * @return name
     */
    public String getName() {
      return name;
    }

    /**
     * Returns the profile parameters.
     * @return profile parameters.
     */
    public Properties getEntries() {
      return entries;
    }

    /**
     * Returns the parameter value for the given key.
     * @param key the parameter key
     * @return parameter value.
     */
    public String get(String key) {
      return (String) entries.get(key);
    }
  }

}
