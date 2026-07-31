package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import us.dot.its.jpo.ode.api.models.postgres.tables.Organization;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {

    Optional<Organization> findByName(String name);

    List<Organization> findByNameIn(List<String> names);

    @Query("SELECT o.name FROM Organization o ORDER BY o.name ASC")
    List<String> findAllOrganizationNames();
}
