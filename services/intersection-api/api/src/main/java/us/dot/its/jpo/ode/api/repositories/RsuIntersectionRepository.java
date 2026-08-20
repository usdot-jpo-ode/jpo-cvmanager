package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import us.dot.its.jpo.ode.api.models.postgres.tables.Intersection;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuIntersection;

import java.net.InetAddress;
import java.util.List;

@Repository
public interface RsuIntersectionRepository extends JpaRepository<RsuIntersection, Integer> {

    /**
     * Projection for fetching RSU IPs alongside the intersection number they belong to.
     */
    interface IntersectionRsuProjection {
        Integer getIntersectionNumber();

        InetAddress getRsuIp();
    }

    /**
     * Fetches RSU IPs for a batch of intersection numbers in one query.
     * Avoids N+1 RSU IP lookups.
     */
    @Query("SELECT ri.intersection.intersectionNumber AS intersectionNumber, " +
            "ri.rsu.ipv4Address AS rsuIp " +
            "FROM RsuIntersection ri " +
            "WHERE ri.intersection.intersectionNumber IN :intersectionNumbers")
    List<IntersectionRsuProjection> findRsuIpsByIntersectionNumbers(
            @Param("intersectionNumbers") List<String> intersectionNumbers);

    /**
     * Fetches RSU IPs for a single intersection number.
     */
    @Query("SELECT ri.rsu.ipv4Address FROM RsuIntersection ri " +
            "WHERE ri.intersection.intersectionNumber = :intersectionNumber")
    List<InetAddress> findRsuIpsByIntersectionNumber(
            @Param("intersectionNumber") Integer intersectionNumber);



    boolean existsByRsuAndIntersection(Rsu rsu, Intersection intersection);

    @Modifying
    @Transactional
    @Query("DELETE FROM RsuIntersection ri WHERE ri.rsu.ipv4Address = :ipv4Address")
    void removeRsuIntersectionByIpv4Address(@Param("ipv4Address") InetAddress ipv4Address);

    @Modifying
    @Transactional
    @Query("DELETE FROM RsuIntersection ri WHERE ri.rsu.ipv4Address IN :ipv4Addresses")
    void removeMultipleRsuIntersectionsByIpv4Address(@Param("ipv4Addresses") List<InetAddress> ipv4Addresses);

    @Modifying
    @Transactional
    void deleteByIntersection_IntersectionNumber(String intersectionNumber);

    @Modifying
    @Transactional
    @Query("DELETE FROM RsuIntersection ri WHERE ri.intersection.intersectionNumber = :intersectionNumber AND ri.rsu.ipv4Address IN :ipv4Addresses")
    void deleteByIntersectionNumberAndRsuIpv4AddressIn(@Param("intersectionNumber") String intersectionNumber,
            @Param("ipv4Addresses") List<InetAddress> ipv4Addresses);
}
