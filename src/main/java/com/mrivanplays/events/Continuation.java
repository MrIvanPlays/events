package com.mrivanplays.events;

/**
 * Represents a continuation of a event fire. {@link #resume()} shall always be called in order to
 * call the next listener in queue. If not called, event fire would hang.
 */
public sealed interface Continuation permits ContinuationImpl {

  /** Resumes the continuation */
  void resume();

  /** Resumes the continuation after the executed task failed */
  void resumeWithException(Throwable e);
}
