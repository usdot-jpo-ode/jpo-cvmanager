package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rsu_models")
public class RsuModel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rsu_models_id_gen")
    @SequenceGenerator(name = "rsu_models_id_gen", sequenceName = "rsu_models_rsu_model_id_seq", allocationSize = 1)
    @Column(name = "rsu_model_id", nullable = false)
    private Integer id;

    @Size(max = 128)
    @NotNull
    @Column(name = "name", nullable = false, unique = true, length = 128)
    private String name;

    @Size(max = 128)
    @NotNull
    @Column(name = "supported_radio", nullable = false, length = 128)
    private String supportedRadio;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manufacturer", nullable = false)
    private Manufacturer manufacturer;


}