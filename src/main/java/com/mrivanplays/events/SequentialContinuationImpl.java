package com.mrivanplays.events;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class SequentialContinuationImpl<T extends Event> implements Continuation {

  private final CompletableFuture<T> future;
  private final T firedEvent;
  private final Queue<RegisteredEventListener> listeners;
  private RegisteredEventListener current;
  private final ExceptionLogger logger;

  SequentialContinuationImpl(
      CompletableFuture<T> future,
      T firedEvent,
      ExceptionLogger logger,
      Queue<RegisteredEventListener> listeners) {
    this.future = future;
    this.firedEvent = firedEvent;
    this.logger = logger;
    this.listeners = listeners;
  }

  @Override
  public void resume() {
    if (this.listeners.isEmpty()) {
      // no next, complete future
      this.future.complete(this.firedEvent);
      return;
    }
    var listener = this.listeners.poll();
    this.current = listener;
    listener.listener().onEventFired(this.firedEvent, this);
  }

  @Override
  public void resumeWithException(@NotNull Throwable e) {
    logger.log(
        new RuntimeException(
            "Exception during continuation of listener for identifier '"
                + current.listener().identifier()
                + "'",
            e));
    this.resume();
  }
}
