package com.mrivanplays.events;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a listener of an event.
 *
 * @param <T> event that this listener is listening to
 */
public interface EventListener<T extends Event> {

  @NotNull
  static <T extends Event> EventListener<T> of(
      @NotNull ListenerIdentifier identifier, @NotNull Consumer<T> onEventFired) {
    return of(identifier, null, onEventFired);
  }

  @NotNull
  static <T extends Event> EventListener<T> of(
      @NotNull ListenerIdentifier identifier,
      @Nullable Short priority,
      @NotNull Consumer<T> onEventFired) {
    Objects.requireNonNull(identifier, "identifier");
    Objects.requireNonNull(onEventFired, "onEventFired");
    return new EventListener<>() {

      @Override
      public short priority() {
        return priority == null ? Priority.NORMAL : priority;
      }

      @Override
      public @NotNull ListenerIdentifier identifier() {
        return identifier;
      }

      @Override
      public void onEventFired(@NotNull T event) {
        onEventFired.accept(event);
      }
    };
  }

  @NotNull
  static <T extends Event> EventListener<T> of(
      @NotNull ListenerIdentifier identifier, @NotNull BiConsumer<T, Continuation> onEventFired) {
    return of(identifier, null, onEventFired);
  }

  @NotNull
  static <T extends Event> EventListener<T> of(
      @NotNull ListenerIdentifier identifier,
      @Nullable Short priority,
      @NotNull BiConsumer<T, Continuation> onEventFired) {
    Objects.requireNonNull(identifier, "identifier");
    Objects.requireNonNull(onEventFired, "onEventFired");
    return new EventListener<>() {
      @Override
      public short priority() {
        return priority == null ? Priority.NORMAL : priority;
      }

      @Override
      public @NotNull ListenerIdentifier identifier() {
        return identifier;
      }

      @Override
      public void onEventFired(@NotNull T event, @NotNull Continuation continuation) {
        onEventFired.accept(event, continuation);
      }
    };
  }

  /** Utility class of default priorities. */
  final class Priority {
    public static final short FIRST = 64;
    public static final short NORMAL = 32;
    public static final short LAST = 0;

    public static short[] defaultValues() {
      return new short[] {FIRST, NORMAL, LAST};
    }
  }

  /**
   * Get the priority in which this event listener is going to be ordered to be called.
   *
   * @return priority
   */
  short priority();

  /**
   * Get the identifier to which this listener is bound to.
   *
   * @return identifier
   */
  @NotNull
  ListenerIdentifier identifier();

  /**
   * Called when the event this listener is listening to is fired.
   *
   * @param event the event instance
   * @param continuation if asynchronous operations are done, resume the event fire cycle when the
   *     operation is done
   */
  default void onEventFired(@NotNull T event, @NotNull Continuation continuation) {
    try {
      this.onEventFired(event);
      continuation.resume();
    } catch (Exception e) {
      continuation.resumeWithException(e);
    }
  }

  /**
   * Called when the event this listener is listening to is fired.
   *
   * @param event the event instance
   * @throws UnsupportedOperationException if neither {@link #onEventFired(Event, Continuation)} or
   *     this is overridden.
   */
  default void onEventFired(@NotNull T event) {
    throw new UnsupportedOperationException(
        "Event listener shall override either onEventFired(Event, Continuation) or onEventFired(Event)");
  }
}
