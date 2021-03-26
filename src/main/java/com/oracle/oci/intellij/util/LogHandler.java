/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.util;

import com.intellij.openapi.diagnostic.Logger;

/**
 * Util class to write log messages.
 */
public class LogHandler {

  private static final Logger logger = Logger.getInstance("oci-plugin");

  /**
   * Logs info note.
   *
   * @param info message.
   */
  public static void info(String info){
    logger.info(info);
  }

  /**
   * Logs an error message.
   *
   * @param error message.
   */
  public static void error(String error){
    logger.error(error);
  }

  /**
   * Logs a warning message.
   *
   * @param warning message.
   */
  public static void warn(String warning){
    logger.warn(warning);
  }

  /**
   * Logs an error message.
   *
   * @param error     message
   * @param throwable exception thrown.
   */
  public static void error(String error, Throwable throwable){
    logger.error(error, throwable);
  }
}
