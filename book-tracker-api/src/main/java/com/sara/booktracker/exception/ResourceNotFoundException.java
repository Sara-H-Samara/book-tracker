package com.sara.booktracker.exception;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String resource, Long id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, resource + " with id " + id + " not found");
    }
}