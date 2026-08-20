package us.dot.its.jpo.ode.api.models.emails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailContent {
    private String subject;
    private String body;
}
