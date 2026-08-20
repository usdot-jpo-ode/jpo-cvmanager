package us.dot.its.jpo.rsustatusmonitor.models.postgres.derived;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@EqualsAndHashCode
@Getter
@AllArgsConstructor
public class RsuData {
    private int rsu_id;
    private String ipv4_address;
    private String intersection_id;
}