package org.dama.damajatek.exception.game;

public class GameAlreadyFinishedException extends RuntimeException {
    public GameAlreadyFinishedException() {
        super("The game is already finished");
    }

    public GameAlreadyFinishedException(String message) {
        super(message);
    }
}
