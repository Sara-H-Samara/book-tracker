package com.sara.booktracker.exception;

/**
 * Parent of all our own exceptions. Every child carries an ErrorCode,
 * so ONE handler can turn any of them into the right HTTP response.
 */
public abstract class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    protected AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}