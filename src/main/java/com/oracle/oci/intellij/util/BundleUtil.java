package com.oracle.oci.intellij.util;

public class BundleUtil {

  /**
   * Swaps out the current thread context class loader with cl before
   * executing the runnable.  Guarantees the original thread classloader is replaced
   * before returning.
   * 
   * This can be necessary per https://plugins.jetbrains.com/docs/intellij/plugin-class-loaders.html
   * if you need to guarantee that a piece of code is run correctly within an IntelliJ plugin.
   * Normally, what you want to do is pass in the classloader associated with the calling class or some other
   * class in the plugin.
   *
   * @param cl
   * @param r
   */
  public static void withContextCL(ClassLoader cl, Runnable r) {
    Thread currentThread = Thread.currentThread();
    ClassLoader originalClassLoader = currentThread.getContextClassLoader();

    try {
      currentThread.setContextClassLoader(cl);
      r.run();
    }
    finally {
      currentThread.setContextClassLoader(originalClassLoader);
    }
  }
}
