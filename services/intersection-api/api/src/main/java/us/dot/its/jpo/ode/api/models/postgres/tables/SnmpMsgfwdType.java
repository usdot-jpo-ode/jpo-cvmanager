package us.dot.its.jpo.ode.api.models.postgres.tables;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "snmp_msgfwd_type")
public class SnmpMsgfwdType {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "snmp_msgfwd_type_id_gen")
    @SequenceGenerator(name = "snmp_msgfwd_type_id_gen", sequenceName = "snmp_msgfwd_type_id_seq", allocationSize = 1)
    @Column(name = "snmp_msgfwd_type_id", nullable = false)
    private Integer id;

    @Size(max = 128)
    @NotNull
    @Column(name = "name", nullable = false, length = 128)
    private String name;


}