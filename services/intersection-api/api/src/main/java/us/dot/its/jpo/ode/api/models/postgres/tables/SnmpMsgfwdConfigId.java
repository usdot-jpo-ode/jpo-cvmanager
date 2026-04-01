package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class SnmpMsgfwdConfigId implements Serializable {
    private static final long serialVersionUID = 228342654123605344L;
    @NotNull
    @Column(name = "rsu_id", nullable = false)
    private Integer rsuId;

    @NotNull
    @Column(name = "msgfwd_type", nullable = false)
    private Integer msgfwdType;

    @NotNull
    @Column(name = "snmp_index", nullable = false)
    private Integer snmpIndex;


}