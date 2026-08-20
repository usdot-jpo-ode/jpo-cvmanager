package us.dot.its.jpo.ode.api.models.emails.contents;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Contents of support request email, including email address of the user submitting the support request, email subject, and email message body")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupportRequestEmailContents {
    @Schema(description = "Email address of the user submitting the support request")
    @NotEmpty(message = "Email address cannot be empty")
    @NotNull(message = "Email address cannot be null")
    private String email;
    @Schema(description = "Email subject")
    @NotEmpty(message = "Email subject cannot be empty")
    @NotNull(message = "Email subject cannot be null")
    private String subject;
    @Schema(description = "Email message body")
    @NotEmpty(message = "Email message cannot be empty")
    @NotNull(message = "Email message cannot be null")
    private String message;
}
