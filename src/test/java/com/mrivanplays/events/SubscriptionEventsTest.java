package com.mrivanplays.events;

import com.mrivanplays.events.reflection.Subscribe;
import com.mrivanplays.events.reflection.SubscribedEventListenerBakery;
import com.mrivanplays.events.reflection.SubscriptionEventListener;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SubscriptionEventsTest {

  static class Capture {

    List<String> captures = new ArrayList<>();

    public void capture(String s) {
      captures.add(s);
    }
  }

  public static class Listener implements SubscriptionEventListener {

    @Subscribe
    public void onEvent(TestEvent event) {
      event.called.incrementAndGet();
      capture.capture("normal");
    }

    @Subscribe(priority = EventListener.Priority.FIRST)
    public void onEventFirst(TestEvent event, Continuation continuation) {
      event.called.incrementAndGet();
      capture.capture("first");
      continuation.resume();
    }

    @Subscribe
    public void prioritized(PrioritizedTestEvent event) {
      capture.capture("PrioritizedTestEvent");
    }

    @Override
    public boolean matches(ListenerIdentifier other) {
      return other instanceof Listener; // just for the sake of the test
    }
  }

  static Capture capture = new Capture();
  static final EventBus bus = EventBus.createSyncEventBus(new CatchExceptionLogger());
  static Listener listener = new Listener();

  @BeforeEach
  void register() {
    SubscribedEventListenerBakery.registerListeners(bus, listener);
  }

  @AfterEach
  void unregister() {
    capture.captures.clear();
    bus.unregisterListeners(listener);
  }

  @Test
  void testFire() {
    var event = bus.fire(new TestEvent()).join();

    Assertions.assertEquals(2, event.called.get());
    Assertions.assertEquals("first", capture.captures.getFirst());
    Assertions.assertEquals("normal", capture.captures.get(1));
  }

  @Test
  void testFireDifferent() {
    bus.fire(new PrioritizedTestEvent());

    Assertions.assertEquals(1, capture.captures.size());
    Assertions.assertEquals("PrioritizedTestEvent", capture.captures.getFirst());
  }

  @Test
  void testUnregister() {
    bus.unregisterListener(listener, TestEvent.class);
    var testEvent = bus.fire(new TestEvent()).join();
    bus.fire(new PrioritizedTestEvent());

    Assertions.assertEquals(0, testEvent.called.get());
    Assertions.assertEquals(1, capture.captures.size());
    Assertions.assertEquals("PrioritizedTestEvent", capture.captures.getFirst());
  }
}
