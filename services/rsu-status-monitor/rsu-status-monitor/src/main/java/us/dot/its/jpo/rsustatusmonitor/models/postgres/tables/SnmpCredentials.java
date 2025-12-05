package us.dot.its.jpo.rsustatusmonitor.models.postgres.tables;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@EqualsAndHashCode
@Getter
@Entity
@Table(name = "snmp_credentials")
public class SnmpCredentials {
    @Id
    private int snmp_credential_id;
    private String username;
    private String password;
    private String encrypt_password;
    private String nickname;

}