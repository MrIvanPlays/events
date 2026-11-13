package com.mrivanplays.events;

import com.mrivanplays.events.reflection.Subscribe;
import com.mrivanplays.events.reflection.SubscribedEventListenerBakery;
import com.mrivanplays.events.reflection.SubscriptionEventListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ContinuationCompleteExceptionallyTest {

  static class Listener implements SubscriptionEventListener {

    @Subscribe
    public void onEvent(TestEvent event, Continuation continuation) {
      event.parallel.set(continuation.isParallel());
      continuation.resumeWithException(new RuntimeException("hello"));
    }

    @Subscribe(priority = EventListener.Priority.LAST)
    public void onEvent(TestEvent event) {
      event.called.incrementAndGet();
    }

    @Override
    public boolean matches(ListenerIdentifier other) {
      return true;
    }
  }

  static CatchExceptionLogger logger = new CatchExceptionLogger();
  static EventBus bus = EventBus.createSyncEventBus(logger);

  @BeforeAll
  static void init() {
    SubscribedEventListenerBakery.registerListeners(bus, new Listener());
  }

  @Test
  void testCall() {
    var event = bus.fire(new TestEvent()).join();

    Assertions.assertEquals(1, event.called.get());
    Assertions.assertEquals(1, logger.caught.size());
    Assertions.assertFalse(event.parallel.get());
  }
}
