package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_organization", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "organization_id"}))
public class UserOrganization {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_organization_id_gen")
    @SequenceGenerator(name = "user_organization_id_gen", sequenceName = "user_organization_user_organization_id_seq", allocationSize = 1)
    @Column(name = "user_organization_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;


}
