package org.dama.damajatek.exception.auth;

/**
 * Exception thrown when the provided current password
 * does not match the stored password of the authenticated user.
 */
public class WrongPasswordException extends RuntimeException {
    public WrongPasswordException() {
        super("Wrong password");
    }

    public WrongPasswordException(String message) {
        super(message);
    }
}
