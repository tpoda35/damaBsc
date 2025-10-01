package org.dama.damajatek.exception.room;

public class PlayersNotReadyException extends RuntimeException {
    public PlayersNotReadyException() {
        super("Players are not ready");
    }

    public PlayersNotReadyException(String message) {
        super(message);
    }
}
