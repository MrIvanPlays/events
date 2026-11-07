package com.mrivanplays.events.reflection;

import com.mrivanplays.events.Continuation;
import com.mrivanplays.events.Event;
import com.mrivanplays.events.EventBus;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a utility class, which is to be used to bake and register event listeners, annotated
 * with {@link Subscribe} annotation.
 */
public class SubscribedEventListenerBakery {

  /**
   * Bakes all methods of the specified subscribed event listener into individual event listeners.
   *
   * @param listener subscription event listener
   * @return baked map
   */
  @NotNull
  public static Map<Class<? extends Event>, List<SubscribedEventListener>> bakeListeners(
      @NotNull SubscriptionEventListener listener) {
    Objects.requireNonNull(listener, "listener");
    Map<Class<? extends Event>, List<SubscribedEventListener>> ret = new HashMap<>();
    for (Method m : listener.getClass().getDeclaredMethods()) {
      if (!m.isAnnotationPresent(Subscribe.class)) {
        continue;
      }
      if (!m.canAccess(listener)) {
        m.trySetAccessible();
      }
      Class<? extends Event> eventClass;
      boolean withContinuation;
      if (m.getParameterCount() == 1) {
        withContinuation = false;
        Class<?> param = m.getParameterTypes()[0];
        if (!param.getSuperclass().isAssignableFrom(Event.class)) {
          throw new IllegalArgumentException(
              "found @Subscribe on method of which parameter is not an event");
        }
        eventClass = (Class<? extends Event>) param;
      } else if (m.getParameterCount() == 2) {
        Class<?> eventParam = m.getParameterTypes()[0];
        Class<?> possibleContinuation = m.getParameterTypes()[1];
        if (!eventParam.getSuperclass().isAssignableFrom(Event.class)) {
          throw new IllegalArgumentException(
              "found @Subscribe on method with 2 parameters, the 1st of which is not an event");
        }
        if (!possibleContinuation.isAssignableFrom(Continuation.class)) {
          throw new IllegalArgumentException(
              "found @Subscribe on method with 2 parameters, the 2nd of which is not Continuation");
        }
        eventClass = (Class<? extends Event>) eventParam;
        withContinuation = true;
      } else {
        throw new IllegalArgumentException(
            "found @Subscribe on method with more than 2 parameters. Acceptable: (Event) or (Event, Continuation)");
      }
      Subscribe anno = m.getAnnotation(Subscribe.class);
      List<SubscribedEventListener> list = ret.computeIfAbsent(eventClass, _ -> new ArrayList<>());
      list.add(new SubscribedEventListener(listener, anno.priority(), m, withContinuation));
    }
    return ret;
  }

  /**
   * Register all event listeners for the passed subscription event listener.
   *
   * @param bus bus to register to
   * @param listener listener
   */
  public static void registerListeners(
      @NotNull EventBus bus, @NotNull SubscriptionEventListener listener) {
    Objects.requireNonNull(bus, "bus");
    Objects.requireNonNull(listener, "listener");
    for (Map.Entry<Class<? extends Event>, List<SubscribedEventListener>> entry :
        bakeListeners(listener).entrySet()) {
      for (SubscribedEventListener sub : entry.getValue()) {
        bus.registerListener(entry.getKey(), sub);
      }
    }
  }
}
