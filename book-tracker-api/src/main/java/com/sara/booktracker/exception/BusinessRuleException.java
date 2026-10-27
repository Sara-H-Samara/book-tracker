package com.sara.booktracker.exception;

/** A valid request that breaks a business rule, e.g. "a book on someone's shelf can't be deleted". */
public class BusinessRuleException extends AppException {

    public BusinessRuleException(String message) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}