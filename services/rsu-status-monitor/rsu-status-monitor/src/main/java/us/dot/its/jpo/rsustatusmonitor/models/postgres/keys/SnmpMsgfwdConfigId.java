package us.dot.its.jpo.rsustatusmonitor.models.postgres.keys;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SnmpMsgfwdConfigId implements Serializable {
    // Composite primary key elements for the snmp_msgfwd_config table
    private int rsu_id;
    private int msgfwd_type;
    private int snmp_index;
}
