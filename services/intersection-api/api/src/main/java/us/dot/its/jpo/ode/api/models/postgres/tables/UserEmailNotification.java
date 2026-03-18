package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "email_type_id", nullable = false)
    private EmailType emailType;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "immediate", nullable = false)
    private Boolean immediate;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "hourly", nullable = false)
    private Boolean hourly;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "daily", nullable = false)
    private Boolean daily;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "weekly", nullable = false)
    private Boolean weekly;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "monthly", nullable = false)
    private Boolean monthly;


}