package org.dama.damajatek.exception;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super("Passwords are not the same.");
    }

    public PasswordMismatchException(String message) {
        super(message);
    }
}
