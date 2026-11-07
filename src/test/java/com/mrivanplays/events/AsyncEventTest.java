package com.mrivanplays.events;

import com.mrivanplays.events.reflection.Subscribe;
import com.mrivanplays.events.reflection.SubscribedEventListenerBakery;
import com.mrivanplays.events.reflection.SubscriptionEventListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/*
 Either I am really tired as of writing this code in 22:15 o'clock
 or the only way to check properly whether concurrent tasks
 run in JUnit is to use a latch and latch#await to block and allow
 after that JUnit to _assert_ and _actually_ check the test result output
*/
public class AsyncEventTest {

  static CountDownLatch latch = new CountDownLatch(3);
  static AtomicBoolean cancelled = new AtomicBoolean(true);
  static ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
  static EventBus bus = EventBus.createAsyncEventBus(executor, new CatchExceptionLogger());

  public static class Listener implements SubscriptionEventListener {

    @Subscribe
    private void asyncResult(TestResultedEvent event, Continuation continuation) {
      executor.schedule(
          () -> {
            event.setResult(ResultedEvent.GenericResult.DENIED);
            continuation.resume();
          },
          100,
          TimeUnit.MILLISECONDS);
    }

    @Subscribe
    private void onEvent(TestEvent event, Continuation continuation) {
      executor.schedule(
          () -> {
            latch.countDown();
            continuation.resume();
          },
          500,
          TimeUnit.MILLISECONDS);
    }

    @Subscribe
    private void onEventLast(TestEvent event, Continuation continuation) {
      executor.submit(() -> {
        latch.countDown();
        continuation.resume();
      });
    }

    @Override
    public boolean matches(ListenerIdentifier other) {
      return other instanceof Listener;
    }
  }

  @BeforeAll
  static void init() {
    SubscribedEventListenerBakery.registerListeners(bus, new AsyncEventTest.Listener());
  }

  @Test
  void testFireAsynchronousEvent() throws Exception {
    bus.fire(new TestEvent()).whenComplete((e, _) -> latch.countDown());
    latch.await(2, TimeUnit.SECONDS);
    Assertions.assertEquals(0, latch.getCount());
  }

  @Test
  void testFireAsyncResultedEvent() throws Exception {
    bus.fire(new TestResultedEvent()).whenComplete((e, _) -> cancelled.set(e.result().allowed()));
    Thread.sleep(150);
    Assertions.assertFalse(cancelled.get());
  }

  @AfterAll
  static void shutdown() {
    executor.shutdown();
  }
}
