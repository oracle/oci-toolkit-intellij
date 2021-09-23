/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.common;

public class AutonomousDatabaseConstants {
  public static final String ALWAYS_FREE_STORAGE_TB = "0.02";
  public static final int ALWAYS_FREE_STORAGE_TB_DUMMY = 1;

  public static final int CPU_CORE_COUNT_MIN = 1;
  public static final int CPU_CORE_COUNT_MAX = 128;
  public static final int CPU_CORE_COUNT_DEFAULT = 1;
  public static final int CPU_CORE_COUNT_INCREMENT = 1;

  public static final int STORAGE_IN_TB_MIN = 1;
  public static final int STORAGE_IN_TB_MAX = 128;
  public static final int STORAGE_IN_TB_DEFAULT = 1;
  public static final double STORAGE_IN_TB_FREE_TIER_DEFAULT = 0.02;
  public static final int STORAGE_IN_TB_INCREMENT = 1;

  public static final String DATABASE_DEFAULT_USERNAME = "ADMIN";

  /* ADB Actions */
  public static final String INSTANCE_WALLET = "Instance Wallet";
  public static final String REGIONAL_WALLET = "Regional Wallet";

}
