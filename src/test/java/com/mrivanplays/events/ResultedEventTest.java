package com.mrivanplays.events;

import com.mrivanplays.events.reflection.Subscribe;
import com.mrivanplays.events.reflection.SubscribedEventListenerBakery;
import com.mrivanplays.events.reflection.SubscriptionEventListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ResultedEventTest {

  public static class Subscribed implements SubscriptionEventListener {

    @Subscribe
    public void onEvent(TestResultedEvent event) {
      event.setResult(ResultedEvent.GenericResult.ALLOWED);
    }

    @Subscribe(priority = EventListener.Priority.LAST)
    public void onEventLast(TestResultedEvent event) {
      event.setResult(ResultedEvent.GenericResult.DENIED);
    }

    @Override
    public boolean matches(ListenerIdentifier other) {
      return true;
    }
  }

  static EventListener<TestResultedEvent> func =
      EventListener.of(
          DummyIdentifier.INSTANCE,
          EventListener.Priority.FIRST,
          event -> event.setResult(ResultedEvent.GenericResult.ALLOWED));
  static EventBus bus = EventBus.createSyncEventBus(new CatchExceptionLogger());

  @BeforeAll
  static void init() {
    SubscribedEventListenerBakery.registerListeners(bus, new Subscribed());
    bus.registerListener(TestResultedEvent.class, func);
  }

  @Test
  void testCallResultedEvent() {
    var called = bus.fire(new TestResultedEvent()).join();

    Assertions.assertFalse(called.result().allowed());
  }
}
