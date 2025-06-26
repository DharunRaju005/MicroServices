package org.example.exceptions;

/**
 * Exception thrown for authentication-related errors such as invalid credentials,
 * account locked, or other authentication failures.
 */
public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}