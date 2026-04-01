package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import us.dot.its.jpo.ode.api.models.postgres.tables.RsuOption;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;

@Repository
public interface RsuOptionRepository extends JpaRepository<RsuOption, Integer> {
    Optional<RsuOption> findByRsuId(Integer rsuId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RsuOption ro WHERE ro.rsu.ipv4Address = :ipv4Address")
    void removeRsuOptionByIpv4Address(@Param("ipv4Address") InetAddress ipv4Address);

    @Modifying
    @Transactional
    @Query("DELETE FROM RsuOption ro WHERE ro.rsu.ipv4Address IN :ipv4Addresses")
    void removeMultipleRsuOptionsByIpv4Address(@Param("ipv4Addresses") List<InetAddress> ipv4Addresses);
}

