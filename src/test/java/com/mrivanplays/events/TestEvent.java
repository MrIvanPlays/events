package com.mrivanplays.events;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestEvent implements Event {
  final AtomicInteger called = new AtomicInteger(0);
  final AtomicBoolean parallel = new AtomicBoolean(false);
}
