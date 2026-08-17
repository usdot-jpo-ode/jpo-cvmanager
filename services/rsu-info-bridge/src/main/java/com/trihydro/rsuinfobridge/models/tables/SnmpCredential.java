package com.trihydro.rsuinfobridge.models.tables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "snmp_credentials")
public class SnmpCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "snmp_credentials_id_gen")
    @SequenceGenerator(name = "snmp_credentials_id_gen", sequenceName = "snmp_credentials_snmp_credential_id_seq", allocationSize = 1)
    @Column(name = "snmp_credential_id", nullable = false)
    private Integer id;

    @Size(max = 128)
    @NotNull
    @Column(name = "username", nullable = false, length = 128)
    private String username;

    @Size(max = 128)
    @NotNull
    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Size(max = 128)
    @Column(name = "encrypt_password", length = 128)
    private String encryptPassword;

    @Size(max = 128)
    @NotNull
    @Column(name = "nickname", nullable = false, length = 128)
    private String nickname;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_organization_id", nullable = false)
    private Organization ownerOrganization;

}