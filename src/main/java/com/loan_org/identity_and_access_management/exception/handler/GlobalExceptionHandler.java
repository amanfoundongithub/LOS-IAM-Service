package com.loan_org.identity_and_access_management.exception.handler;

import com.loan_org.identity_and_access_management.exception.AccountAlreadyExistsException;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountAlreadyExistsException(
            AccountAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("WARNING: Triggered an AccountAlreadyExistsException with the following message: {}",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                create(ex, HttpStatus.CONFLICT, request)
        );
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotFoundException(
            AccountNotFoundException ex, HttpServletRequest request) {
        log.warn("WARNING: Triggered an AccountNotFoundException with the following message: {}",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                create(ex, HttpStatus.NOT_FOUND, request)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.debug("WARNING: Encountered malformed request for a service, with the following message: {}. Violations: {}",
                ex.getMessage(),
                errors);
        ApiErrorResponse response = create(ex, HttpStatus.BAD_REQUEST, request);
        response.setValidationErrors(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(
            Exception ex, HttpServletRequest request) {
        log.warn("WARNING: Encountered an internal server exception with message: {}",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                create(ex, HttpStatus.INTERNAL_SERVER_ERROR, request)
        );
    }

    private ApiErrorResponse create(Exception ex,
                                    HttpStatus status,
                                    HttpServletRequest request
                                    ) {
        return ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

}
