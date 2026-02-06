package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;

import java.util.List;

@Repository
public interface RsuRepository extends JpaRepository<Rsu, Integer> {
    /**
     * Check if RSU exists in any of the given organizations using entity relationships
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM Rsu r " +
           "JOIN r.rsuOrganizations ro " +
           "JOIN ro.organization o " +
           "WHERE r.ipv4Address = :rsuIp AND o.name IN :organizations")
    boolean existsByIpAndOrganizations(@Param("rsuIp") String rsuIp, @Param("organizations") List<String> organizations);
}
