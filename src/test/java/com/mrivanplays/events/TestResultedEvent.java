package com.mrivanplays.events;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class TestResultedEvent implements ResultedEvent<ResultedEvent.GenericResult> {

  private GenericResult result = GenericResult.ALLOWED;

  @Override
  public @NotNull GenericResult result() {
    return result;
  }

  @Override
  public void setResult(@NotNull ResultedEvent.GenericResult newResult) {
    this.result = Objects.requireNonNull(newResult, "result");
  }
}
