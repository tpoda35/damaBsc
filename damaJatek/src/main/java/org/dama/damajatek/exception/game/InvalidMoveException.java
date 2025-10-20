package org.dama.damajatek.exception.game;

public class InvalidMoveException extends RuntimeException {
    public InvalidMoveException() {
        super("Invalid move");
    }
    public InvalidMoveException(String message) {
        super(message);
    }
}
