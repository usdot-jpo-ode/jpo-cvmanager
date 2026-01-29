package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import us.dot.its.jpo.ode.api.models.postgres.tables.Intersection;

import java.util.List;

@Repository
public interface IntersectionRepository extends JpaRepository<Intersection, Integer> {

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