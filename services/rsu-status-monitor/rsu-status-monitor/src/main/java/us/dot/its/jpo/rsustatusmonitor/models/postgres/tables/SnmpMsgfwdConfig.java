package us.dot.its.jpo.rsustatusmonitor.models.postgres.tables;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import us.dot.its.jpo.rsustatusmonitor.models.postgres.keys.SnmpMsgfwdConfigId;

@ToString
@Setter
@EqualsAndHashCode
@Getter
@Entity
@IdClass(SnmpMsgfwdConfigId.class)
@Table(name = "snmp_msgfwd_config")
public class SnmpMsgfwdConfig {
    @Id
    private int rsu_id;
    @Id
    private int msgfwd_type;
    @Id
    private int snmp_index;
    private String message_type;
    private String dest_ipv4;
    private int dest_port;
    private LocalDateTime start_datetime;
    private LocalDateTime end_datetime;
    private boolean active;
    private boolean security;
}