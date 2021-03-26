/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.util;

import com.intellij.openapi.util.SystemInfo;

/**
 * Util class to read system properties.
 */
public class SystemProperties {

  /**
   * Returns the line separator.
   *
   * @return the line separator.
   */
  public static String lineSeparator() { return System.getProperty("line.separator"); }
  /**
   * Returns absolute path of user's home directory.
   *
   * @return absolute path of user's home directory.
   */
  public static String userHome(){
    return System.getProperty("user.home");
  }

  /**
   * Returns true if the running operating system is Microsoft Windows.
   *
   * @return true if the running operating system is Microsoft Windows.
   */
  public static boolean isWindows(){
    return SystemInfo.isWindows;
  }

  /**
   * Returns true if the running operating system is Apple Macintosh (macOS).
   *
   * @return true if the running operating system is Apple Macintosh (macOS).
   */
  public static boolean isMac(){
    return SystemInfo.isMac;
  }

  /**
   * Returns true if the running operating system is Linux-based.
   *
   * @return true if the running operating system is Linux-based.
   */
  public static boolean isLinux(){
    return SystemInfo.isLinux;
  }

}
