package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rsu_organization")
public class RsuOrganization {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rsu_organization_id_gen")
    @SequenceGenerator(name = "rsu_organization_id_gen", sequenceName = "rsu_organization_rsu_organization_id_seq", allocationSize = 1)
    @Column(name = "rsu_organization_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rsu_id", nullable = false)
    private Rsu rsu;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;


}