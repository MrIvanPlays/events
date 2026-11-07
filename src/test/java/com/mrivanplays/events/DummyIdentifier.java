package com.mrivanplays.events;

public class DummyIdentifier implements ListenerIdentifier {

  public static DummyIdentifier INSTANCE = new DummyIdentifier();

  private DummyIdentifier() {}

  @Override
  public boolean matches(ListenerIdentifier other) {
    return other instanceof DummyIdentifier;
  }
}
