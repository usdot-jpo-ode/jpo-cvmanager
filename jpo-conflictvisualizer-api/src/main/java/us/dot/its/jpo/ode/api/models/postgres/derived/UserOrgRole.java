package us.dot.its.jpo.ode.api.models.postgres.derived;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@EqualsAndHashCode
@Getter
public class UserOrgRole {
    private String email;
    private String organization_name;
    private String role_name;
}
