/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.account;

import java.util.Properties;

public class Profile {
  private final String profileName;
  private final Properties properties;

  public Profile(String profileName, Properties properties) {
    this.profileName = profileName;
    this.properties = properties;
  }

  public String getName() {
    return profileName;
  }

  public Properties getProperties() {
    return properties;
  }

}
