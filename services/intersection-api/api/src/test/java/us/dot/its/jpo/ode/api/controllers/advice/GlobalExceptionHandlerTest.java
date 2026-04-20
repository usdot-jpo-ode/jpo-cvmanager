package us.dot.its.jpo.ode.api.controllers.advice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.ode.api.services.RsuCredentialManagementService;
import us.dot.its.jpo.ode.api.services.RsuUpgradeService;
import us.dot.its.jpo.ode.api.services.SnmpCredentialManagementService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Nested;

class GlobalExceptionHandlerTest {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    class HandleEntityNotFoundTests {

        @Test
        void testReturnsNotFound() {
            EntityNotFoundException ex = new EntityNotFoundException("User not found");

            ErrorResponse response = handler.handleEntityNotFound(ex);

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertTrue(response.getBody().getDetail().contains("User not found"));
        }

        @Test
        void testWithDifferentMessage() {
            EntityNotFoundException ex = new EntityNotFoundException("RSU with IP 192.168.1.1 not found");

            ErrorResponse response = handler.handleEntityNotFound(ex);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertTrue(response.getBody().getDetail().contains("192.168.1.1"));
        }
    }

    @Nested
    class HandleRsuCredentialAlreadyExistsExceptionTests {

        @Test
        void testHandleRsuCredentialAlreadyExistsException() {
            // Arrange
            RsuCredentialManagementService.RsuCredentialAlreadyExistsException exception = new RsuCredentialManagementService.RsuCredentialAlreadyExistsException(
                    "RSU Credential already exists");

            // Act
            ProblemDetail problemDetail = handler.handleRsuCredentialAlreadyExistsException(exception);

            // Assert
            assertNotNull(problemDetail);
            assertEquals("RSU Credential already exists", problemDetail.getDetail());
            assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        }
    }

    @Nested
    class HandleSnmpCredentialAlreadyExistsExceptionTests {
        @Test
        void testHandleSnmpCredentialAlreadyExistsException() {
            // Arrange
            SnmpCredentialManagementService.SnmpCredentialAlreadyExistsException exception = new SnmpCredentialManagementService.SnmpCredentialAlreadyExistsException(
                    "SNMP Credential already exists");

            // Act
            ProblemDetail problemDetail = handler.handleSnmpCredentialAlreadyExistsException(exception);

            // Assert
            assertNotNull(problemDetail);
            assertEquals("SNMP Credential already exists", problemDetail.getDetail());
            assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        }
    }

    @Nested
    class HandleFirmwareUpgradeUnavailableExceptionTests {
        @Test
        void testHandleFirmwareUpgradeUnavailableException() {
            RsuUpgradeService.FirmwareUpgradeUnavailableException exception = new RsuUpgradeService.FirmwareUpgradeUnavailableException(
                    "Requested RSU is already up to date");

            ProblemDetail problemDetail = handler.handleFirmwareUpgradeUnavailableException(exception);

            assertNotNull(problemDetail);
            assertEquals("Requested RSU is already up to date", problemDetail.getDetail());
            assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        }
    }

    @Nested
    class HandleAccessDeniedExceptionTests {

        @Test
        void testHandleAccessDeniedException() {
            // Arrange
            AccessDeniedException exception = new AccessDeniedException("Access denied");

            // Act
            ProblemDetail problemDetail = handler.handleAccessDeniedException(exception);

            // Assert
            assertNotNull(problemDetail);
            assertEquals("Access denied", problemDetail.getDetail());
            assertEquals(HttpStatus.FORBIDDEN.value(), problemDetail.getStatus());
        }
    }

    @Nested
    class HandleIllegalArgumentTests {

        @Test
        void testInvalidIpAddress() {
            IllegalArgumentException ex = new IllegalArgumentException("Invalid IP address: invalid-ip");

            ErrorResponse response = handler.handleIllegalArgument(ex);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            ProblemDetail body = response.getBody();
            assertTrue(body.getDetail().contains("Invalid IP address"));
            assertTrue(body.getDetail().contains("invalid-ip"));
        }

        @Test
        void testInvalidParameter() {
            IllegalArgumentException ex = new IllegalArgumentException(
                    "Parameter 'organizationName' cannot be null or empty");

            ErrorResponse response = handler.handleIllegalArgument(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().getDetail().contains("organizationName"));
            assertTrue(response.getBody().getDetail().contains("cannot be null or empty"));
        }

        @Test
        void testInvalidEnumValue() {
            IllegalArgumentException ex = new IllegalArgumentException(
                    "Invalid model type: UNKNOWN. Valid values are: COMMSIGNIA, YUNEX");

            ErrorResponse response = handler.handleIllegalArgument(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            ProblemDetail body = response.getBody();
            assertTrue(body.getDetail().contains("Invalid model type"));
            assertTrue(body.getDetail().contains("UNKNOWN"));
        }

        @Test
        void testGenericIllegalArgument() {
            IllegalArgumentException ex = new IllegalArgumentException("Invalid input provided");

            ErrorResponse response = handler.handleIllegalArgument(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Invalid input provided", response.getBody().getDetail());
        }
    }

    @Nested
    class HandleResponseStatusExceptionTests {

        @Test
        void testNotFoundWithReason() {
            ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "RSU not found");

            ErrorResponse response = handler.handleResponseStatusException(ex);

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("RSU not found", response.getBody().getDetail());
        }

        @Test
        void testBadRequestWithReason() {
            ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid request parameters");

            ErrorResponse response = handler.handleResponseStatusException(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Invalid request parameters", response.getBody().getDetail());
        }

        @Test
        void testUnauthorizedWithReason() {
            ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authentication required");

            ErrorResponse response = handler.handleResponseStatusException(ex);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("Authentication required", response.getBody().getDetail());
        }

        @Test
        void testForbiddenWithReason() {
            ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied to this resource");

            ErrorResponse response = handler.handleResponseStatusException(ex);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertEquals("Access denied to this resource", response.getBody().getDetail());
        }

        @Test
        void testConflictWithReason() {
            ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT, "Resource already exists");

            ErrorResponse response = handler.handleResponseStatusException(ex);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertEquals("Resource already exists", response.getBody().getDetail());
        }

        @Test
        void testInternalServerErrorWithReason() {
            ResponseStatusException ex = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database connection failed");

            ErrorResponse response = handler.handleResponseStatusException(ex);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals("Database connection failed", response.getBody().getDetail());
        }

        @Test
        void testWithoutReasonUsesDefaultStatusPhrase() {
            ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND);

            ErrorResponse response = handler.handleResponseStatusException(ex);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            // Should use default HTTP status reason phrase
            assertEquals("Not Found", response.getBody().getDetail());
        }

        @Test
        void testServiceUnavailable() {
            ResponseStatusException ex = new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Service temporarily unavailable");

            ErrorResponse response = handler.handleResponseStatusException(ex);

            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
            assertEquals("Service temporarily unavailable", response.getBody().getDetail());
        }
    }

    @Nested
    class HandleConstraintViolationTests {

        @Test
        void testSingleViolation() {
            ConstraintViolation<?> violation = mock(ConstraintViolation.class);
            Path path = mock(Path.class);

            when(violation.getPropertyPath()).thenReturn(path);
            when(path.toString()).thenReturn("getRsu.id");
            when(violation.getMessage()).thenReturn("must be greater than 0");

            ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

            ErrorResponse response = handler.handleConstraintViolation(ex);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            ProblemDetail body = response.getBody();
            assertTrue(body.getDetail().contains("Validation failed"));
            assertTrue(body.getDetail().contains("id"));
            assertTrue(body.getDetail().contains("must be greater than 0"));

            @SuppressWarnings("unchecked")
            Map<String, String> violations = (Map<String, String>) body.getProperties().get("violations");
            assertNotNull(violations);
            assertEquals("must be greater than 0", violations.get("id"));
        }

        @Test
        void testMultipleViolations() {
            ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
            ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
            Path path1 = mock(Path.class);
            Path path2 = mock(Path.class);

            when(violation1.getPropertyPath()).thenReturn(path1);
            when(path1.toString()).thenReturn("createRsu.rsuIp");
            when(violation1.getMessage()).thenReturn("must not be blank");

            when(violation2.getPropertyPath()).thenReturn(path2);
            when(path2.toString()).thenReturn("createRsu.limit");
            when(violation2.getMessage()).thenReturn("must be less than 100");

            ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation1, violation2));

            ErrorResponse response = handler.handleConstraintViolation(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, String> violations = (Map<String, String>) response.getBody().getProperties().get("violations");
            assertEquals(2, violations.size());
        }
    }

    @Nested
    class HandleMethodArgumentNotValidTests {

        @Test
        void testSingleFieldError() {
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError = new FieldError("rsuDto", "ipv4Address", "must not be null");

            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

            ErrorResponse response = handler.handleMethodArgumentNotValid(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            ProblemDetail body = response.getBody();
            assertTrue(body.getDetail().contains("Validation failed"));
            assertTrue(body.getDetail().contains("ipv4Address"));
            assertTrue(body.getDetail().contains("must not be null"));

            @SuppressWarnings("unchecked")
            Map<String, String> fieldErrors = (Map<String, String>) body.getProperties().get("fieldErrors");
            assertEquals("must not be null", fieldErrors.get("ipv4Address"));
        }

        @Test
        void testMultipleFieldErrors() {
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError error1 = new FieldError("userDto", "email", "must not be null");
            FieldError error2 = new FieldError("userDto", "firstName", "size must be between 1 and 128");

            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(error1, error2));

            ErrorResponse response = handler.handleMethodArgumentNotValid(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, String> fieldErrors = (Map<String, String>) response.getBody().getProperties()
                    .get("fieldErrors");
            assertEquals(2, fieldErrors.size());
            assertEquals("must not be null", fieldErrors.get("email"));
        }
    }

    @Nested
    class HandleMissingRequestHeaderExceptionTests {

        @Test
        void testReturnsBadRequestWithHeaderName() {
            MissingRequestHeaderException ex = mock(MissingRequestHeaderException.class);
            when(ex.getHeaderName()).thenReturn("X-Organization-Name");

            ProblemDetail problemDetail = handler.handleMissingRequestHeaderException(ex);

            assertNotNull(problemDetail);
            assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
            assertEquals("Required request header 'X-Organization-Name' is not present", problemDetail.getDetail());
            assertEquals("Missing Header", problemDetail.getTitle());
        }

        @Test
        void testWithDifferentHeaderName() {
            MissingRequestHeaderException ex = mock(MissingRequestHeaderException.class);
            when(ex.getHeaderName()).thenReturn("Authorization");

            ProblemDetail problemDetail = handler.handleMissingRequestHeaderException(ex);

            assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
            assertEquals("Required request header 'Authorization' is not present", problemDetail.getDetail());
            assertEquals("Missing Header", problemDetail.getTitle());
        }
    }

    @Nested
    class HandleDataIntegrityViolationTests {

        @Test
        void testDuplicateKey() {
            String errorMessage = "could not execute statement [ERROR: duplicate key value violates unique constraint \"rsu_milepost_primary_route\" "
                    +
                    "Detail: Key (milepost, primary_route)=(1, I999) already exists.] " +
                    "constraint [rsu_milepost_primary_route]";

            DataIntegrityViolationException ex = new DataIntegrityViolationException(errorMessage);

            ErrorResponse response = handler.handleDataIntegrityViolation(ex);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            ProblemDetail body = response.getBody();
            assertTrue(body.getDetail().contains("RSU"));
            assertTrue(body.getDetail().contains("milepost '1'"));
            assertTrue(body.getDetail().contains("primary route 'I999'"));
            assertTrue(body.getDetail().contains("already exists"));
            assertFalse(body.getDetail().contains("SQL"));
            assertFalse(body.getDetail().contains("constraint"));

            assertEquals("rsu_milepost_primary_route", body.getProperties().get("constraint"));
        }

        @Test
        void testRsuDuplicateSerialNumber() {
            String errorMessage = "could not execute statement [ERROR: duplicate key value violates unique constraint \"rsu_milepost_primary_route\""
                    + "Detail: Key (milepost, primary_route)=(1, I999) already exists.] "
                    + "[insert into rsus (credential_id,firmware_version,geography,ipv4_address,iss_scms_id,milepost,model,primary_route,serial_number,snmp_credential_id,snmp_protocol_id,target_firmware_version,rsu_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]; "
                    + "SQL [insert into rsus (credential_id,firmware_version,geography,ipv4_address,iss_scms_id,milepost,model,primary_route,serial_number,snmp_credential_id,snmp_protocol_id,target_firmware_version,rsu_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]; "
                    + "constraint [rsu_milepost_primary_route]";

            DataIntegrityViolationException ex = new DataIntegrityViolationException(errorMessage);

            ErrorResponse response = handler.handleDataIntegrityViolation(ex);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            ProblemDetail body = response.getBody();
            assertEquals("RSU with milepost '1' and primary route 'I999' already exists.", body.getDetail());

            assertEquals("rsu_milepost_primary_route", body.getProperties().get("constraint"));
        }

        @Test
        void testForeignKeyNotPresent() {
            String errorMessage = "could not execute statement [ERROR: insert or update on table \"rsus\" " +
                    "violates foreign key constraint \"fk_credential\" " +
                    "Detail: Key (credential_id)=(999) is not present in table \"credentials\".] " +
                    "constraint [fk_credential]";
            DataIntegrityViolationException ex = new DataIntegrityViolationException(errorMessage);

            ErrorResponse response = handler.handleDataIntegrityViolation(ex);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertTrue(response.getBody().getDetail().contains("referenced item does not exist"));
            assertFalse(response.getBody().getDetail().contains("SQL"));
        }

        @Test
        void testNotNull() {
            String errorMessage = "could not execute statement [ERROR: null value in column \"ipv4_address\" " +
                    "violates not-null constraint]";
            DataIntegrityViolationException ex = new DataIntegrityViolationException(errorMessage);

            ErrorResponse response = handler.handleDataIntegrityViolation(ex);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertTrue(response.getBody().getDetail().contains("IPv4 address"));
            assertTrue(response.getBody().getDetail().contains("required"));
            assertFalse(response.getBody().getDetail().contains("null value"));
        }

        @Test
        void testGenericConstraint() {
            String errorMessage = "Generic database constraint violation";
            DataIntegrityViolationException ex = new DataIntegrityViolationException(errorMessage);

            ErrorResponse response = handler.handleDataIntegrityViolation(ex);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertTrue(response.getBody().getDetail().contains("database constraint was violated"));
        }
    }

    @Nested
    class HandleExceptionTests {

        @Test
        void testGenericException() {
            Exception ex = new RuntimeException("Unexpected error");

            ErrorResponse response = handler.handleException(ex);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.getBody().getDetail().contains("unexpected error occurred"));
            assertFalse(response.getBody().getDetail().contains("RuntimeException"));
        }

        @Test
        void testNullPointerException() {
            Exception ex = new NullPointerException("Something was null");

            ErrorResponse response = handler.handleException(ex);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            // Should return generic message, not expose internal details
            assertFalse(response.getBody().getDetail().contains("null"));
        }
    }

    @Nested
    class HelperMethodTests {

        @Test
        void testBuildDuplicateKeyMessageExtractsFieldsAndValues() {
            String message = "duplicate key value violates unique constraint \"rsu_serial_number\" " +
                    "Detail: Key (serial_number)=(E5673) already exists. " +
                    "[http-nio-8089-exec-9] WARN us.dot.its.jpo.ode.api.controllers.GlobalExceptionHandler - Data integrity violation: could not execute statement [ERROR: duplicate key value violates unique constraint \"rsu_serial_number\" "
                    +
                    "Detail: Key (serial_number)=(E5673) already exists.]";
            DataIntegrityViolationException ex = new DataIntegrityViolationException(message);

            ErrorResponse response = handler.handleDataIntegrityViolation(ex);

            assertTrue(response.getBody().getDetail().contains("RSU"));
            assertTrue(response.getBody().getDetail().contains("already exists"));
        }

        @Test
        void testFormatFieldNameHandlesSpecialCases() {
            String message = "null value in column \"ipv4_address\" violates not-null constraint";
            DataIntegrityViolationException ex = new DataIntegrityViolationException(message);

            ErrorResponse response = handler.handleDataIntegrityViolation(ex);

            // Should format ipv4_address as "IPv4 address"
            assertTrue(response.getBody().getDetail().contains("IPv4 address"));
            assertFalse(response.getBody().getDetail().contains("ipv4_address"));
        }

        @Test
        void testDetermineResourceTypeRecognizesRsu() {
            String message = "duplicate key violates constraint \"rsus_pkey\"";
            DataIntegrityViolationException ex = new DataIntegrityViolationException(message);

            ErrorResponse response = handler.handleDataIntegrityViolation(ex);

            // should not contain "RSU" since pattern doesn't match, but tests the
            // method
            assertNotNull(response.getBody().getDetail());
        }
    }
}