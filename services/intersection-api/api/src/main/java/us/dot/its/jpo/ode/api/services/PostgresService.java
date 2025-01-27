package us.dot.its.jpo.ode.api.services;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole;
import us.dot.its.jpo.ode.api.models.postgres.tables.User;

@Service
public class PostgresService {

    @PersistenceContext
    private EntityManager entityManager;

    // TODO: Consider using "EXISTS" to improve queries
    private final String findUserOrgRolesQuery = "SELECT new us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole(u.email, o.name, r.name) "
            +
            "FROM Users u JOIN UserOrganization uo on u.user_id = uo.user_id " +
            "JOIN Organizations o on uo.organization_id = o.organization_id " +
            "JOIN Roles r on uo.role_id = r.role_id " +
            "where u.email = :email";

    // TODO: Consider using "EXISTS" to improve queries
    private final String findUserQuery = "SELECT u from Users u where u.email = :email";

    // TODO: Consider using "EXISTS" to improve queries
    private final String findUserRsuIPQuery = "select r.ipv4_address " +
            "FROM Users u JOIN UserOrganization uo on u.user_id = uo.user_id " +
            "JOIN RsuOrganization ro on ro.organization_id = uo.organization_id " +
            "JOIN Rsus r on r.rsu_id = ro.rsu_id " +
            "where u.email = :email";

    // TODO: Consider using "EXISTS" to improve queries
    private final String findUserIntersectionQuery = "select io.intersection_id " +
            "FROM Users u JOIN UserOrganization uo on u.user_id = uo.user_id " +
            "JOIN IntersectionOrganization io on io.organization_id = uo.organization_id " +
            "JOIN Intersections i on i.intersection_id = io.intersection_id " +
            "where u.email = :email";

    public List<UserOrgRole> findUserOrgRoles(String email) {
        TypedQuery<UserOrgRole> query = entityManager.createQuery(findUserOrgRolesQuery, UserOrgRole.class);
        query.setParameter("email", email);
        return query.getResultList();
    }

    public User findUser(String email) {
        TypedQuery<User> query = entityManager.createQuery(findUserQuery, User.class).setMaxResults(1);
        query.setParameter("email", email);
        List<User> results = query.getResultList();
        if (!results.isEmpty()) {
            return results.getFirst();
        }
        else {
            return null;
        }
    }

    public List<String> getAllowedRsuIpByEmail(String email) {
        TypedQuery<String> query = entityManager.createQuery(findUserRsuIPQuery, String.class);
        query.setParameter("email", email);
        return query.getResultList();
    }

    public List<Integer> getAllowedIntersectionIdsByEmail(String email) {
        TypedQuery<Integer> query = entityManager.createQuery(findUserIntersectionQuery, Integer.class);
        query.setParameter("email", email);
        return query.getResultList();
    }
}
