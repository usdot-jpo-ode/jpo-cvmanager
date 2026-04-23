package us.dot.its.jpo.ode.api.models.postgres.projections;

import lombok.RequiredArgsConstructor;

import java.net.InetAddress;
import java.time.Instant;

/**
 * Test implementation of ScmsHealthRsuProjection for unit tests.
 * In production, Spring Data JPA creates proxy implementations for native query results.
 */
@RequiredArgsConstructor
public class ScmsHealthRsuProjectionImpl implements ScmsHealthRsuProjection {

    private final InetAddress ipv4Address;
    private final Boolean health;
    private final Instant expiration;

    @Override
    public InetAddress getIpv4Address() {
        return ipv4Address;
    }

    @Override
    public Boolean getHealth() {
        return health;
    }

    @Override
    public Instant getExpiration() {
        return expiration;
    }
}

