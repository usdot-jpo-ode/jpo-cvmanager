package us.dot.its.jpo.ode.api.models.devices.management;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RsuUpgradeRequest {

    @NotEmpty
    @JsonProperty("rsu_ips")
    private List<String> rsuIps;
}
