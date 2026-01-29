package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import us.dot.its.jpo.ode.api.models.postgres.tables.Role;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query("SELECT uo.role.name " +
            "FROM UserOrganization uo " +
            "WHERE uo.user.email = :email AND uo.organization.name = :organization")
    Optional<String> findUserRoleInOrg(@Param("email") String email, @Param("organization") String organization);
}