package com.oracle.oci.intellij.util;

import java.util.Optional;
import java.util.function.Consumer;
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

  /**
   * An optional that can take alternates if the main optional object
   * is empty. Used in cases like where you want to have a compartment object
   * but you can only get a compartment id.
   *
   * @param <t>
   */
  public static class AlternateOptional<MAIN, ALTERNATE> {
    private MAIN main;
    private ALTERNATE alternate;
    private Runnable ifEmpty;

    private AlternateOptional(MAIN main, ALTERNATE alternate,
                              Runnable ifEmpty) {
      this.main = main;
      this.alternate = alternate;
      this.ifEmpty = ifEmpty;
    }

    public void ifPresent(Consumer<MAIN> mainAction,
                          Consumer<ALTERNATE> alternateAction) {
      if (main != null) {
        mainAction.accept(this.main);
      } else if (alternateAction != null) {
        alternateAction.accept(alternate);
      } else if (ifEmpty != null) {
        ifEmpty.run();
      }
    }

    public <R> Optional<R> map(Function<MAIN, R> mapMain, Function<ALTERNATE,R> mapAlternate) {
      if (this.main != null) {
        return Optional.ofNullable(mapMain.apply(main));
      }
      else if (this.alternate != null) {
        return Optional.ofNullable(mapAlternate.apply(alternate));
      }
      return Optional.empty();
    }

    public Optional<MAIN> getMain() {
      return Optional.ofNullable(this.main);
    }
    public boolean isPresent() {
      return main != null || alternate != null;
    }

    public boolean empty() {
      return !isPresent();
    }

    public static <M, A> AlternateOptional<M, A> ofNullable(M main, A alternate,
                                                            Runnable ifEmpty) {
      if (ifEmpty == null) {
        throw new NullPointerException();
      }
      return new AlternateOptional<>(main, alternate, ifEmpty);
    }

    public static <M, A> AlternateOptional<M, A> ofNullable(M main,
                                                            A alternate) {
      return new AlternateOptional<>(main, alternate, () -> {
      });
    }
  }
}
