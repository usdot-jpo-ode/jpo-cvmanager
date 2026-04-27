package us.dot.its.jpo.ode.api.models.emails;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EmailResponseException extends RuntimeException {
    private final EmailApiResponse response;
    private final HttpStatus status;

    public EmailResponseException(EmailApiResponse response, HttpStatus status) {
        super(String.format("Email operation completed with status %s", status));
        this.response = response;
        this.status = status;
    }

    public static EmailResponseException multiStatus(EmailApiResponse response) {
        return new EmailResponseException(response, HttpStatus.MULTI_STATUS);
    }

    public static EmailResponseException internalServerError(EmailApiResponse response) {
        return new EmailResponseException(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}