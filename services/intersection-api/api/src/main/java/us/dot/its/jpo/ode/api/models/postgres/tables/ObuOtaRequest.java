package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.net.InetAddress;
import java.time.LocalDateTime;

/**
 * An OBU firmware download attempt recorded by the OBU OTA server. Rows are
 * written by the OTA server and are read-only to the API.
 */
@Getter
@Setter
@Entity
@Immutable
@Table(name = "obu_ota_requests")
public class ObuOtaRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "obu_ota_requests_id_gen")
    @SequenceGenerator(name = "obu_ota_requests_id_gen", sequenceName = "obu_ota_request_id_seq", allocationSize = 1)
    @Column(name = "request_id", nullable = false)
    private Integer requestId;

    @Size(max = 128)
    @NotNull
    @Column(name = "obu_sn", nullable = false, length = 128)
    private String obuSn;

    /** Stored without a time zone; the OTA server writes UTC. */
    @NotNull
    @Column(name = "request_datetime", nullable = false)
    private LocalDateTime requestDatetime;

    @NotNull
    @Column(name = "origin_ip", nullable = false)
    private InetAddress originIp;

    @Size(max = 128)
    @NotNull
    @Column(name = "obu_firmware_version", nullable = false, length = 128)
    private String obuFirmwareVersion;

    @Size(max = 128)
    @NotNull
    @Column(name = "requested_firmware_version", nullable = false, length = 128)
    private String requestedFirmwareVersion;

    @NotNull
    @Column(name = "error_status", nullable = false)
    private Boolean errorStatus;

    @Size(max = 128)
    @NotNull
    @Column(name = "error_message", nullable = false, length = 128)
    private String errorMessage;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manufacturer", nullable = false)
    private Manufacturer manufacturer;

}
