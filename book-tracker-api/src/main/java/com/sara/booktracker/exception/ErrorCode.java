package com.sara.booktracker.exception;

import org.springframework.http.HttpStatus;

/**
 * Every kind of error our API can return, with its HTTP status.
 * Services throw exceptions that carry a code; only this enum knows about HTTP.
 * The name of each constant is sent to clients as "errorCode".
 */
public enum ErrorCode {

    // ----- 400 Bad Request -----
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),            // a field or parameter broke a validation rule
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST),            // /api/books/abc: "abc" is not a Long
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST),            // /api/shelf without ?userId
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST),            // body missing or not valid JSON
    INVALID_SORT(HttpStatus.BAD_REQUEST),                 // ?sort=pagez
    BAD_REQUEST(HttpStatus.BAD_REQUEST),                  // any other client error from Spring MVC

    // ----- 401 / 403 Security -----
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),         // login: wrong username or password
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),                // no token, or an invalid / expired token
    ACCESS_DENIED(HttpStatus.FORBIDDEN),                  // logged in, but not allowed (wrong role)

    // ----- 404 / 405 / 406 / 415 -----
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),             // the URL is right, the id doesn't exist
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND),             // no endpoint at this URL
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE),            // client asked for a format we don't produce
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    // ----- 409 / 422 -----
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT),              // caught by our own check before saving
    DATA_CONFLICT(HttpStatus.CONFLICT),                   // caught by a database constraint
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_CONTENT),

    // ----- 500 -----
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}