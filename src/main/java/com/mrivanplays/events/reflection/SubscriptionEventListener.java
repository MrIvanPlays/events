package com.mrivanplays.events.reflection;

import com.mrivanplays.events.ListenerIdentifier;

/**
 * Classes which listen to multiple events via methods, annotated with {@link Subscribe}, must
 * implement this interface.
 */
public interface SubscriptionEventListener extends ListenerIdentifier {}
