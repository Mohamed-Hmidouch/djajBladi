package org.example.djajbladibackend.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions
 * ✅ Spring Boot Best Practice: @RestControllerAdvice pour gestion centralisée des erreurs
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Validation failed"
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());
        
        log.error("Validation error: {}", errors);
        return problemDetail;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "Invalid email or password"
        );
        problemDetail.setTitle("Authentication Failed");
        problemDetail.setProperty("timestamp", Instant.now());
        
        log.error("Bad credentials: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setProperty("timestamp", Instant.now());
        
        log.error("Resource not found: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(RegistrationNotAllowedException.class)
    public ProblemDetail handleRegistrationNotAllowed(RegistrationNotAllowedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Registration Not Allowed");
        problemDetail.setProperty("timestamp", Instant.now());
        
        log.warn("Registration not allowed: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(InvalidRoleForAdminCreationException.class)
    public ProblemDetail handleInvalidRoleForAdminCreation(InvalidRoleForAdminCreationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Invalid Role");
        problemDetail.setProperty("timestamp", Instant.now());
        
        log.warn("Invalid role for admin user creation: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(DuplicateBatchNumberException.class)
    public ProblemDetail handleDuplicateBatchNumber(DuplicateBatchNumberException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Duplicate Batch Number");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Duplicate batch number: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Email Already Exists");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Email already exists: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(DuplicateDailyMortalityException.class)
    public ProblemDetail handleDuplicateDailyMortality(DuplicateDailyMortalityException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Duplicate Daily Mortality");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Duplicate daily mortality: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(HealthRecordNotPendingException.class)
    public ProblemDetail handleHealthRecordNotPending(HealthRecordNotPendingException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Health Record Not Pending");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Health record not pending: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(ForbiddenRoleException.class)
    public ProblemDetail handleForbiddenRole(ForbiddenRoleException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            ex.getMessage()
        );
        problemDetail.setTitle("Forbidden");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Forbidden role: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Bad Request");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Invalid argument: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(BatchNotActiveException.class)
    public ProblemDetail handleBatchNotActive(BatchNotActiveException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Batch Not Active");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Batch not active: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(MortalityExceedsBatchSizeException.class)
    public ProblemDetail handleMortalityExceedsBatchSize(MortalityExceedsBatchSizeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Mortality Exceeds Batch Size");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Mortality exceeds batch size: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(DateRangeTooLargeException.class)
    public ProblemDetail handleDateRangeTooLarge(DateRangeTooLargeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Date Range Too Large");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Date range too large: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(InvalidDataException.class)
    public ProblemDetail handleInvalidData(InvalidDataException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Invalid Data");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Invalid data: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String detail = ex.getCause() instanceof JsonProcessingException
                ? "Malformed JSON. Check request body format and field types."
                : "Request body could not be read or parsed.";
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle("Malformed Request");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Malformed request: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Required parameter '" + ex.getParameterName() + "' is missing"
        );
        problemDetail.setTitle("Missing Parameter");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Missing parameter: {}", ex.getParameterName());
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, b) -> a
                ));
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Validation failed for request parameters"
        );
        problemDetail.setTitle("Parameter Validation Error");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Constraint violation: {}", errors);
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = "Parameter '" + ex.getName() + "' has invalid value. Expected type: " +
                (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle("Invalid Parameter Type");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Type mismatch: {} = {}", ex.getName(), ex.getValue());
        return problemDetail;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.METHOD_NOT_ALLOWED,
            "HTTP method " + ex.getMethod() + " is not supported for this endpoint"
        );
        problemDetail.setTitle("Method Not Allowed");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Method not allowed: {}", ex.getMethod());
        return problemDetail;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Content-Type " + ex.getContentType() + " is not supported. Use application/json."
        );
        problemDetail.setTitle("Unsupported Media Type");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Unsupported media type: {}", ex.getContentType());
        return problemDetail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        String detail = message != null && (message.contains("unique") || message.contains("duplicate"))
                ? "Duplicate or conflicting data. The record may already exist."
                : "Data integrity constraint violated.";
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
        problemDetail.setTitle("Data Integrity Violation");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("Data integrity violation: {}", ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handleNotFound(NoHandlerFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "No endpoint found for " + ex.getHttpMethod() + " " + ex.getRequestURL()
        );
        problemDetail.setTitle("Not Found");
        problemDetail.setProperty("timestamp", Instant.now());
        log.warn("No handler: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", Instant.now());
        
        log.error("Unexpected error", ex);
        return problemDetail;
    }
}
