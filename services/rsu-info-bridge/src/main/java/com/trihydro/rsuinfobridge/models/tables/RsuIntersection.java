package com.trihydro.rsuinfobridge.models.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Simplified entity for rsu_intersection table.
 * Used for deleteAll operations in tests.
 */
@Getter
@Setter
@Entity
@Table(name = "rsu_intersection")
public class RsuIntersection {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rsu_intersection_id_gen")
    @SequenceGenerator(name = "rsu_intersection_id_gen", sequenceName = "rsu_intersection_rsu_intersection_id_seq", allocationSize = 1)
    @Column(name = "rsu_intersection_id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "rsu_id", nullable = false)
    private Integer rsuId;

    @NotNull
    @Column(name = "intersection_id", nullable = false)
    private Integer intersectionId;
}

