package com.trihydro.rsuinfobridge.models.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.time.Instant;

/**
 * Entity for snmp_msgfwd_config table.
 * Used for deleteAll operations in tests.
 */
@Getter
@Setter
@Entity
@Table(name = "snmp_msgfwd_config")
public class SnmpMsgfwdConfig {
    @EmbeddedId
    private SnmpMsgfwdConfigId id;

    @NotNull
    @Column(name = "rsu_id", nullable = false, insertable = false, updatable = false)
    private Integer rsuId;

    @NotNull
    @Column(name = "msgfwd_type", nullable = false, insertable = false, updatable = false)
    private Integer msgfwdType;

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

