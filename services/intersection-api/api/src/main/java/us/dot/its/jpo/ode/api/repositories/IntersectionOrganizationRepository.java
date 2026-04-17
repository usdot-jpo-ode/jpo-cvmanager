package us.dot.its.jpo.ode.api.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import us.dot.its.jpo.ode.api.models.postgres.tables.Intersection;
import us.dot.its.jpo.ode.api.models.postgres.tables.IntersectionOrganization;

import java.util.List;

@Repository
public interface IntersectionOrganizationRepository extends JpaRepository<IntersectionOrganization, Integer> {

    @Modifying
    @Transactional
    void deleteIntersectionOrganizationByIntersection_IntersectionNumber(String intersectionNumber);

    @Modifying
    @Transactional
    @Query("DELETE FROM IntersectionOrganization io WHERE io.intersection.intersectionNumber = :intersectionNumber AND io.organization.name IN :orgNames")
    void deleteByIntersectionNumberAndOrganizationNameIn(@Param("intersectionNumber") String intersectionNumber,
            @Param("orgNames") List<String> orgNames);

    @Query("SELECT DISTINCT i FROM Intersection i " +
            "LEFT JOIN FETCH i.intersectionOrganizations io " +
            "LEFT JOIN FETCH io.organization " +
            "WHERE NOT EXISTS " +
            "(SELECT 1 FROM IntersectionOrganization io2 " +
            "WHERE io2.intersection.id = i.id AND io2.organization.name = :organizationName)")
    List<Intersection> findAllIntersectionsNotInOrganizationName(
            @Param("organizationName") String organizationName);
}
