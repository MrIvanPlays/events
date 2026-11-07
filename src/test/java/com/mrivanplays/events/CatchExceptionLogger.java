package com.mrivanplays.events;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class CatchExceptionLogger implements ExceptionLogger {

  List<Throwable> caught = new ArrayList<>();

  @Override
  public void log(@NotNull Throwable e) {
    caught.add(e);
  }
}
