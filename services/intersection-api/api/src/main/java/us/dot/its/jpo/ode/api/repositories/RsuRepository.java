package us.dot.its.jpo.ode.api.repositories;

import java.net.InetAddress;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;

@Repository
public interface RsuRepository extends JpaRepository<Rsu, Integer> {
    /**
     * Check if RSU exists in any of the given organizations using entity
     * relationships
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Rsu r " +
            "JOIN r.rsuOrganizations ro " +
            "JOIN ro.organization o " +
            "WHERE r.ipv4Address = :ipv4Address AND o.name IN :organizations")
    boolean existsByIpAndOrganizations(@Param("ipv4Address") InetAddress ipv4Address,
            @Param("organizations") List<String> organizations);

    Rsu findByIpv4Address(InetAddress ipv4Address);

    List<Rsu> findByIpv4AddressIn(List<InetAddress> ipv4Addresses);

    @Query("SELECT rsu " +
            "FROM Rsu rsu " +
            "JOIN rsu.rsuOrganizations ro " +
            "JOIN ro.organization o " +
            "WHERE o.name = :orgName " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(CAST(rsu.ipv4Address AS string)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(CAST(rsu.milepost AS string)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(rsu.primaryRoute) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(rsu.model.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(rsu.model.manufacturer.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(rsu.serialNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Rsu> findAllByOrganization(@Param("orgName") String orgName, @Param("search") String search,
            Pageable pageable);

    @Query("SELECT DISTINCT r.primaryRoute FROM Rsu r ORDER BY r.primaryRoute ASC")
    List<String> findAllPrimaryRoutes();

    @Query("SELECT m.name as manufacturer, rm.name as model " +
            "FROM RsuModel rm " +
            "JOIN rm.manufacturer m " +
            "ORDER BY m.name ASC, rm.name ASC")
    List<RsuModelProjection> findAllRsuModels();

    @Query("SELECT o.name " +
            "FROM Rsu r " +
            "JOIN r.rsuOrganizations ro " +
            "JOIN ro.organization o " +
            "WHERE r.ipv4Address = :ipv4Address " +
            "ORDER BY o.name ASC")
    List<String> findAllOrganizationNamesByIpv4Address(@Param("ipv4Address") InetAddress ipv4Address);

    @Query("SELECT r.ipv4Address " +
            "FROM Rsu r " +
            "JOIN r.rsuOrganizations ro " +
            "JOIN ro.organization o " +
            "WHERE o.name in :organizationNames")
    List<InetAddress> findAllowedRsuIpsInOrganizations(@Param("organizationNames") List<String> organizationNames);

    /**
     * Returns all RSUs belonging to the given organisation, fetching
     * model, manufacturer, and rsuOption.
     */
    @Query("SELECT DISTINCT rsu " +
            "FROM Rsu rsu " +
            "JOIN FETCH rsu.model m " +
            "JOIN FETCH m.manufacturer " +
            "LEFT JOIN FETCH rsu.rsuOption " +
            "JOIN rsu.rsuOrganizations ro " +
            "JOIN ro.organization o " +
            "WHERE o.name = :orgName")
    List<Rsu> findAllRsusByOrganizationName(@Param("orgName") String orgName);

    @Transactional
    void removeRsuByIpv4Address(InetAddress ipv4Address);

    @Modifying
    @Transactional
    @Query("DELETE FROM Rsu r WHERE r.ipv4Address IN :ipv4Addresses")
    void removeByIpv4AddressIn(@Param("ipv4Addresses") List<InetAddress> ipv4Addresses);

    interface RsuModelProjection {
        String getManufacturer();

        String getModel();
    }
}
