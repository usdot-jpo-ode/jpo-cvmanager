package us.dot.its.jpo.ode.api.models.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import us.dot.its.jpo.ode.api.models.postgres.tables.User;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for {@link User}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto implements Serializable {
    @Size(max = 128)
    @Email
    @NotNull
    private String email;

    @JsonProperty("first_name")
    @Size(max = 128)
    private String firstName;

    @JsonProperty("last_name")
    @Size(max = 128)
    private String lastName;

    @JsonProperty("super_user")
    @NotNull
    private Boolean superUser;

    @JsonProperty("organizations")
    @Size(min = 1)
    @NotNull
    List<UserOrganizationDto> organizations;
}