package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.util.List;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

@Getter
@Setter
@Entity
@Table(name = "intersections")
public class Intersection {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "intersections_id_gen")
    @SequenceGenerator(name = "intersections_id_gen", sequenceName = "intersections_intersection_id_seq", allocationSize = 1)
    @Column(name = "intersection_id", nullable = false)
    private Integer id;

    @Size(max = 128)
    @NotNull
    @Column(name = "intersection_number", nullable = false, length = 128)
    private String intersectionNumber;

    @Column(name = "ref_pt", columnDefinition = "geography not null")
    private Point refPt;

    @Column(name = "bbox", columnDefinition = "geography")
    private Polygon bbox;

    @Size(max = 128)
    @Column(name = "intersection_name", length = 128)
    private String intersectionName;

    @Column(name = "origin_ip")
    private InetAddress originIp;

    @OneToMany(mappedBy = "intersection", fetch = FetchType.LAZY)
    private List<IntersectionOrganization> intersectionOrganizations;

    @OneToMany(mappedBy = "intersection", fetch = FetchType.LAZY)
    private List<RsuIntersection> rsuIntersections;

}