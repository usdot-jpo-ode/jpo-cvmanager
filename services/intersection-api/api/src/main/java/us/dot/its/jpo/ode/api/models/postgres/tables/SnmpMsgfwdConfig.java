package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "snmp_msgfwd_config")
public class SnmpMsgfwdConfig {
    @EmbeddedId
    private SnmpMsgfwdConfigId id;

    @MapsId("rsuId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rsu_id", nullable = false)
    private Rsu rsu;

    @MapsId("msgfwdType")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "msgfwd_type", nullable = false)
    private SnmpMsgfwdType msgfwdType;

    @Size(max = 128)
    @NotNull
    @Column(name = "message_type", nullable = false, length = 128)
    private String messageType;

    @NotNull
    @Column(name = "dest_ipv4", nullable = false)
    private InetAddress destIpv4;

    @NotNull
    @Column(name = "dest_port", nullable = false)
    private Integer destPort;

    @NotNull
    @Column(name = "start_datetime", nullable = false)
    private Instant startDatetime;

    @NotNull
    @Column(name = "end_datetime", nullable = false)
    private Instant endDatetime;

    @NotNull
    @Column(name = "active", nullable = false, columnDefinition = "bit(1)")
    private Boolean active;

    @NotNull
    @Column(name = "security", nullable = false, columnDefinition = "bit(1)")
    private Boolean security;

}
