package us.dot.its.jpo.ode.api.models.postgres.tables;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "organizations")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organizations_id_gen")
    @SequenceGenerator(name = "organizations_id_gen", sequenceName = "organizations_organization_id_seq", allocationSize = 1)
    @Column(name = "organization_id", nullable = false)
    private Integer id;

    @Size(max = 128)
    @NotNull
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Size(max = 128)
    @Column(name = "email", length = 128)
    private String email;

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    private List<IntersectionOrganization> intersectionOrganizations;

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    private List<UserOrganization> userOrganizations;

}