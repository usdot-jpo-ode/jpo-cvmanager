package us.dot.its.jpo.ode.api.models.postgres.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import us.dot.its.jpo.ode.api.models.SimplePosition;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for {@link Rsu}
 */
@Value
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

    @JsonProperty("organizations")
    List<String> organizations;

    @JsonProperty("tim_deposit")
    Boolean timDeposit;

    @JsonProperty("snmp_monitoring")
    Boolean snmpMonitoring;
}