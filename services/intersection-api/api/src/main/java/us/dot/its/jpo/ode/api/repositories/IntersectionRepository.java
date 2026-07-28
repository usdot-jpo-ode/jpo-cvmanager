package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import us.dot.its.jpo.ode.api.models.postgres.tables.Intersection;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntersectionRepository extends JpaRepository<Intersection, Integer> {

    /**
     * Finds a single intersection by its intersection_number.
     */
    Optional<Intersection> findByIntersectionNumber(String intersectionNumber);

    /**
     * Fetches a single intersection with its organization associations eagerly loaded.
     * Avoids N+1 queries when reading org names.
     */
    @Query("SELECT DISTINCT i FROM Intersection i " +
            "LEFT JOIN FETCH i.intersectionOrganizations io " +
            "LEFT JOIN FETCH io.organization " +
            "WHERE i.intersectionNumber = :intersectionNumber")
    Optional<Intersection> findByIntersectionNumberWithOrgs(
            @Param("intersectionNumber") Integer intersectionNumber);

    /**
     * Fetches all intersections with their organization associations eagerly loaded.
     */
    @Query("SELECT DISTINCT i FROM Intersection i " +
            "LEFT JOIN FETCH i.intersectionOrganizations io " +
            "LEFT JOIN FETCH io.organization")
    List<Intersection> findAllWithOrgs();

    /**
     * Fetches intersections belonging to a single organization, with org associations loaded.
     */
    @Query("SELECT DISTINCT i FROM Intersection i " +
            "LEFT JOIN FETCH i.intersectionOrganizations io " +
            "LEFT JOIN FETCH io.organization o " +
            "WHERE o.name = :orgName")
    List<Intersection> findAllByOrgNameWithOrgs(@Param("orgName") String orgName);

    /**
     * Fetches intersections belonging to any of the given organizations, with org associations loaded.
     */
    @Query("SELECT DISTINCT i FROM Intersection i " +
            "LEFT JOIN FETCH i.intersectionOrganizations io " +
            "LEFT JOIN FETCH io.organization o " +
            "WHERE o.name IN :orgNames")
    List<Intersection> findAllByOrgNamesWithOrgs(@Param("orgNames") List<String> orgNames);

    @Query("SELECT i.intersectionNumber " +
            "FROM Intersection i " +
            "JOIN i.intersectionOrganizations io " +
            "JOIN io.organization.userOrganizations uo " +
            "JOIN uo.user u " +
            "WHERE u.email = :email")
    List<String> findAllowedIntersectionIdsByEmail(@Param("email") String email);

    @Query("SELECT i.intersectionNumber " +
            "FROM Intersection i " +
            "JOIN i.intersectionOrganizations io " +
            "JOIN io.organization o " +
            "WHERE o.name = :orgName")
    List<String> findIntersectionsByOrganization(@Param("orgName") String orgName);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
            "FROM Intersection i " +
            "JOIN i.intersectionOrganizations io " +
            "JOIN io.organization o " +
            "WHERE i.intersectionNumber = :intersectionId AND o.name IN :organizations")
    boolean existsByIdAndOrganizations(@Param("intersectionId") String intersectionId,
            @Param("organizations") List<String> organizations);
}