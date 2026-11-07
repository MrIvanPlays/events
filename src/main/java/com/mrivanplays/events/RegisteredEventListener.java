package com.mrivanplays.events;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record RegisteredEventListener<T extends Event>(
    EventListener<T> listener, long registrationSequence) {}
