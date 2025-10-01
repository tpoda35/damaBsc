package org.dama.damajatek.exception.auth;

public class LogoutException extends RuntimeException{
    public LogoutException(String message) {
        super(message);
    }

    public LogoutException() {
        super("Logout failed.");
    }
}
