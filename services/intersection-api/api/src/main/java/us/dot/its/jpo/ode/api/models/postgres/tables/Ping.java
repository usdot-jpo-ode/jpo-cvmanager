package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "ping")
public class Ping {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ping_id_gen")
    @SequenceGenerator(name = "ping_id_gen", sequenceName = "ping_ping_id_seq", allocationSize = 1)
    @Column(name = "ping_id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "\"timestamp\"", nullable = false)
    private Instant timestamp;

    @NotNull
    @Column(name = "result", nullable = false, columnDefinition = "bit(1)")
    private Boolean result;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rsu_id", nullable = false)
    private Rsu rsu;

}