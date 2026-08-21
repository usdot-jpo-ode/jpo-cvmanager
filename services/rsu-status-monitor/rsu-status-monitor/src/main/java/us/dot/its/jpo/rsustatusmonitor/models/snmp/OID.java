package us.dot.its.jpo.rsustatusmonitor.models.snmp;

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
public class OID {

    private String name;
    private OID_TYPE type;
    private String oid;
}

enum OID_TYPE {
    SCALAR,
    NODE,
    TABLE,
    ROW
}
