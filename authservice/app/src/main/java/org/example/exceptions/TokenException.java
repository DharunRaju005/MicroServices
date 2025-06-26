package org.example.exceptions;

/**
 * Exception thrown for token-related errors such as expired tokens,
 * invalid tokens, or token validation failures.
 */
public class TokenException extends RuntimeException {
    
    public TokenException(String message) {
        super(message);
    }
    
    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}