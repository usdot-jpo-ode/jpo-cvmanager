package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "firmware_images")
public class FirmwareImage {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "firmware_images_id_gen")
    @SequenceGenerator(name = "firmware_images_id_gen", sequenceName = "firmware_images_firmware_id_seq", allocationSize = 1)
    @Column(name = "firmware_id", nullable = false)
    private Integer id;

    @Size(max = 128)
    @NotNull
    @Column(name = "name", nullable = false, unique = true, length = 128)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model", nullable = false)
    private RsuModel model;

    @Size(max = 128)
    @NotNull
    @Column(name = "install_package", nullable = false, unique = true, length = 128)
    private String installPackage;

    @Size(max = 128)
    @NotNull
    @Column(name = "version", nullable = false, unique = true, length = 128)
    private String version;


}
