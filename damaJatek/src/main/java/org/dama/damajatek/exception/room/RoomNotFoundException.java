package org.dama.damajatek.exception.room;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException() {
        super("Room not found.");
    }

    public RoomNotFoundException(String message) {
        super(message);
    }
}
