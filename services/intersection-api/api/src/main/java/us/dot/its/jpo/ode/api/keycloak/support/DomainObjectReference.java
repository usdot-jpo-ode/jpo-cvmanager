package us.dot.its.jpo.ode.api.keycloak.support;

import lombok.Data;

/**
 * Defines a single domain object by a type and name to look up
 */
@Data
public class DomainObjectReference {

    private final String type;

    private final String id;
}