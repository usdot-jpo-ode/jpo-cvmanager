package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_gen")
    @SequenceGenerator(name = "users_id_gen", sequenceName = "users_user_id_seq", allocationSize = 1)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @NotNull
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "keycloak_id", nullable = false)
    private UUID keycloakId;

    @Size(max = 128)
    @NotNull
    @Column(name = "email", nullable = false, length = 128)
    private String email;

    @Size(max = 128)
    @Column(name = "first_name", length = 128)
    private String firstName;

    @Size(max = 128)
    @Column(name = "last_name", length = 128)
    private String lastName;

    @NotNull
    @Column(name = "created_timestamp", nullable = false)
    private Long createdTimestamp;

    @NotNull
    @ColumnDefault("(0)::bit(1)")
    @Column(name = "super_user", nullable = false, columnDefinition = "bit(1)")
    private Boolean superUser;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserOrganization> userOrganizations;

}