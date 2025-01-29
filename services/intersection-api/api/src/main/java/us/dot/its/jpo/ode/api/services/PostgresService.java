package us.dot.its.jpo.ode.api.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.ForbiddenException;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole;
import us.dot.its.jpo.ode.api.models.postgres.tables.Organizations;
import us.dot.its.jpo.ode.api.models.postgres.tables.Roles;
import us.dot.its.jpo.ode.api.models.postgres.tables.Users;

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
    private final String findUserIntersectionQuery = "select i.intersection_number " +
            "FROM Users u JOIN UserOrganization uo on u.user_id = uo.user_id " +
            "JOIN IntersectionOrganization io on io.organization_id = uo.organization_id " +
            "JOIN Intersections i on i.intersection_id = io.intersection_id " +
            "where u.email = :email";

    // TODO: Consider using "EXISTS" to improve queries
    private final String findIntersectionsByOrganizationQuery = "select i.intersection_number " +
            "FROM Organizations o JOIN IntersectionOrganization io on io.organization_id = o.organization_id " +
            "JOIN Intersections i on i.intersection_id = io.intersection_id " +
            "where o.name = :orgName";

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
                "FROM Users u JOIN UserOrganization uo on u.user_id = uo.user_id " +
                "JOIN Organizations o on uo.organization_id = o.organization_id " +
                "JOIN Roles r on uo.role_id = r.role_id " +
                "where u.email = :email AND o.name = :organization";
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
        TypedQuery<Integer> query = entityManager.createQuery(findUserIntersectionQuery, Integer.class);
        query.setParameter("email", email);
        return query.getResultList();
    }

    public List<Integer> getAllowedIntersectionIdsByOrganization(String organization) {
        TypedQuery<Integer> query = entityManager.createQuery(findIntersectionsByOrganizationQuery, Integer.class);
        query.setParameter("orgName", organization);
        return query.getResultList();
    }

    public boolean checkRsuWithOrg(String rsuIp, List<String> organizations) {
        if (organizations.isEmpty()) {
            return false;
        }
        String queryString = "SELECT rsu.ipv4_address::text AS ipv4_address " +
                "FROM public.rsus rsu " +
                "JOIN public.rsu_organization AS rsu_org ON rsu_org.rsu_id = rsu.rsu_id " +
                "JOIN public.organizations AS org ON org.organization_id = rsu_org.organization_id " +
                "WHERE org.name IN :allowedOrgs " +
                "AND rsu.ipv4_address = :rsuIp";

        TypedQuery<String> query = entityManager.createQuery(queryString, String.class);
        query.setParameter("allowedOrgs", organizations);
        query.setParameter("rsuIp", rsuIp);

        List<String> result = query.getResultList();
        return !result.isEmpty() && result.get(0).equals(rsuIp);
    }

    public boolean checkIntersectionWithOrg(String intersectionId, List<String> organizations) {
        if (organizations.isEmpty()) {
            return false;
        }
        String queryString = "SELECT i.intersection_number::text AS intersection_number " +
                "FROM public.intersections i " +
                "JOIN public.intersection_organization AS io ON io.intersection_id = i.intersection_id " +
                "JOIN public.organizations AS org ON org.organization_id = io.organization_id " +
                "WHERE org.name = :allowedOrgs " +
                "AND i.intersection_number = :intersectionId";

        TypedQuery<String> query = entityManager.createQuery(queryString, String.class);
        query.setParameter("allowedOrgs", organizations);
        query.setParameter("intersectionId", intersectionId);

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
