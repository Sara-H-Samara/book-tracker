package com.sara.booktracker.dto;

import com.sara.booktracker.exception.ApiFieldError;
import com.sara.booktracker.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;
import java.util.List;

/**
 * The ONE envelope every endpoint returns, for success and for errors:
 * { "status", "message", "data", "errors", "errorCode", "timestamp" }
 *
 * Success: data = the payload, errors = [], errorCode = null.
 * Error:   data = null, errors = field problems (or []), errorCode = an ErrorCode name.
 * <T> is the payload type: ApiResponse<BookResponse>, ApiResponse<PageResponse<BookResponse>>, ...
 */
public record ApiResponse<T>(
        int status,
        String message,
        T data,
        List<ApiFieldError> errors,
        String errorCode,
        Instant timestamp
) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return success(HttpStatus.CREATED, message, data);
    }

    public static ApiResponse<Void> error(ErrorCode code, String message, List<ApiFieldError> errors) {
        return error(code.getStatus(), code, message, errors);
    }

    /** For Spring MVC errors whose HTTP status isn't the ErrorCode's own status (see GlobalExceptionHandler). */
    public static ApiResponse<Void> error(HttpStatusCode status, ErrorCode code, String message,
                                          List<ApiFieldError> errors) {
        return new ApiResponse<>(status.value(), message, null,
                errors != null ? errors : List.of(), code.name(), Instant.now());
    }

    private static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status.value(), message, data, List.of(), null, Instant.now());
    }
}
