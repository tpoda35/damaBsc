package org.dama.damajatek.exception.auth;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException() {
        super("Missing refresh token. Please log in again.");
    }

    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}
