package com.sara.booktracker.exception;

public class InvalidCredentialsException extends AppException {

    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
    }
}