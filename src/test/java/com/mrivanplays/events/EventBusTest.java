package com.mrivanplays.events;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EventBusTest {

  static ListenerIdentifier DUMMY = DummyIdentifier.INSTANCE;
  static EventListener<TestEvent> listener =
      EventListener.of(DUMMY, event -> event.called.incrementAndGet());
  static BiConsumer<Short, PrioritizedTestEvent> prioritizedListener =
      (priority, event) -> {
        System.out.println("Run order priority: " + priority);
        event.order.add(priority);
      };
  static ExecutorService executor = Executors.newFixedThreadPool(10);
  static EventBus bus = EventBus.createAsyncEventBus(executor, new CatchExceptionLogger());

  @BeforeAll
  static void init() {
    bus.registerListener(TestEvent.class, listener);
    for (short i = 2; i > -1; i--) {
      short priority = EventListener.Priority.defaultValues()[i];
      bus.registerListener(
          DUMMY,
          PrioritizedTestEvent.class,
          priority,
          event -> prioritizedListener.accept(priority, event));
    }
  }

  @AfterAll
  static void shutdown() {
    executor.shutdown();
  }

  @AfterEach
  void reregister() {
    bus.unregisterListener(
        DUMMY, TestEvent.class); // will unregister all listeners with identifier and type
    bus.registerListener(TestEvent.class, listener);
  }

  @Test
  void testFire() {
    TestEvent event = bus.fire(new TestEvent()).join();

    Assertions.assertEquals(1, event.called.get());
  }

  @Test
  void testUnregister() {
    bus.unregisterListener(DUMMY, TestEvent.class);
    TestEvent event = bus.fire(new TestEvent()).join();

    Assertions.assertEquals(0, event.called.get());
  }

  @Test
  void testRegisterTwice() {
    bus.registerListener(TestEvent.class, listener);
    var event = bus.fire(new TestEvent()).join();

    Assertions.assertEquals(2, event.called.get());
  }

  @Test
  void testPriorityCall() {
    var event = bus.fire(new PrioritizedTestEvent()).join();

    Assertions.assertEquals(3, event.order.size());
    Assertions.assertSame(EventListener.Priority.FIRST, event.order.getFirst());
    Assertions.assertSame(EventListener.Priority.NORMAL, event.order.get(1));
    Assertions.assertSame(EventListener.Priority.LAST, event.order.get(2));
  }

  @Test
  void concurrencyTest() throws InterruptedException, ExecutionException {
    TestEvent event = new TestEvent();
    var futures = IntStream.range(0, 1000).mapToObj(_ -> bus.fire(event)).toList();
    for (var future : futures) {
      future.get();
    }

    Assertions.assertEquals(1000, event.called.get());
  }
}
