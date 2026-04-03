package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "iss_keys")
public class IssKey {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "iss_keys_id_gen")
    @SequenceGenerator(name = "iss_keys_id_gen", sequenceName = "iss_keys_iss_key_id_seq", allocationSize = 1)
    @Column(name = "iss_key_id", nullable = false)
    private Integer issKeyId;

    @Size(max = 128)
    @NotNull
    @Column(name = "common_name", nullable = false, length = 128)
    private String commonName;

    @Size(max = 128)
    @NotNull
    @Column(name = "token", nullable = false, length = 128)
    private String token;

}