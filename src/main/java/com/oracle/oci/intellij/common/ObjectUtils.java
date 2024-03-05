package com.oracle.oci.intellij.common;

public class ObjectUtils {

  public static boolean isEmpty(Object...objects) {
    for (Object obj : objects) {
      if (isEmpty(obj)) {
        return true;
      }
    }
    
    return false;
  }
  
  public static boolean isEmpty(Object object) {
    if (object instanceof String) {
      return isEmpty((String)object);
    }
    return object == null;
  }
  
  public static boolean isEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }
}
