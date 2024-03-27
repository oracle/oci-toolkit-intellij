package com.oracle.oci.intellij.util;

import java.util.Optional;
import java.util.function.Consumer;

import com.intellij.notification.NotificationType;
import com.oracle.oci.intellij.ui.common.UIUtil;

public class SafeRunnerUtil {

  public static class SafeRunner<T> {
    private Consumer<T> run;
    private Optional<Consumer<Throwable>> onError;

    public SafeRunner(Consumer<T> run, Optional<Consumer<Throwable>> onError) {
      this.run = run;
      this.onError = onError;
    }

    public void run(T arg) throws Exception {
      try {
        this.run.accept(arg);
      } catch (Throwable e) {
        if (e instanceof Exception || e instanceof ExceptionInInitializerError) {
          this.onError.ifPresentOrElse(
              onErr -> onErr.accept(e), 
              () -> {System.err.println(new Exception("no handler"));});
        }
        else {
          throw new Exception(e);
        }
      }
    }
  }

  public static class DefaultErrorRunnable implements Consumer<Throwable> {
    @Override
    public void accept(Throwable ex) {
      final String message = "Oracle Cloud account configuration failed: " + ex.getMessage();
      LogHandler.error(message, ex);
      UIUtil.fireNotification(NotificationType.ERROR, message, null);
      ex.printStackTrace();
    }
  }

  public static <T> void run(Consumer<T> r, T arg) {
   SafeRunner<T> runner = new SafeRunner(r, Optional.of(new DefaultErrorRunnable()));
   try
   {
     runner.run(arg);
   }
   catch (Exception e) {e.printStackTrace();}
  }
}
