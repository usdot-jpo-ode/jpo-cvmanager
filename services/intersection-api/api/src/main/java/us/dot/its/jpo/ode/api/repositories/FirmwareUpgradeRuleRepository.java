package us.dot.its.jpo.ode.api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUpgradeRule;

@Repository
public interface FirmwareUpgradeRuleRepository extends JpaRepository<FirmwareUpgradeRule, Integer> {
    Optional<FirmwareUpgradeRule> findFirstByFrom_Id(Integer fromId);
}
