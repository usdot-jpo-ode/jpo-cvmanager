package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@EqualsAndHashCode
@Getter
@Entity
@Table(name = "user_email_notification")
public class UserEmailNotification {

    @Id
    private int user_email_notification_id;
    private int user_id;
    private int email_type_id;

}