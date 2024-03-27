package com.oracle.oci.intellij.ui.appstack.models.converter;

// TODO: use Apache BeanConverters later when we can add the deps.
public abstract class Converter<T> {
  
  public static class ConversionException extends Exception {

    private static final long serialVersionUID = -6355157594218660982L;

    public ConversionException(String message, Throwable cause) {
      super(message, cause);
    }

    public ConversionException(String message) {
      super(message);
    }

    public ConversionException(Throwable cause) {
      super(cause);
    }
    
  }
  public abstract String to(T val, Class<T> clazz) throws ConversionException;
  public abstract T from(String val, Class<T> clazz) throws ConversionException;
}
