package com.oracle.oci.intellij;

public class ErrorHandler {

  public static void logInfo(String message) {
    System.out.println(message);
  }

  public static void logError(String message) {
    System.out.println(message);
  }

  public static void logErrorStack(String message, Throwable th) {
    System.out.println(message);
    th.printStackTrace();
  }
}
