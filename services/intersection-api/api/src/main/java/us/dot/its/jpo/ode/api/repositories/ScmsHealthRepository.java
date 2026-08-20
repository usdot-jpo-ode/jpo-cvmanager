package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;
import us.dot.its.jpo.ode.api.models.postgres.projections.ScmsHealthRsuProjection;
import us.dot.its.jpo.ode.api.models.postgres.tables.ScmsHealth;

import java.net.InetAddress;
import java.util.List;

@Repository
public interface ScmsHealthRepository extends JpaRepository<ScmsHealth, Integer> {

    @Modifying
    @Transactional
    @Query("DELETE FROM ScmsHealth ro WHERE ro.rsu.ipv4Address = :ipv4Address")
    void removeScmsHealthByIpv4Address(@Param("ipv4Address") InetAddress ipv4Address);

    @Modifying
    @Transactional
    @Query("DELETE FROM ScmsHealth ro WHERE ro.rsu.ipv4Address IN :ipv4Addresses")
    void removeMultipleScmsHealthByIpv4Address(@Param("ipv4Addresses") List<InetAddress> ipv4Addresses);

    /**
     * Retrieves the latest SCMS health record for each RSU within a specific organization.
     * <p>
     * This query is functionally equivalent to the legacy Python implementation which uses:
     * {@code ROW_NUMBER() OVER (PARTITION BY sh.rsu_id ORDER BY sh.timestamp DESC)}
     * <p>
     * It achieves parity by:
     * <ul>
     *     <li>Using a <b>LEFT JOIN</b> to ensure all RSUs in the organization are returned, even those without health
     *     records (matching Python's LEFT JOIN).</li>
     *     <li>Using a <b>subquery with ROW_NUMBER()</b> window function to efficiently select only the most recent
     *     health record per RSU (single pass, no correlated subquery).</li>
     *     <li>Filtering by the <b>organization name</b> and sorting by <b>IPv4 address</b>.</li>
     * </ul>
     * <p>
     * Note: Uses native SQL for PostgreSQL-specific ROW_NUMBER() window function for performance.
     *
     * @param organization The name of the organization to filter by.
     * @return A list of projections containing RSU and their latest SCMS health data.
     */
    @Query(value = """
            SELECT rd.ipv4_address, sh.health, sh.expiration
            FROM rsus rd
            JOIN rsu_organization ro ON ro.rsu_id = rd.rsu_id
            JOIN organizations o ON o.organization_id = ro.organization_id
            LEFT JOIN (
                SELECT rsu_id, health, expiration
                FROM (
                    SELECT rsu_id, health, expiration,
                           ROW_NUMBER() OVER (PARTITION BY rsu_id ORDER BY timestamp DESC) AS row_num
                    FROM scms_health
                ) ranked
                WHERE row_num = 1
            ) sh ON sh.rsu_id = rd.rsu_id
            WHERE o.name = :organization
            ORDER BY rd.ipv4_address
            """, nativeQuery = true)
    @Transactional(readOnly = true)
    List<ScmsHealthRsuProjection> findLatestScmsHealthByOrganization(@Param("organization") String organization);
}
