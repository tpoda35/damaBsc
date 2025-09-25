package org.dama.damajatek.exception;

/**
 * Exception thrown when the provided new password and confirmation password
 * do not match during a password change attempt.
 */
public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super("Passwords are not the same.");
    }

    public PasswordMismatchException(String message) {
        super(message);
    }
}
