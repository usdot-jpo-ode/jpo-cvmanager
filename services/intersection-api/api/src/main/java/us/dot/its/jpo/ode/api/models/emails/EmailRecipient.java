package us.dot.its.jpo.ode.api.models.emails;

import com.sendgrid.helpers.mail.objects.Email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailRecipient {
    private String email;
    private String name;

    public Email toSendGridEmail() {
        return new Email(this.email, this.name);
    }
}
