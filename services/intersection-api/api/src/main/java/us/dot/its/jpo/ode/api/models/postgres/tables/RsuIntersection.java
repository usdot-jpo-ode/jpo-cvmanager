package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rsu_intersection", uniqueConstraints = {
    @UniqueConstraint(name = "rsu_intersection_unique", columnNames = {"rsu_id", "intersection_id"})
})
public class RsuIntersection {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rsu_intersection_id_gen")
    @SequenceGenerator(name = "rsu_intersection_id_gen", sequenceName = "rsu_intersection_rsu_intersection_id_seq", allocationSize = 1)
    @Column(name = "rsu_intersection_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rsu_id", nullable = false)
    private Rsu rsu;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intersection_id", nullable = false)
    private Intersection intersection;

}
