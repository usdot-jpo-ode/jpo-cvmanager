package us.dot.its.jpo.ode.api.models.emails.contents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupportRequestEmailContents {
    private String email;
    private String subject;
    private String message;
}
