package org.dama.damajatek.exception.auth;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Refresh token is expired or invalid. Log in again.");
    }

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
