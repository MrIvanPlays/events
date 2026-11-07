package com.mrivanplays.events;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a event which has a result.
 *
 * @param <R> result of event
 */
public interface ResultedEvent<R> extends Event {

  /**
   * Get the event result
   *
   * @return result
   */
  @NotNull
  R result();

  /**
   * Modify the event result
   *
   * @param newResult new result
   */
  void setResult(@NotNull R newResult);

  /** Represents a generic <code>true/false</code> result */
  final class GenericResult {

    public static final GenericResult ALLOWED = new GenericResult(true);
    public static final GenericResult DENIED = new GenericResult(false);

    private final boolean allowed;

    private GenericResult(boolean allowed) {
      this.allowed = allowed;
    }

    public boolean allowed() {
      return allowed;
    }
  }
}
