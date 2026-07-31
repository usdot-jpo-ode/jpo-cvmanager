package com.trihydro.rsuinfobridge.testutil.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Minimal test-only mapping of the scms_health telemetry table. The bridge has
 * no production entity for this table; this exists so tests can clear telemetry
 * rows (RESTRICT FK to rsus) through a JPA repository before deleting RSUs.
 * Cleanup-only: not all columns are mapped, so it must not be used for inserts
 * and is incompatible with hibernate ddl-auto validate/update.
 */
@Getter
@Setter
@Entity
@Table(name = "scms_health")
public class ScmsHealth {
    @Id
    @Column(name = "scms_health_id", nullable = false)
    private Integer id;

    @Column(name = "rsu_id", nullable = false)
    private Integer rsuId;
}
