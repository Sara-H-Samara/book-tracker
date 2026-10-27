package com.sara.booktracker.exception;

public class DuplicateResourceException extends AppException {

    public DuplicateResourceException(String message) {
        super(ErrorCode.DUPLICATE_RESOURCE, message);
    }
}