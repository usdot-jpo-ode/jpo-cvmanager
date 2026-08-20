package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "intersection_organization", uniqueConstraints = @UniqueConstraint(columnNames = {"intersection_id", "organization_id"}))
public class IntersectionOrganization {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "intersection_organization_id_gen")
    @SequenceGenerator(name = "intersection_organization_id_gen", sequenceName = "intersection_organization_intersection_organization_id_seq", allocationSize = 1)
    @Column(name = "intersection_organization_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intersection_id", nullable = false)
    private Intersection intersection;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;


}
