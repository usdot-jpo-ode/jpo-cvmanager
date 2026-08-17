package us.dot.its.jpo.ode.api.models.devices.management;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RsuSingleUpgradeCheckRequest {

    @NotBlank
    @JsonProperty("rsu_ip")
    private String rsuIp;
}