/*
 * Copyright (c) Ivan Pekov 2026
 * Copyright (c) Contributors 2026
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.mrivanplays.events;

import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ParallelCallTest {

  static EventBus bus =
      EventBus.createAsyncEventBus(
          Executors.newFixedThreadPool(5),
          ExceptionLogger.from(Logger.getLogger("ParallelCallTest")));
  static Consumer<TestEvent> listener = e -> e.called.incrementAndGet();

  @BeforeAll
  static void init() {
    final DummyIdentifier identifier = DummyIdentifier.INSTANCE;
    bus.registerListener(identifier, TestEvent.class, listener);
    bus.registerListener(identifier, TestEvent.class, EventListener.Priority.LAST, listener);
    bus.registerListener(
        identifier,
        TestEvent.class,
        (e, c) -> {
          e.called.incrementAndGet();
          e.parallel.set(c.isParallel());
          c.resume();
        });
  }

  @Test
  void testParallelCall() {
    TestEvent event = bus.fireParallel(new TestEvent()).join();

    Assertions.assertEquals(3, event.called.get());
    Assertions.assertTrue(event.parallel.get());
  }
}
