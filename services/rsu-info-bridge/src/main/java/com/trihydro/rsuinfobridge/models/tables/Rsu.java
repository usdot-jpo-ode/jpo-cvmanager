package com.trihydro.rsuinfobridge.models.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.InetAddress;

import org.locationtech.jts.geom.Point;

@Getter
@Setter
@Entity
@Table(name = "rsus")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rsu {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rsus_id_gen")
    @SequenceGenerator(name = "rsus_id_gen", sequenceName = "rsus_rsu_id_seq", allocationSize = 1)
    @Column(name = "rsu_id", nullable = false)
    private Integer id;

    @Column(name = "geography", columnDefinition = "geography not null")
    private Point geography;

    @NotNull
    @Column(name = "milepost", nullable = false)
    private Double milepost;

    @NotNull
    @Column(name = "ipv4_address", nullable = false)
    private InetAddress ipv4Address;

    @Size(max = 128)
    @NotNull
    @Column(name = "serial_number", nullable = false, length = 128)
    private String serialNumber;

    @Size(max = 128)
    @NotNull
    @Column(name = "iss_scms_id", nullable = false, length = 128)
    private String issScmsId;

    @Size(max = 128)
    @NotNull
    @Column(name = "primary_route", nullable = false, length = 128)
    private String primaryRoute;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model", nullable = false)
    private RsuModel model;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credential_id", nullable = false)
    private RsuCredential credential;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "snmp_credential_id", nullable = false)
    private SnmpCredential snmpCredential;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "snmp_protocol_id", nullable = false)
    private SnmpProtocol snmpProtocol;

    @OneToOne(mappedBy = "rsu")
    private RsuOption rsuOption;
}