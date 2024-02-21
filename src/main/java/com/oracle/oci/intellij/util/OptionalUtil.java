package com.oracle.oci.intellij.util;

import java.util.Optional;
import java.util.function.Function;

public final class OptionalUtil {

  public static class ExtendedOptional<T> {
    private Optional<T> optional;
    
    public static <T> ExtendedOptional<T> of(Optional<T> optional) {
      return new ExtendedOptional<>(optional);
    }
    
    public ExtendedOptional(Optional<T> optional) {
      this.optional = optional;
    }

    
    public <R> R ifPresentElseNull(Function<T,R> mapper) {
      return this.optional.map(mapper).get();
    }

    public Optional<T> optional() {
      return optional;
    }
  }
}
