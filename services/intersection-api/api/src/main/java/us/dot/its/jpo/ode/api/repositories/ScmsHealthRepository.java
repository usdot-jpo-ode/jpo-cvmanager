package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
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
}
