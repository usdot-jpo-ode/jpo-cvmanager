package us.dot.its.jpo.ode.api.controllers.advice;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import us.dot.its.jpo.ode.api.services.RsuCredentialManagementService;
import us.dot.its.jpo.ode.api.services.SnmpCredentialManagementService;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler()
    public ProblemDetail handleEntityNotFoundException(EntityNotFoundException e) {
        String message = e.getMessage();
        log.error(message);
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler()
    public ProblemDetail handleRsuCredentialAlreadyExistsException(RsuCredentialManagementService.RsuCredentialAlreadyExistsException e) {
        String message = e.getMessage();
        log.error(message);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler()
    public ProblemDetail handleSnmpCredentialAlreadyExistsException(SnmpCredentialManagementService.SnmpCredentialAlreadyExistsException e) {
        String message = e.getMessage();
        log.error(message);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler()
    public ProblemDetail handleAccessDeniedException(AccessDeniedException e) {
        String message = e.getMessage();
        log.error(message);
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, message);
    }
}
