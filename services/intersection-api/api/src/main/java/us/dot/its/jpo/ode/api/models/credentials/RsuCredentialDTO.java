package us.dot.its.jpo.ode.api.models.credentials;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RsuCredentialDTO {

    private Integer id;

    private String nickname;

    private String username;

    private String password;

    private Integer ownerOrganizationId;
}
