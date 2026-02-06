package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "snmp_protocols")
public class SnmpProtocol {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "snmp_protocols_id_gen")
    @SequenceGenerator(name = "snmp_protocols_id_gen", sequenceName = "snmp_protocols_snmp_protocol_id_seq", allocationSize = 1)
    @Column(name = "snmp_protocol_id", nullable = false)
    private Integer id;

    @Size(max = 128)
    @NotNull
    @Column(name = "protocol_code", nullable = false, length = 128)
    private String protocolCode;

    @Size(max = 128)
    @NotNull
    @Column(name = "nickname", nullable = false, length = 128)
    private String nickname;


}