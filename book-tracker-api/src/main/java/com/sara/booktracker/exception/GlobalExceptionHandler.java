package com.sara.booktracker.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns EVERY exception into the same error shape (RFC 9457 ProblemDetail):
 * type, title, status, detail, instance + errorCode + timestamp.
 * Spring picks the most specific handler; Exception.class is the last safety net.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===================== 1. Our own exceptions =====================

    /**
     * Every child of AppException: 404 / 409 / 422
     */
    @ExceptionHandler(AppException.class)
    public ProblemDetail handleAppException(AppException ex) {
        ErrorCode code = ex.getErrorCode();
        return problem(code.getStatus(), ex.getMessage(), code.name());
    }

    // ===================== 2. Bad requests (400) =====================

    /**
     * Validation failed on a request body or filter
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION_FAILED");
        problem.setProperty("errors", errors);
        return problem;
    }

    /**
     * /api/books/abc → "abc" can't become a Long
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expectedType = (ex.getRequiredType() != null) ? ex.getRequiredType().getSimpleName() : "another type";
        return problem(HttpStatus.BAD_REQUEST,
                "'" + ex.getName() + "' must be of type " + expectedType, "INVALID_PARAMETER");
    }

    /**
     * Broken JSON, wrong JSON types, or an unknown enum value like "DONE"
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST,
                "Request body is missing or is not valid JSON", "MALFORMED_REQUEST");
    }

    /**
     * A required ?param is missing, e.g. /api/shelf without ?userId
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
        return problem(HttpStatus.BAD_REQUEST,
                "Required parameter '" + ex.getParameterName() + "' is missing", "MISSING_PARAMETER");
    }

    /**
     * ?sort=pagez → no such field
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleBadSort(PropertyReferenceException ex) {
        return problem(HttpStatus.BAD_REQUEST,
                "Cannot sort by '" + ex.getPropertyName() + "'", "INVALID_SORT");
    }

    // ===================== 3. Wrong URL / method / content type =====================

    /**
     * /api/nothing → no endpoint here: 404
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoEndpoint(NoResourceFoundException ex) {
        return problem(HttpStatus.NOT_FOUND,
                "No endpoint " + ex.getHttpMethod() + " /" + ex.getResourcePath(), "ENDPOINT_NOT_FOUND");
    }

    /**
     * DELETE on a URL that only supports GET/POST: 405
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return problem(HttpStatus.METHOD_NOT_ALLOWED,
                "Method " + ex.getMethod() + " is not supported here", "METHOD_NOT_ALLOWED");
    }

    /**
     * POST without Content-Type: application/json: 415
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content-Type must be application/json", "UNSUPPORTED_MEDIA_TYPE");
    }

    // ===================== 4. Database =====================

    /**
     * A unique/foreign-key constraint was broken (e.g. two requests saving the same ISBN at once): 409
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT,
                "The request conflicts with existing data", "DATA_CONFLICT");
    }

    // ===================== 5. Safety net =====================

    /**
     * Anything we didn't expect (a bug): 500, with NO internal details sent to the client
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);   // full stack trace in OUR logs, not in the response
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again later.", "INTERNAL_ERROR");
    }

    // ===================== Helper =====================

    /**
     * The ONE place that builds an error response, so every error has the same fields.
     */
    private ProblemDetail problem(HttpStatus status, String detail, String errorCode) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("errorCode", errorCode);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}