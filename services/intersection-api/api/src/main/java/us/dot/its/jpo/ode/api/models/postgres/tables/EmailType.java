package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "email_type")
public class EmailType {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_type_id_gen")
    @SequenceGenerator(name = "email_type_id_gen", sequenceName = "email_type_email_type_id_seq", allocationSize = 1)
    @Column(name = "email_type_id", nullable = false)
    private Integer id;

    @Size(max = 128)
    @NotNull
    @Column(name = "email_type", nullable = false, length = 128)
    private String emailType;

    @Size(max = 256)
    @Column(name = "description", length = 256)
    private String description;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "supports_immediate", nullable = false)
    private Boolean supportsImmediate;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "supports_hourly", nullable = false)
    private Boolean supportsHourly;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "supports_daily", nullable = false)
    private Boolean supportsDaily;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "supports_weekly", nullable = false)
    private Boolean supportsWeekly;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "supports_monthly", nullable = false)
    private Boolean supportsMonthly;


}