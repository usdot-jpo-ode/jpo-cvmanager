package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "scms_health")
public class ScmsHealth {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scms_health_id_gen")
    @SequenceGenerator(name = "scms_health_id_gen", sequenceName = "scms_health_scms_health_id_seq", allocationSize = 1)
    @Column(name = "scms_health_id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "\"timestamp\"", nullable = false)
    private Instant timestamp;

    @NotNull
    @Column(name = "health", nullable = false, columnDefinition = "bit(1)")
    private Boolean health;

    @Column(name = "expiration")
    private Instant expiration;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rsu_id", nullable = false)
    private Rsu rsu;

}