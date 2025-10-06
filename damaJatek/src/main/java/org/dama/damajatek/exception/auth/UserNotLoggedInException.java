package org.dama.damajatek.exception.auth;

public class UserNotLoggedInException extends RuntimeException {
    public UserNotLoggedInException() {
        super("User is not logged in");
    }

    public UserNotLoggedInException(String message) {
        super(message);
    }
}
