package com.sara.booktracker.exception;

/**
 * One problem with one field or parameter, inside an error response's "errors" array.
 * field is null when the problem is about the request as a whole.
 * (Named ApiFieldError so it isn't confused with Spring's own FieldError.)
 */
public record ApiFieldError(String field, String message) {
}
