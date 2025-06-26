package org.example.exceptions;

/**
 * Exception thrown for user-related errors such as user not found,
 * duplicate username, or other user management issues.
 */
public class UserException extends RuntimeException {
    
    public UserException(String message) {
        super(message);
    }
    
    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}