package us.dot.its.jpo.ode.api.models.postgres.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import us.dot.its.jpo.ode.api.models.SimplePosition;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for {@link Rsu}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RsuInfoDto implements Serializable {
    @JsonProperty("ip")
    @NotNull
    String ipv4Address;

    @JsonProperty("geo_position")
    @NotNull
    SimplePosition geoPosition;

    @JsonProperty("milepost")
    @NotNull
    Double milepost;

    @JsonProperty("primary_route")
    @NotNull
    @Size(max = 128)
    String primaryRoute;

    @JsonProperty("serial_number")
    @NotNull
    @Size(max = 128)
    String serialNumber;

    @JsonProperty("scms_id")
    @NotNull
    @Size(max = 128)
    String issScmsId;

    @JsonProperty("model")
    String model;

    @JsonProperty("ssh_credential_group")
    String sshCredentialGroup;

    @JsonProperty("snmp_credential_group")
    String snmpCredentialGroup;

    @JsonProperty("snmp_version_group")
    String snmpVersionGroup;

    @NotNull
    @Size(min = 1)
    @JsonProperty("organizations")
    List<String> organizations;

    @NotNull
    @JsonProperty("tim_deposit")
    Boolean timDeposit;

    @NotNull
    @JsonProperty("snmp_monitoring")
    Boolean snmpMonitoring;
}