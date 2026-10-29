package com.sara.booktracker.exception;

public class ResourceNotFoundException extends AppException {

    // Not found by id → "Book with id 99 not found"
    public ResourceNotFoundException(String resource, Long id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, resource + " with id " + id + " not found");
    }

    // Not found by another field → "User with username 'sara' not found"
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(ErrorCode.RESOURCE_NOT_FOUND, resource + " with " + field + " '" + value + "' not found");
    }
}