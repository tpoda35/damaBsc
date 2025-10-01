package org.dama.damajatek.exception.room;

public class RoomAlreadyFullException extends RuntimeException {
    public RoomAlreadyFullException() {
        super("Room is full");
    }

    public RoomAlreadyFullException(String message) {
        super(message);
    }
}
