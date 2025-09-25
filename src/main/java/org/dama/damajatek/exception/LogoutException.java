package org.dama.damajatek.exception;

public class LogoutException extends RuntimeException{
    public LogoutException(String message) {
        super(message);
    }

    public LogoutException() {
        super("Logout failed.");
    }
}
