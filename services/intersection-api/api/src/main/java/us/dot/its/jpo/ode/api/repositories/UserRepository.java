package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import us.dot.its.jpo.ode.api.models.postgres.tables.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u.email as email, o.name as organizationName, r.name as roleName " +
            "FROM User u " +
            "JOIN u.userOrganizations uo " +
            "JOIN uo.organization o " +
            "JOIN uo.role r " +
            "WHERE u.email = :email")
    List<UserOrgRoleProjection> findUserOrgRoles(@Param("email") String email);

    User findByEmail(@Param("email") String email);

    interface UserOrgRoleProjection {
        String getEmail();

        String getOrganizationName();

        String getRoleName();
    }
}