package com.mrivanplays.events;

/** Represents an identifier of a listener. */
public interface ListenerIdentifier {

  /**
   * Compare to another identifier and return whether they are the same
   *
   * @param other identifier to compare to
   * @return true if matches, false otherwise
   */
  boolean matches(ListenerIdentifier other);
}
