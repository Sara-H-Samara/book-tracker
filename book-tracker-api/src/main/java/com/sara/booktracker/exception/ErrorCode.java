package com.sara.booktracker.exception;

import org.springframework.http.HttpStatus;

/**
 * Every kind of error our API can return, with its HTTP status.
 * Services throw exceptions that carry a code; only this enum knows about HTTP.
 */
public enum ErrorCode {

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),             // 404
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT),              // 409
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_CONTENT);   // 422

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}