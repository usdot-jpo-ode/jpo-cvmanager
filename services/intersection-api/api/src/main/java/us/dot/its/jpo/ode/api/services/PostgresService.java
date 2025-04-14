package us.dot.its.jpo.ode.api.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole;
import us.dot.its.jpo.ode.api.models.postgres.tables.Users;

@Service
public class PostgresService {

    @PersistenceContext
    private EntityManager entityManager;

    // TODO: Consider using "EXISTS" to improve queries
    private final String findUserOrgRolesQuery = "SELECT new us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole(u.email, o.name, r.name) "
            +
            "FROM Users u WHERE EXISTS (" +
            "SELECT 1 " +
            "FROM UserOrganization uo " +
            "JOIN Organizations o ON uo.organization_id = o.organization_id " +
            "JOIN Roles r ON uo.role_id = r.role_id " +
            "WHERE u.user_id = uo.user_id AND u.email = :email" +
            ")";

    // TODO: Consider using "EXISTS" to improve queries
    private final String findUserQuery = "SELECT u FROM Users u WHERE EXISTS (" +
            "SELECT 1 " +
            "FROM Users u2 " +
            "WHERE u2.email = :email AND u2.user_id = u.user_id)";

    // TODO: Consider using "EXISTS" to improve queries
    private final String findUserRsuIPQuery = "SELECT r.ipv4_address " +
            "FROM Rsus r " +
            "WHERE EXISTS (" +
            "SELECT 1 " +
            "FROM Users u " +
            "JOIN UserOrganization uo ON u.user_id = uo.user_id " +
            "JOIN RsuOrganization ro ON ro.organization_id = uo.organization_id " +
            "WHERE ro.rsu_id = r.rsu_id AND u.email = :email" +
            ")";

    // TODO: Consider using "EXISTS" to improve queries
    private final String findUserIntersectionQuery = "SELECT i.intersection_number " +
            "FROM Intersections i " +
            "WHERE EXISTS (" +
            "SELECT 1 " +
            "FROM Users u " +
            "JOIN UserOrganization uo ON u.user_id = uo.user_id " +
            "JOIN IntersectionOrganization io ON io.organization_id = uo.organization_id " +
            "WHERE io.intersection_id = i.intersection_id AND u.email = :email" +
            ")";

    // TODO: Consider using "EXISTS" to improve queries
    private final String findIntersectionsByOrganizationQuery = "SELECT i.intersection_number " +
            "FROM Intersections i " +
            "WHERE EXISTS (" +
            "SELECT 1 " +
            "FROM IntersectionOrganization io " +
            "JOIN Organizations o ON io.organization_id = o.organization_id " +
            "WHERE io.intersection_id = i.intersection_id AND o.name = :orgName" +
            ")";

    public List<UserOrgRole> findUserOrgRoles(String email) {
        TypedQuery<UserOrgRole> query = entityManager.createQuery(findUserOrgRolesQuery, UserOrgRole.class);
        query.setParameter("email", email);
        return query.getResultList();
    }

    public List<UserOrgRole> isUserRoleAboveRequired(String email, String organization, String requiredRole) {
        TypedQuery<UserOrgRole> query = entityManager.createQuery(findUserOrgRolesQuery, UserOrgRole.class);
        query.setParameter("email", email);
        return query.getResultList();
    }

    public Users findUser(String email) {
        TypedQuery<Users> query = entityManager.createQuery(findUserQuery, Users.class).setMaxResults(1);
        query.setParameter("email", email);
        List<Users> results = query.getResultList();
        if (!results.isEmpty()) {
            return results.getFirst();
        } else {
            return null;
        }
    }

    public String getUserRoleInOrg(String email, String organization) {
        String queryString = "SELECT r.name " +
                "FROM Roles r " +
                "WHERE EXISTS (" +
                "SELECT 1 " +
                "FROM Users u " +
                "JOIN UserOrganization uo ON u.user_id = uo.user_id " +
                "JOIN Organizations o ON uo.organization_id = o.organization_id " +
                "WHERE u.email = :email AND o.name = :organization AND uo.role_id = r.role_id" +
                ")";
        TypedQuery<String> query = entityManager.createQuery(queryString, String.class);
        query.setParameter("email", email);
        query.setParameter("organization", organization);
        List<String> results = query.getResultList();
        if (!results.isEmpty()) {
            return results.get(0);
        } else {
            return null;
        }
    }

    public List<String> getAllowedRsuIpByEmail(String email) {
        TypedQuery<String> query = entityManager.createQuery(findUserRsuIPQuery, String.class);
        query.setParameter("email", email);
        return query.getResultList();
    }

    public List<Integer> getAllowedIntersectionIdsByEmail(String email) {
        TypedQuery<String> query = entityManager.createQuery(findUserIntersectionQuery, String.class);
        query.setParameter("email", email);
        return query.getResultList().stream().map(Integer::valueOf).collect(Collectors.toList());
    }

    public List<Integer> getAllowedIntersectionIdsByOrganization(String organization) {
        TypedQuery<String> query = entityManager.createQuery(findIntersectionsByOrganizationQuery, String.class);
        query.setParameter("orgName", organization);
        return query.getResultList().stream().map(Integer::valueOf).collect(Collectors.toList());
    }

    public boolean checkRsuWithOrg(String rsuIp, List<String> organizations) {
        if (organizations.isEmpty()) {
            return false;
        }
        String queryString = "SELECT rsu.ipv4_address::text AS ipv4_address " +
                "FROM Rsus rsu " +
                "WHERE EXISTS (" +
                "SELECT 1 " +
                "FROM RsuOrganization rsu_org " +
                "JOIN Organizations org ON org.organization_id = rsu_org.organization_id " +
                "WHERE rsu_org.rsu_id = rsu.rsu_id " +
                "AND org.name IN (:allowedOrgs)) " +
                "AND rsu.ipv4_address = :rsuIp";

        TypedQuery<String> query = entityManager.createQuery(queryString, String.class);
        query.setParameter("allowedOrgs", organizations);
        query.setParameter("rsuIp", rsuIp);
        query.setMaxResults(1);

        List<String> result = query.getResultList();
        return !result.isEmpty() && result.get(0).equals(rsuIp);
    }

    public boolean checkIntersectionWithOrg(String intersectionId, List<String> organizations) {
        if (organizations.isEmpty()) {
            return false;
        }

        String queryString = "SELECT i.intersection_number " +
                "FROM Intersections i " +
                "WHERE EXISTS (" +
                "SELECT 1 " +
                "FROM IntersectionOrganization io " +
                "JOIN Organizations org ON org.organization_id = io.organization_id " +
                "WHERE io.intersection_id = i.intersection_id " +
                "AND org.name IN (:allowedOrgs)) " +
                "AND i.intersection_number = :intersectionId";

        TypedQuery<String> query = entityManager.createQuery(queryString, String.class);
        query.setParameter("allowedOrgs", organizations);
        query.setParameter("intersectionId", intersectionId);
        query.setMaxResults(1);

        List<String> result = query.getResultList();
        return !result.isEmpty() && result.get(0).equals(intersectionId);
    }

    public List<String> getQualifiedOrgList(String email, String requiredRole) {
        List<UserOrgRole> organizationRoles = findUserOrgRoles(email);
        return organizationRoles.stream()
                .filter(entry -> PermissionService.checkRoleAbove(entry.getRole_name(), requiredRole))
                .map(val -> val.getOrganization_name())
                .collect(Collectors.toList());
    }

    public List<String> getQualifiedOrgList(List<UserOrgRole> organizationRoles, String requiredRole) {
        return organizationRoles.stream()
                .filter(entry -> PermissionService.checkRoleAbove(entry.getRole_name(), requiredRole))
                .map(val -> val.getOrganization_name())
                .collect(Collectors.toList());
    }
}
