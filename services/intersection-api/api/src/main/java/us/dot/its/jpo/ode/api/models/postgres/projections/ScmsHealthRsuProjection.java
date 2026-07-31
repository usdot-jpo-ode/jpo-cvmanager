package us.dot.its.jpo.ode.api.models.postgres.projections;

import java.net.InetAddress;
import java.time.Instant;

/**
 * Interface-based projection for SCMS health queries.
 * Spring Data JPA automatically maps column names to getter methods for native queries.
 */
public interface ScmsHealthRsuProjection {

    /**
     * @return The RSU IPv4 address (column: ipv4_address)
     */
    InetAddress getIpv4Address();

    /**
     * @return The health status (column: health).
     *         Returns true if healthy, false otherwise.
     */
    Boolean getHealth();

    /**
     * @return The certificate expiration timestamp (column: expiration)
     */
    Instant getExpiration();
}