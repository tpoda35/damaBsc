package org.dama.damajatek.exception.room;

public class HostCannotJoinOwnRoomException extends RuntimeException {
  public HostCannotJoinOwnRoomException() {
    super("Host cannot join as opponent in their own room");
  }

  public HostCannotJoinOwnRoomException(String message) {
    super(message);
  }
}

