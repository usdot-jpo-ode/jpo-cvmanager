package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_email_notification")
public class UserEmailNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_email_notification_id_gen")
    @SequenceGenerator(name = "user_email_notification_id_gen", sequenceName = "user_email_notification_user_email_notification_id_seq", allocationSize = 1)
    @Column(name = "user_email_notification_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "email_type_id", nullable = false)
    private EmailType emailType;


}