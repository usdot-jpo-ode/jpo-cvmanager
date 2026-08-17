package us.dot.its.jpo.ode.api.models.devices.management;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModifyRsuAllowedSelections {
    @JsonProperty("primary_routes")
    List<String> primaryRoutes;
    @JsonProperty("rsu_models")
    List<String> rsuModels;
    @JsonProperty("ssh_credential_groups")
    List<String> sshCredentialGroups;
    @JsonProperty("snmp_credential_groups")
    List<String> snmpCredentialGroups;
    @JsonProperty("snmp_version_groups")
    List<String> snmpVersionGroups;
    @JsonProperty("organizations")
    List<String> organizations;
}
