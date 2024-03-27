package com.oracle.oci.intellij.ui.appstack.models.converter;

public class NumberConverter extends Converter<Integer> {


  @Override
  public String to(Integer val, Class<Integer> clazz) {
    return String.valueOf(val);
  }

  @Override
  public Integer from(String val, Class<Integer> clazz) throws ConversionException {
    try {
      return Integer.valueOf(val);
    } catch (NumberFormatException e) {
      throw new ConversionException(e);
    }
  }

}
