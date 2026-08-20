package us.dot.its.jpo.ode.api.models.users;

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
public class ModifyUserAllowedSelections {
    @JsonProperty("roles")
    List<String> roles;
    @JsonProperty("organizations")
    List<String> organizations;
}
