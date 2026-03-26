package com.dex.dex_insights_mini.exception;

import com.dex.dex_insights_mini.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleStoreNotFound(StoreNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.warn("event=store_not_found path={} message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status).body(errorBody(status, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(InvalidRequestException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn("event=invalid_request path={} message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status).body(errorBody(status, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(DataLoadException.class)
    public ResponseEntity<ApiErrorResponse> handleDataLoad(DataLoadException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("event=data_load_failed path={} message={}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(errorBody(status, "Failed to load application data", request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("event=validation_failed path={} errors={}", request.getRequestURI(), message);
        return ResponseEntity.status(status).body(errorBody(status, message, request.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Malformed JSON request body";
        log.warn("event=malformed_json path={} message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status).body(errorBody(status, message, request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Invalid value for parameter '" + ex.getName() + "'";
        log.warn("event=type_mismatch path={} parameter={} value={}", request.getRequestURI(), ex.getName(), ex.getValue());
        return ResponseEntity.status(status).body(errorBody(status, message, request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn("event=illegal_argument path={} message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status).body(errorBody(status, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("event=unexpected_error path={} message={}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(errorBody(status, "Unexpected server error", request.getRequestURI()));
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ApiErrorResponse errorBody(HttpStatus status, String message, String path) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }
}

