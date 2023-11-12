package com.oracle.oci.intellij.ui.appstack.models.converter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CompositeConverter<T extends Object> extends Converter<T> {
  public final static CompositeConverter<Object> DEFAULT_CONVERTERS;
  static {
    Map<Class<?>, Converter<?>> converters = new HashMap<>();
    converters.put(Integer.class, new NumberConverter());
    converters.put(Enum.class, new EnumConverter());

    DEFAULT_CONVERTERS = (CompositeConverter<Object>) Collections.unmodifiableMap(converters);
  }

  private Map<Class<?>, Converter<Object>> converters;

  public CompositeConverter(Map<Class<?>, Converter<Object>> converters) {
    this.converters = new HashMap<>();
  }

  @Override
  public T from(String val, Class<T> clazz) throws ConversionException {
    @SuppressWarnings("unchecked")
    Converter<T> converter = (Converter<T>) this.converters.get(clazz);
    if (converter != null) {
      return converter.from(val, clazz);
    }
    throw new ConversionException("No converter for class: "+clazz.toString());
  }

  @Override
  public String to(T val, Class<T> clazz) throws ConversionException {
    @SuppressWarnings("unchecked")
    Converter<T> converter = (Converter<T>) this.converters.get(clazz);
    if (converter != null) 
    {
      return converter.to(val, clazz);
    }
    throw new ConversionException("No converter for class: "+clazz.toString());
  }
}
