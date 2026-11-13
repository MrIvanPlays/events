package com.mrivanplays.events;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Represents a bus of event listeners. */
@SuppressWarnings({"rawtypes", "unchecked"}) // its fine
public class EventBus {

  /**
   * Create a new asynchronous event bus.
   *
   * @param async executor to call event listeners on
   * @param logger exception logger
   * @return new event bus instance
   */
  public static EventBus createAsyncEventBus(Executor async, ExceptionLogger logger) {
    return new EventBus(async, logger);
  }

  /**
   * Creates a new synchronous event bus.
   *
   * @param logger exception logger
   * @return new event bus instance
   */
  public static EventBus createSyncEventBus(ExceptionLogger logger) {
    return new EventBus(Runnable::run, logger);
  }

  private final Lock listenersLock = new ReentrantLock();
  private final AtomicLong registrationSequence = new AtomicLong();
  private final Map<Class<? extends Event>, SortedSet<RegisteredEventListener>> listeners =
      new ConcurrentHashMap<>();
  private final Executor async;
  private final ExceptionLogger logger;

  protected EventBus(Executor async, ExceptionLogger logger) {
    this.async = async;
    this.logger = logger;
  }

  /**
   * Register a new listener
   *
   * @param identifier identifier
   * @param eventClass event class
   * @param onEventFired listener handle
   * @param <T> event type
   */
  public <T extends Event> void registerListener(
      @NotNull ListenerIdentifier identifier,
      @NotNull Class<T> eventClass,
      @NotNull BiConsumer<T, Continuation> onEventFired) {
    this.registerListener(identifier, eventClass, null, onEventFired);
  }

  /**
   * Register a new listener
   *
   * @param identifier identifier
   * @param eventClass event class
   * @param onEventFired listener handle
   * @param <T> event type
   */
  public <T extends Event> void registerListener(
      @NotNull ListenerIdentifier identifier,
      @NotNull Class<T> eventClass,
      @NotNull Consumer<T> onEventFired) {
    this.registerListener(identifier, eventClass, null, onEventFired);
  }

  /**
   * Register a new listener
   *
   * @param identifier identifier
   * @param eventClass event class
   * @param priority priority to register to, if null {@link EventListener.Priority#NORMAL} will be
   *     used.
   * @param onEventFired listener handle
   * @param <T> event type
   */
  public <T extends Event> void registerListener(
      @NotNull ListenerIdentifier identifier,
      @NotNull Class<T> eventClass,
      @Nullable Short priority,
      @NotNull BiConsumer<T, Continuation> onEventFired) {
    this.registerListener(eventClass, EventListener.of(identifier, priority, onEventFired));
  }

  /**
   * Register a new listener
   *
   * @param identifier identifier
   * @param eventClass event class
   * @param priority priority to register to, if null {@link EventListener.Priority#NORMAL} will be
   *     used.
   * @param onEventFired listener handle
   * @param <T> event type
   */
  public <T extends Event> void registerListener(
      @NotNull ListenerIdentifier identifier,
      @NotNull Class<T> eventClass,
      @Nullable Short priority,
      @NotNull Consumer<T> onEventFired) {
    this.registerListener(eventClass, EventListener.of(identifier, priority, onEventFired));
  }

  /**
   * Register a new listener.
   *
   * @param eventClass event class
   * @param listener listener to register
   * @param <T> event type
   */
  public <T extends Event> void registerListener(
      @NotNull Class<T> eventClass, @NotNull EventListener<T> listener) {
    Objects.requireNonNull(eventClass, "eventClass");
    Objects.requireNonNull(listener, "listener");
    listenersLock.lock();
    try {
      SortedSet<RegisteredEventListener> eventListeners =
          this.listeners.computeIfAbsent(
              eventClass,
              _ ->
                  new ConcurrentSkipListSet<>(
                      Comparator.comparing((RegisteredEventListener l) -> l.listener().priority())
                          .reversed()
                          .thenComparingLong(RegisteredEventListener::registrationSequence)));
      eventListeners.add(
          new RegisteredEventListener<>(listener, registrationSequence.incrementAndGet()));
    } finally {
      listenersLock.unlock();
    }
  }

  /**
   * Unregisters a listener for the specified {@code eventClass}, registered by the specified {@link
   * ListenerIdentifier identifier} registrator, if there is such
   *
   * @param identifier registrator identifier
   * @param eventClass event class
   * @param <T> event
   */
  public <T extends Event> void unregisterListener(
      @NotNull ListenerIdentifier identifier, @NotNull Class<T> eventClass) {
    this.unregisterListener(identifier, eventClass, null);
  }

  /**
   * Unregisters a listener for the specified {@code eventClass}, registered by the specified {@link
   * ListenerIdentifier identifier} registrator, with the specified priority, if there is such
   *
   * @param identifier registrator identifier
   * @param eventClass event class
   * @param priority priority at which the listener is registered
   * @param <T> event
   */
  public <T extends Event> void unregisterListener(
      @NotNull ListenerIdentifier identifier,
      @NotNull Class<T> eventClass,
      @Nullable Short priority) {
    Objects.requireNonNull(identifier, "identifier");
    Objects.requireNonNull(eventClass, "eventClass");
    listenersLock.lock();
    try {
      SortedSet<RegisteredEventListener> eventListeners = this.listeners.get(eventClass);
      if (eventListeners == null) {
        return;
      }
      eventListeners.removeIf(listener -> {
        boolean identifierMatches = listener.listener().identifier().matches(identifier);
        if (identifierMatches && priority != null) {
          return listener.listener().priority() == priority;
        }
        return identifierMatches;
      });
    } finally {
      listenersLock.unlock();
    }
  }

  /**
   * Unregisters all listeners, registered by the specified {@link ListenerIdentifier identifier}
   *
   * @param identifier identifier registrator
   */
  public void unregisterListeners(@NotNull ListenerIdentifier identifier) {
    Objects.requireNonNull(identifier, "identifier");
    listenersLock.lock();
    try {
      for (Set<RegisteredEventListener> eventListeners : this.listeners.values()) {
        eventListeners.removeIf(listener -> listener.listener().identifier().matches(identifier));
      }
    } finally {
      listenersLock.unlock();
    }
  }

  /**
   * Calls all the listeners for the inputted {@code event} and does not handle any event result.
   *
   * @param event event to call
   * @param <T> event
   */
  public <T extends Event> void fireAndForget(@NotNull T event) {
    this.fire(event);
  }

  /**
   * Calls all the listeners for the inputted {@code event}
   *
   * @param event event to call
   * @return future, completed with the event, after all listeners have been called.
   * @param <T> event
   */
  @NotNull
  public <T extends Event> CompletableFuture<T> fire(@NotNull T event) {
    Objects.requireNonNull(event, "event");
    SortedSet<RegisteredEventListener> eventListeners = this.listeners.get(event.getClass());
    if (eventListeners == null) {
      return CompletableFuture.completedFuture(event);
    }
    CompletableFuture<T> ret = new CompletableFuture<>();
    var cont =
        new SequentialContinuationImpl<>(
            ret, event, logger, new ConcurrentLinkedQueue<>(eventListeners));
    async.execute(cont::resume); // start calling
    return ret;
  }

  /**
   * Calls all the listeners for the inputted {@code event} in parallel and waits for all of them to
   * finish before completing the returned future.
   *
   * @param event event to call
   * @return future, completed with the event, after all listeners have been called.
   * @param <T> event
   */
  @NotNull
  public <T extends Event> CompletableFuture<T> fireParallel(@NotNull T event) {
    Objects.requireNonNull(event, "event");
    SortedSet<RegisteredEventListener> eventListeners = this.listeners.get(event.getClass());
    if (eventListeners == null) {
      return CompletableFuture.completedFuture(event);
    }
    CompletableFuture<T> ret = new CompletableFuture<>();
    AtomicLong counter = new AtomicLong(eventListeners.size());
    eventListeners.parallelStream()
        .forEachOrdered(current -> {
          Consumer<Throwable> callback =
              e -> {
                if (e != null) {
                  logger.log(
                      new RuntimeException(
                          "Exception during continuation of listener for identifier '"
                              + current.listener().identifier()
                              + "'",
                          e));
                }
                if (counter.decrementAndGet() == 0) {
                  if (ret.isDone()) {
                    logger.log(new IllegalStateException("Cannot complete done future"));
                    return;
                  }
                  ret.complete(event);
                }
              };
          current.listener().onEventFired(event, new ParallelContinuationImpl(callback));
        });
    return ret;
  }

  /**
   * Calls all the listeners for the inputted {@code event} in parallel and does not wait for the
   * outcome.
   *
   * @param event event to call
   * @param <T> event
   */
  public <T extends Event> void fireParallelAndForget(@NotNull T event) {
    Objects.requireNonNull(event, "event");
    SortedSet<RegisteredEventListener> eventListeners = this.listeners.get(event.getClass());
    if (eventListeners == null) {
      return;
    }
    eventListeners.parallelStream()
        .forEachOrdered(current -> {
          // only exception logging callback
          Consumer<Throwable> callback =
              e -> {
                if (e != null) {
                  logger.log(
                      new RuntimeException(
                          "Exception during continuation of listener for identifier '"
                              + current.listener().identifier()
                              + "'",
                          e));
                }
              };
          current.listener().onEventFired(event, new ParallelContinuationImpl(callback));
        });
  }
}
