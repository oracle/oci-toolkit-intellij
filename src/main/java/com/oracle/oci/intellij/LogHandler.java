/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij;

import com.intellij.openapi.diagnostic.Logger;

public class LogHandler {

  private static com.intellij.openapi.diagnostic.Logger logger = com.intellij.openapi.diagnostic.Logger
      .getInstance("OCI Plugin");

  public static void info(String message) {
    logger.info(message);
  }

  public static void error(String message) {
    logger.error(message);
  }

  public static void warn(String message) {
    logger.warn(message);
  }

  public static void error(String message, Throwable th) {
    logger.error(message, th);
  }
}
