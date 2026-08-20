package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "max_retry_limit_reached_instances")
public class MaxRetryLimitReachedInstance {
    @EmbeddedId
    private MaxRetryLimitReachedInstanceId id;

    @MapsId("rsuId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rsu_id", nullable = false)
    private Rsu rsu;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_firmware_version", nullable = false)
    private FirmwareImage targetFirmwareVersion;


}
