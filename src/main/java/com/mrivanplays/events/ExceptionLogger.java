package com.mrivanplays.events;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Shall represent a simple logger wrapper for the needs of logging exceptions in event calls, when
 * {@link Continuation#resumeWithException(Throwable)} is called.
 */
public interface ExceptionLogger {

  /**
   * Create from a java {@link Logger}
   *
   * @param java parent logger
   * @return new instance of exception logger
   */
  @NotNull
  static ExceptionLogger from(@NotNull Logger java) {
    Objects.requireNonNull(java, "logger");
    return e -> {
      Objects.requireNonNull(e, "exception");
      java.log(Level.SEVERE, "Exception occurred during event handling", e);
    };
  }

  /**
   * Creates a logger that logs to System.out
   *
   * <p><b>WARNING:</b> System.out shall not be a proper logging mechanism in your application,
   * please use a proper logger implementation.
   *
   * @return new instance of exception logging
   */
  @NotNull
  static ExceptionLogger printStackTrace() {
    return e -> {
      Objects.requireNonNull(e, "exception");
      e.printStackTrace(System.out);
    };
  }

  void log(@NotNull Throwable e);
}
