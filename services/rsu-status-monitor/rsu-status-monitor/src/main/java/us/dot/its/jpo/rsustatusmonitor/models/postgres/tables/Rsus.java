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
@Table(name = "rsus")
public class Rsus {

    @Id
    private int rsu_id;
    private Geometry geography;
    private float milepost;
    private String ipv4_address;
    private String serial_number;
    private String iss_scms_id;
    private String primary_route;
    private int model;
    private int credential_id;
    private int snmp_credential_id;
    private int snmp_protocol_id;
    private int firmware_version;
    private int target_firmware_version;

}