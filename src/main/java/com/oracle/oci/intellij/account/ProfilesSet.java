/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import java.io.*;
import java.util.*;

/**
 * A Set of all profiles read from config file.
 */
public class ProfilesSet {
  private final Map<String, Properties> mapOfProfiles = new HashMap<>();
  private static final ProfilesSet instance = new ProfilesSet();

  /**
   * Returns the singleton instance.
   *
   * @return the singleton instance.
   */
  public static ProfilesSet instance() {
    return instance;
  }

  /**
   * Populates {@link ProfilesSet} from the given Reader.
   *
   * @param bufferedReader the config file
   * @return {@link ProfilesSet}.
   * @throws IOException throws if file read fails.
   */
  public ProfilesSet populate(BufferedReader bufferedReader) throws IOException {
    // Clear the previous profiles, if any, before populating with new configuration.
    mapOfProfiles.clear();

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
                "Config parse error, attempted to read configuration without specifying a profile: "
                        + line);
      }
    }
    return instance();
  }

  public Profile get(String profileName) {
    return new Profile(profileName, mapOfProfiles.get(profileName));
  }

  public Set<String> getProfileNames() {
    return mapOfProfiles.keySet();
  }

}
