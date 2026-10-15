package us.dot.its.jpo.ode.api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import us.dot.its.jpo.ode.api.models.postgres.tables.Manufacturer;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, Integer> {
    /**
     * Finds a manufacturer by name ignoring case, preferring the oldest row when
     * names differ only by case. Matches the OBU OTA server's manufacturer lookup.
     */
    Optional<Manufacturer> findFirstByNameIgnoreCaseOrderByIdAsc(String name);
}
