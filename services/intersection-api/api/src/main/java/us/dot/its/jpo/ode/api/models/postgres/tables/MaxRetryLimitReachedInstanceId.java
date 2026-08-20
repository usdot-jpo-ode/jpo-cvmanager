package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class MaxRetryLimitReachedInstanceId implements Serializable {
    private static final long serialVersionUID = 1483431920836543739L;
    @NotNull
    @Column(name = "rsu_id", nullable = false)
    private Integer rsuId;

    @NotNull
    @Column(name = "reached_at", nullable = false)
    private Instant reachedAt;


}