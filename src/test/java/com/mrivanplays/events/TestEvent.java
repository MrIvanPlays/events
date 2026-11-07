package com.mrivanplays.events;

import java.util.concurrent.atomic.AtomicInteger;

public class TestEvent implements Event {
  final AtomicInteger called = new AtomicInteger(0);
}
