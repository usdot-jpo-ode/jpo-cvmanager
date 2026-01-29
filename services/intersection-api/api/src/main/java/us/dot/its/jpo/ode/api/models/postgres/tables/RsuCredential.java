package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rsu_credentials")
public class RsuCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rsu_credentials_id_gen")
    @SequenceGenerator(name = "rsu_credentials_id_gen", sequenceName = "rsu_credentials_credential_id_seq", allocationSize = 1)
    @Column(name = "credential_id", nullable = false)
    private Integer id;

    @Size(max = 128)
    @NotNull
    @Column(name = "username", nullable = false, length = 128)
    private String username;

    @Size(max = 128)
    @NotNull
    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Size(max = 128)
    @NotNull
    @Column(name = "nickname", nullable = false, length = 128)
    private String nickname;


}