package org.dama.damajatek.exception.room;

public class HostCannotLeaveException extends RuntimeException {
    public HostCannotLeaveException() {
        super("Host cannot leave their own room");
    }

    public HostCannotLeaveException(String message) {
        super(message);
    }
}
