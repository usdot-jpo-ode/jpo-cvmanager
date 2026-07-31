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
public class UserPatch {
    @JsonProperty("email")
    String email;

    @JsonProperty("first_name")
    String firstName;

    @JsonProperty("last_name")
    String lastName;

    @JsonProperty("super_user")
    Boolean superUser;

    @JsonProperty("organizations_to_add")
    List<UserOrganizationDto> organizationsToAdd;

    @JsonProperty("organizations_to_remove")
    List<UserOrganizationDto> organizationsToRemove;

    @JsonProperty("organizations_to_modify")
    List<UserOrganizationDto> organizationsToModify;
}
