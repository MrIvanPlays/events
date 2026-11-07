package com.mrivanplays.events.reflection;

import com.mrivanplays.events.EventListener;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods annotated by this annotation will be baked into event listeners via {@link
 * SubscribedEventListenerBakery} and registered to a specified {@link
 * com.mrivanplays.events.EventBus event bus}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface Subscribe {

  /**
   * The priority of which the method to be wrapped into a listener.
   *
   * @return priority
   */
  short priority() default EventListener.Priority.NORMAL;
}
