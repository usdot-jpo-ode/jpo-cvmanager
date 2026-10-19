package us.dot.its.jpo.ode.api.models.postgres.tables;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** OBU firmware the OBU OTA server can serve once published. */
@Getter
@Setter
@Entity
@Table(name = "obu_firmware_images", uniqueConstraints = {
        @UniqueConstraint(name = "obu_firmware_images_manufacturer_file_name_unique",
                columnNames = {"manufacturer", "file_name"}),
        @UniqueConstraint(name = "obu_firmware_images_destination_unique",
                columnNames = {"storage_provider", "storage_container", "object_name"})})
public class ObuFirmwareImage {
    /** Storage provider for files on the OBU OTA server's local firmware mount. */
    public static final String LOCAL_STORAGE_PROVIDER = "local";
    /** Storage container recorded for files on the OBU OTA server's local firmware mount. */
    public static final String LOCAL_STORAGE_CONTAINER = "local";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "obu_firmware_images_id_gen")
    @SequenceGenerator(name = "obu_firmware_images_id_gen",
            sequenceName = "obu_firmware_images_obu_firmware_id_seq", allocationSize = 1)
    @Column(name = "obu_firmware_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manufacturer", nullable = false)
    private Manufacturer manufacturer;

    @Size(max = 128)
    @NotNull
    @Column(name = "file_name", nullable = false, length = 128)
    private String fileName;

    @Valid
    @NotNull
    @Embedded
    private ObuFirmwareFileMetadata metadata;

    @Size(max = 32)
    @NotNull
    @Column(name = "storage_provider", nullable = false, length = 32)
    private String storageProvider;

    @Size(max = 255)
    @NotNull
    @Column(name = "storage_container", nullable = false, length = 255)
    private String storageContainer;

    @NotNull
    @Column(name = "object_name", nullable = false, columnDefinition = "text")
    private String objectName;

    @Column(name = "content_length")
    private Long contentLength;

    @Size(max = 32)
    @Column(name = "checksum_algorithm", length = 32)
    private String checksumAlgorithm;

    @Size(max = 128)
    @Column(name = "checksum", length = 128)
    private String checksum;

    @Column(name = "provider_object_version", columnDefinition = "text")
    private String providerObjectVersion;

    // Rows registered directly for local files have no upload record.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_upload_id", unique = true)
    private ObuFirmwareUpload verifiedUpload;

    @NotNull
    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt = Instant.now();

    @NotNull
    @Column(name = "published", nullable = false)
    private Boolean published = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Size(max = 255)
    @Column(name = "published_by", length = 255)
    private String publishedBy;

    public boolean isLocal() {
        return LOCAL_STORAGE_PROVIDER.equals(storageProvider);
    }
}
