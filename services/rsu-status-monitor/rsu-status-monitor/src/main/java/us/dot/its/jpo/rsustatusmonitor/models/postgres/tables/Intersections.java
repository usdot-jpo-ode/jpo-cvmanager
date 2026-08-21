package us.dot.its.jpo.rsustatusmonitor.models.postgres.tables;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Geometry;

@ToString
@Setter
@EqualsAndHashCode
@Getter
@Entity
@Table(name = "intersections")
public class Intersections {

    @Id
    private int intersection_id;
    private String intersection_number;
    private Geometry ref_pt;
    private Geometry bbox;
    private String intersection_name;
    private String origin_ip;

}