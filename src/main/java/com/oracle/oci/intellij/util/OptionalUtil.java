package com.oracle.oci.intellij.util;

import java.util.Optional;
import java.util.function.Function;

public final class OptionalUtil {

  public static class ExtendedOptional<T> {
    private Optional<T> optional;

    public static <T> ExtendedOptional<T> of(Optional<T> optional) {
      return new ExtendedOptional<>(optional);
    }

    public static <T> ExtendedOptional<T> of(T val) {
      return new ExtendedOptional<>(Optional.of(val));
    }

    public static <T> ExtendedOptional<T> ofNullable(T val) {
      return new ExtendedOptional<>(Optional.ofNullable(val));
    }

    public ExtendedOptional(Optional<T> optional) {
      this.optional = optional;
    }

    public <R> R ifPresentElseDefault(Function<T, R> mapper, R defaultVal) {
      if (this.optional.isPresent()) {
        return this.optional.map(mapper).get();
      }
      return (R) defaultVal;
    }

    public Optional<T> optional() {
      return optional;
    }
  }
}
