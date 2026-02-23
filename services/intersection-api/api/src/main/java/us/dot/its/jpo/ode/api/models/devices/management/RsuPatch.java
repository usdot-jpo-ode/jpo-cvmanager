package us.dot.its.jpo.ode.api.models.devices.management;

import lombok.Getter;
import lombok.Setter;
import us.dot.its.jpo.ode.api.models.SimplePosition;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RsuPatch {
    @JsonProperty("ip")
    String ipv4Address;

    @JsonProperty("geo_position")
    SimplePosition geoPosition;

    @JsonProperty("milepost")
    Double milepost;

    @JsonProperty("primary_route")
    @Size(max = 128)
    String primaryRoute;

    @JsonProperty("serial_number")
    @Size(max = 128)
    String serialNumber;

    @JsonProperty("model")
    String model;

    @JsonProperty("scms_id")
    @Size(max = 128)
    String issScmsId;

    @JsonProperty("ssh_credential_group")
    String sshCredentialGroup;

    @JsonProperty("snmp_credential_group")
    String snmpCredentialGroup;

    @JsonProperty("snmp_version_group")
    String snmpVersionGroup;

    @JsonProperty("organizations_to_add")
    List<String> organizationsToAdd;

    @JsonProperty("organizations_to_remove")
    List<String> organizationsToRemove;

    @JsonProperty("tim_deposit")
    Boolean timDeposit;

    @JsonProperty("snmp_monitoring")
    Boolean snmpMonitoring;
}
