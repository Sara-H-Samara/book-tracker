package com.sara.booktracker.exception;

import com.sara.booktracker.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;          // ← new
import org.springframework.security.core.AuthenticationException;          // ← new
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns EVERY exception into the same ApiResponse envelope as successful responses:
 * status, message, data (= null), errors, errorCode, timestamp.
 *
 * ResponseEntityExceptionHandler already maps every built-in Spring MVC exception to the
 * right status (405, 406, 415, ...). We override the ones we want our own message for,
 * and handleExceptionInternal() wraps all the others.
 *
 * Error responses are always sent as application/json, even if the client's Accept header
 * asked for something else (e.g. XML on a 406), so the error itself can always be read.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String GENERIC_SERVER_ERROR = "Something went wrong. Please try again later.";

    // ===================== 1. Our own exceptions =====================

    /**
     * Every child of AppException: 401 / 404 / 409 / 422                     // ← updated
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        return error(ex.getErrorCode(), ex.getMessage());
    }

    // ===================== 2. Security (401 / 403) =====================    // ← new section

    /**
     * No token, or an invalid / expired token, on a protected endpoint: 401.
     * Security errors happen in the filters, before any controller, so SecurityErrorHandler
     * passes them here. That way every error has the same JSON shape.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        return error(ErrorCode.UNAUTHORIZED, "Login required: the token is missing, invalid or expired");
    }

    /**
     * Logged in, but not allowed: 403.
     * From a URL rule (e.g. POST /api/books) or from @PreAuthorize on a controller method.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return error(ErrorCode.ACCESS_DENIED, "You are not allowed to access this resource");
    }

    // ===================== 3. Bad requests (400) =====================

    /**
     * Validation failed on a request body or filter
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ApiFieldError> errors = new ArrayList<>();
        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            // ?minPages=abc: Spring's own message names Java types, so we don't send it
            String message = err.isBindingFailure() ? "has an invalid value" : err.getDefaultMessage();
            errors.add(new ApiFieldError(err.getField(), message));
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(err -> errors.add(new ApiFieldError(null, err.getDefaultMessage())));

        return respond(ex, ErrorCode.VALIDATION_FAILED, "Validation failed", errors, headers, request);
    }

    /**
     * Validation failed on a @RequestParam / @PathVariable, e.g. ?year=3000
     */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ApiFieldError> errors = new ArrayList<>();
        ex.getParameterValidationResults().forEach(result -> {
            String parameter = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(err -> {
                String field = (err instanceof FieldError fieldError) ? fieldError.getField() : parameter;
                errors.add(new ApiFieldError(field, err.getDefaultMessage()));
            });
        });

        return respond(ex, ErrorCode.VALIDATION_FAILED, "Validation failed", errors, headers, request);
    }

    /**
     * /api/books/abc → "abc" can't become a Long
     */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String name = (ex instanceof MethodArgumentTypeMismatchException mismatch) ? mismatch.getName() : ex.getPropertyName();
        String expectedType = (ex.getRequiredType() != null) ? ex.getRequiredType().getSimpleName() : "another type";

        return respond(ex, ErrorCode.INVALID_PARAMETER, "'" + name + "' must be of type " + expectedType,
                List.of(new ApiFieldError(name, "must be of type " + expectedType)), headers, request);
    }

    /**
     * Broken JSON, wrong JSON types, or an unknown enum value like "DONE"
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return respond(ex, ErrorCode.MALFORMED_REQUEST, "Request body is missing or is not valid JSON",
                null, headers, request);
    }

    /**
     * A required ?param is missing, e.g. /api/shelf without ?userId
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String name = ex.getParameterName();
        return respond(ex, ErrorCode.MISSING_PARAMETER, "Required parameter '" + name + "' is missing",
                List.of(new ApiFieldError(name, "is required")), headers, request);
    }

    /**
     * ?sort=pagez → no such field
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadSort(PropertyReferenceException ex) {
        return error(ErrorCode.INVALID_SORT, "Cannot sort by '" + ex.getPropertyName() + "'");
    }

    // ===================== 4. Wrong URL / method / content type =====================

    /**
     * /api/nothing → no endpoint here: 404
     */
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return respond(ex, ErrorCode.ENDPOINT_NOT_FOUND,
                "No endpoint " + ex.getHttpMethod() + " /" + ex.getResourcePath(), null, headers, request);
    }

    /**
     * DELETE on a URL that only supports GET/POST: 405 (headers carry "Allow")
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return respond(ex, ErrorCode.METHOD_NOT_ALLOWED,
                "Method " + ex.getMethod() + " is not supported here", null, headers, request);
    }

    /**
     * POST without Content-Type: application/json: 415
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return respond(ex, ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                "Content-Type must be application/json", null, headers, request);
    }

    // ===================== 5. Database =====================

    /**
     * A unique/foreign-key constraint was broken (e.g. two requests saving the same ISBN at once): 409
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return error(ErrorCode.DATA_CONFLICT, "The request conflicts with existing data");
    }

    // ===================== 6. Safety nets =====================

    /**
     * Anything we didn't expect (a bug): 500, with NO internal details sent to the client
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);   // full stack trace in OUR logs, not in the response
        return error(ErrorCode.INTERNAL_ERROR, GENERIC_SERVER_ERROR);
    }

    /**
     * Every Spring MVC exception ends here. The ones we didn't override above (e.g. 406)
     * arrive with Spring's default ProblemDetail body and get wrapped in our envelope;
     * server-side ones (5xx) get a generic message.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        if (!(body instanceof ApiResponse<?>)) {
            String message;
            if (statusCode.is5xxServerError()) {
                log.error("Unexpected error", ex);
                message = GENERIC_SERVER_ERROR;
            } else {
                // Spring's default details for client errors are safe, e.g. "Acceptable representations: [...]"
                message = (body instanceof ProblemDetail springDefault) ? springDefault.getDetail() : null;
            }
            body = ApiResponse.error(statusCode, fallbackCode(statusCode), message, null);
        }

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.addAll(headers);   // keeps e.g. "Allow" on a 405
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        return super.handleExceptionInternal(ex, body, jsonHeaders, statusCode, request);
    }

    // ===================== Helpers =====================

    /** For the overrides above: build the envelope, then go through handleExceptionInternal. */
    private ResponseEntity<Object> respond(Exception ex, ErrorCode code, String message, List<ApiFieldError> errors,
                                           HttpHeaders headers, WebRequest request) {
        return handleExceptionInternal(ex, ApiResponse.error(code, message, errors), headers, code.getStatus(), request);
    }

    /** For our own @ExceptionHandler methods. */
    private static ResponseEntity<ApiResponse<Void>> error(ErrorCode code, String message) {
        return ResponseEntity.status(code.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(code, message, null));
    }

    /** errorCode for Spring MVC exceptions we didn't override. */
    private static ErrorCode fallbackCode(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> ErrorCode.ENDPOINT_NOT_FOUND;
            case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
            case 406 -> ErrorCode.NOT_ACCEPTABLE;
            case 415 -> ErrorCode.UNSUPPORTED_MEDIA_TYPE;
            default -> status.is4xxClientError() ? ErrorCode.BAD_REQUEST : ErrorCode.INTERNAL_ERROR;
        };
    }
}