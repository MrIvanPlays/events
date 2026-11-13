package com.mrivanplays.events.reflection;

import com.mrivanplays.events.Continuation;
import com.mrivanplays.events.Event;
import com.mrivanplays.events.EventListener;
import com.mrivanplays.events.ListenerIdentifier;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
@SuppressWarnings("rawtypes") // its fine
public class SubscribedEventListener implements EventListener {

  private final SubscriptionEventListener listener;
  private final boolean withContinuation;
  private final short priority;
  private final Method method;

  @ApiStatus.Internal
  public SubscribedEventListener(
      @NotNull SubscriptionEventListener listener,
      short priority,
      @NotNull Method m,
      boolean withContinuation) {
    this.listener = Objects.requireNonNull(listener, "listener");
    this.priority = priority;
    this.method = Objects.requireNonNull(m, "method");
    this.withContinuation = withContinuation;
  }

  @Override
  public short priority() {
    return priority;
  }

  @Override
  public @NotNull ListenerIdentifier identifier() {
    return listener;
  }

  @Override
  public void onEventFired(@NotNull Event event, @NotNull Continuation continuation) {
    try {
      if (withContinuation) {
        method.invoke(listener, event, continuation);
      } else {
        method.invoke(listener, event);
        continuation.resume();
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Method became inaccessible", e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException("Could not invoke method", e);
    }
  }
}
