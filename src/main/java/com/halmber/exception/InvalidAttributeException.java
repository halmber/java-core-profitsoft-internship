package com.halmber.exception;

/**
 * Exception thrown when an invalid attribute is provided for statistics processing.
 * This is an unchecked exception (RuntimeException) because it indicates a programming error,
 * not a recoverable condition.
 */
public class InvalidAttributeException extends RuntimeException {
    public InvalidAttributeException(String message) {
        super(message);
    }

    public InvalidAttributeException(String message, Throwable cause) {
        super(message, cause);
    }
}