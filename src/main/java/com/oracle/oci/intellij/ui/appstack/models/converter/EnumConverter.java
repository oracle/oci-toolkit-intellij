package com.oracle.oci.intellij.ui.appstack.models.converter;

public class EnumConverter<T extends Enum<T>> extends Converter<T> {

  @Override
  public String to(T val, Class<T> clazz) throws ConversionException {
    return val.name();
  }

  @Override
  public T from(String val, Class<T> clazz) throws ConversionException {
    return Enum.valueOf(clazz, val);
  }


 

}
