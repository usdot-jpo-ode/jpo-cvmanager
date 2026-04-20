package us.dot.its.jpo.ode.api.controllers.advice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.ode.api.services.RsuCredentialManagementService;
import us.dot.its.jpo.ode.api.services.RsuUpgradeService;
import us.dot.its.jpo.ode.api.services.SnmpCredentialManagementService;

/**
 * Global exception handler for REST API endpoints.
 * Provides standardized, user-friendly error responses following RFC 7807
 * Problem Details.
 * 
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7807">RFC 7807 -
 *      Problem Details for HTTP APIs</a>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // Pattern to extract constraint name from PostgreSQL error messages
    private static final Pattern CONSTRAINT_PATTERN = Pattern.compile("constraint \\[([^\\]]+)\\]");

    // Pattern to extract duplicate key details from PostgreSQL error messages
    private static final Pattern DUPLICATE_KEY_PATTERN = Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]+)\\)");

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Resource requested not found: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        var errorRes = ErrorResponse.builder(ex, problemDetail);
        return errorRes.build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler()
    public ProblemDetail handleRsuCredentialAlreadyExistsException(
            RsuCredentialManagementService.RsuCredentialAlreadyExistsException e) {
        String message = e.getMessage();
        log.error(message);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler()
    public ProblemDetail handleSnmpCredentialAlreadyExistsException(
            SnmpCredentialManagementService.SnmpCredentialAlreadyExistsException e) {
        String message = e.getMessage();
        log.error(message);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler()
    public ProblemDetail handleFirmwareUpgradeUnavailableException(
            RsuUpgradeService.FirmwareUpgradeUnavailableException e) {
        String message = e.getMessage();
        log.warn(message);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler()
    public ProblemDetail handleAccessDeniedException(AccessDeniedException e) {
        String message = e.getMessage();
        log.error(message);
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, message);
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        var errorRes = ErrorResponse.builder(ex, problemDetail);
        return errorRes.build();
    }

    /**
     * Handles ResponseStatusException thrown by controllers.
     * Allows controllers to throw exceptions with specific HTTP status codes and
     * messages.
     * 
     * Example: throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not
     * found");
     */
    @ExceptionHandler()
    public ErrorResponse handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        // Log at different levels based on status code
        if (status.is5xxServerError()) {
            log.error("Server error ({}): {}", status.value(), ex.getReason(), ex);
        } else if (status.is4xxClientError()) {
            log.warn("Client error ({}): {}", status.value(), ex.getReason());
        } else {
            log.info("Response status exception ({}): {}", status.value(), ex.getReason());
        }

        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);

        var errorRes = ErrorResponse.builder(ex, problemDetail);
        return errorRes.build();
    }

    /**
     * Handles validation errors for @Validated method parameters (path variables,
     * request params).
     * Thrown when @Validated constraints on method parameters fail.
     * 
     * Example: @PathVariable @Min(1) Integer id
     */
    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        // Build user-friendly validation error message
        String validationErrors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    // Extract just the property name (remove method path prefix)
                    String propertyPath = violation.getPropertyPath().toString();
                    String propertyName = propertyPath.contains(".")
                            ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                            : propertyPath;
                    return String.format("%s: %s", propertyName, violation.getMessage());
                })
                .collect(Collectors.joining("; "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed: " + validationErrors);

        // Add detailed violations as additional property
        Map<String, String> violations = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String propertyName = propertyPath.contains(".")
                    ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                    : propertyPath;
            violations.put(propertyName, violation.getMessage());
        }
        problemDetail.setProperty("violations", violations);

        var errorRes = ErrorResponse.builder(ex, problemDetail);
        return errorRes.build();
    }

    /**
     * Handles validation errors for @Valid/@Validated request bodies.
     * Thrown when @Valid constraints on @RequestBody fail.
     * 
     * Example: public void create(@Valid @RequestBody UserDto user)
     */
    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("Method argument validation failed: {}", ex.getMessage());

        // Build user-friendly validation error message
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed: " + validationErrors);

        // Add detailed field errors as additional property
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        problemDetail.setProperty("fieldErrors", fieldErrors);

        var errorRes = ErrorResponse.builder(ex, problemDetail);
        return errorRes.build();
    }

    /**
     * Handle missing required request header exceptions
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        log.warn("Missing required request header: {}", ex.getHeaderName());

        String message = String.format("Required request header '%s' is not present", ex.getHeaderName());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Missing Header");

        return problemDetail;
    }

    /**
     * Handles database constraint violations with user-friendly messages.
     * Extracts constraint details and provides human-readable error messages
     * without exposing SQL implementation details.
     * 
     * Common constraint violations handled:
     * - Duplicate key violations (unique constraints)
     * - Foreign key violations
     * - Not null violations
     * - Check constraint violations
     */
    @ExceptionHandler()
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String originalMessage = ex.getMessage();
        log.warn("Data integrity violation: {}", originalMessage);

        String userFriendlyMessage = buildUserFriendlyMessage(originalMessage, ex);

        // Use HTTP 409 Conflict for constraint violations (more appropriate than 400
        // Bad Request)
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                userFriendlyMessage);

        // Add constraint name as additional property if available
        String constraintName = extractConstraintName(originalMessage);
        if (constraintName != null) {
            problemDetail.setProperty("constraint", constraintName);
        }

        var errorRes = ErrorResponse.builder(ex, problemDetail);
        return errorRes.build();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleServletRequestBinding(ServletRequestBindingException ex) {
        log.warn("Request binding error: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ErrorResponse.builder(ex, problemDetail).build();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex) {
        log.error("Unexpected server error:", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        var errorRes = ErrorResponse.builder(ex, problemDetail);
        return errorRes.build();
    }

    private String buildUserFriendlyMessage(String message, DataIntegrityViolationException ex) {
        if (message == null) {
            return "A database constraint was violated. Please check your input and try again.";
        }

        String lowerCaseMessage = message.toLowerCase();

        if (lowerCaseMessage.contains("duplicate key")) {
            return buildDuplicateKeyMessage(message);
        }
        if (lowerCaseMessage.contains("foreign key")) {
            return buildForeignKeyMessage(message);
        }
        if (lowerCaseMessage.contains("not null") || lowerCaseMessage.contains("violates not-null")) {
            return buildNotNullMessage(message);
        }
        if (lowerCaseMessage.contains("check constraint")) {
            return buildCheckConstraintMessage(message);
        }

        String constraintName = extractConstraintName(message);
        if (constraintName != null) {
            return String.format("The operation violates the '%s' constraint. Please verify your input.",
                    formatConstraintName(constraintName));
        }

        return "A database constraint was violated. Please check your input and try again.";
    }

    private String buildDuplicateKeyMessage(String message) {
        Matcher matcher = DUPLICATE_KEY_PATTERN.matcher(message);

        if (matcher.find()) {
            String fields = matcher.group(1);
            String values = matcher.group(2);

            String[] fieldArray = fields.split(",\\s*");
            String[] valueArray = values.split(",\\s*");

            StringBuilder details = new StringBuilder();
            for (int i = 0; i < Math.min(fieldArray.length, valueArray.length); i++) {
                if (i > 0) {
                    details.append(" and ");
                }
                details.append(formatFieldName(fieldArray[i].trim()))
                        .append(" '")
                        .append(valueArray[i].trim())
                        .append("'");
            }

            String resourceType = determineResourceType(message);
            return String.format("%s with %s already exists.", resourceType, details.toString());
        }

        return "A record with these values already exists. Please use different values.";
    }

    private String buildForeignKeyMessage(String message) {
        if (message.contains("not present")) {
            return "The referenced item does not exist. Please verify the relationship.";
        } else if (message.contains("still referenced")) {
            return "This item cannot be deleted because it is being used by other records.";
        }

        String constraintName = extractConstraintName(message);
        if (constraintName != null) {
            return String.format("Foreign key constraint '%s' was violated. Please verify related records exist.",
                    formatConstraintName(constraintName));
        }

        return "A foreign key constraint was violated. Please verify that all related records exist.";
    }

    private String buildNotNullMessage(String message) {
        Pattern columnPattern = Pattern.compile("column \"([^\"]+)\"");
        Matcher matcher = columnPattern.matcher(message);

        if (matcher.find()) {
            String columnName = matcher.group(1);
            return String.format("The field '%s' is required and cannot be empty.",
                    formatFieldName(columnName));
        }

        return "A required field is missing. Please provide all required information.";
    }

    private String buildCheckConstraintMessage(String message) {
        String constraintName = extractConstraintName(message);

        if (constraintName != null) {
            return String.format("The value violates the '%s' validation rule. Please check your input.",
                    formatConstraintName(constraintName));
        }

        return "The provided value does not meet the validation requirements.";
    }

    private String extractConstraintName(String message) {
        Matcher matcher = CONSTRAINT_PATTERN.matcher(message);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String determineResourceType(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("rsu") || lowerMessage.contains("rsus")) {
            return "RSU";
        } else if (lowerMessage.contains("user")) {
            return "User";
        } else if (lowerMessage.contains("organization")) {
            return "Organization";
        } else if (lowerMessage.contains("credential")) {
            return "Credential";
        } else if (lowerMessage.contains("intersection")) {
            return "Intersection";
        }

        return "record";
    }

    private String formatFieldName(String fieldName) {
        if (fieldName == null) {
            return "";
        }

        if (fieldName.equalsIgnoreCase("ipv4_address")) {
            return "IPv4 address";
        }
        if (fieldName.equalsIgnoreCase("ipv6_address")) {
            return "IPv6 address";
        }

        return fieldName.replace("_", " ");
    }

    private String formatConstraintName(String constraintName) {
        if (constraintName == null) {
            return "";
        }

        String formatted = constraintName.replaceAll("^(rsu|user|org)_", "");
        return formatted.replace("_", " ");
    }
}
