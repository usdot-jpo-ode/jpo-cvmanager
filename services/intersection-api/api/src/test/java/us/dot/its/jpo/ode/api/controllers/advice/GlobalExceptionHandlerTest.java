package us.dot.its.jpo.ode.api.controllers.advice;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import us.dot.its.jpo.ode.api.services.RsuCredentialManagementService;
import us.dot.its.jpo.ode.api.services.SnmpCredentialManagementService;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleEntityNotFoundException() {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException("Entity not found");

        // Act
        ProblemDetail problemDetail = handler.handleEntityNotFoundException(exception);

        // Assert
        assertNotNull(problemDetail);
        assertEquals("Entity not found", problemDetail.getDetail());
        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
    }

    @Test
    void testHandleRsuCredentialAlreadyExistsException() {
        // Arrange
        RsuCredentialManagementService.RsuCredentialAlreadyExistsException exception = new RsuCredentialManagementService.RsuCredentialAlreadyExistsException("RSU Credential already exists");

        // Act
        ProblemDetail problemDetail = handler.handleRsuCredentialAlreadyExistsException(exception);

        // Assert
        assertNotNull(problemDetail);
        assertEquals("RSU Credential already exists", problemDetail.getDetail());
        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
    }

    @Test
    void testHandleSnmpCredentialAlreadyExistsException() {
        // Arrange
        SnmpCredentialManagementService.SnmpCredentialAlreadyExistsException exception = new SnmpCredentialManagementService.SnmpCredentialAlreadyExistsException("SNMP Credential already exists");

        // Act
        ProblemDetail problemDetail = handler.handleSnmpCredentialAlreadyExistsException(exception);

        // Assert
        assertNotNull(problemDetail);
        assertEquals("SNMP Credential already exists", problemDetail.getDetail());
        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
    }

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