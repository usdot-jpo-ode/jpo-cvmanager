package us.dot.its.jpo.ode.api.services;

import java.util.List;

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
    
    private final String findUserOrgRolesQuery = 
        "SELECT new us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole(u.email, o.name, r.name) " +
        "FROM Users u JOIN UserOrganization uo on u.user_id = uo.user_id " +
        "JOIN Organizations o on uo.organization_id = o.organization_id "+
        "JOIN Roles r on uo.role_id = r.role_id " +
        "where u.email = \"%s\"";


    private final String findUserQuery = "SELECT u from Users u where u.email = \"%s\"";


    private final String findUserRsuIPQuery = 
        "select r.ipv4_address " + 
        "FROM Users u JOIN UserOrganization uo on u.user_id = uo.user_id " + 
        "JOIN RsuOrganization ro on ro.organization_id = uo.organization_id " + 
        "JOIN Rsus r on r.rsu_id = ro.rsu_id " +
        "where u.email = '%s'";


    private final String findUserIntersectionQuery = 
        "select io.intersection_id " + 
        "FROM Users u JOIN UserOrganization uo on u.user_id = uo.user_id " + 
        "JOIN IntersectionOrganization io on io.organization_id = uo.organization_id " + 
        "JOIN Intersections i on i.intersection_id = io.intersection_id " +
        "where u.email = '%s'";


    

    public List<UserOrgRole> findUserOrgRoles(String email){
        String queryString = String.format(findUserOrgRolesQuery, email);

        TypedQuery<UserOrgRole> query 
            = entityManager.createQuery(queryString, UserOrgRole.class);
        return query.getResultList();
    }

    public List<Users> findUser(String email){
        String queryString = String.format(findUserQuery, email);

        TypedQuery<Users> query 
            = entityManager.createQuery(queryString, Users.class).setMaxResults(1);
        return query.getResultList();
    }

    public List<String> getAllowedRSUIPByEmail(String email){
        String queryString = String.format(findUserRsuIPQuery, email);

        TypedQuery<String> query 
            = entityManager.createQuery(queryString, String.class);
        return query.getResultList();
    }

    public List<Integer> getAllowedIntersectionIdByEmail(String email){
        String queryString = String.format(findUserIntersectionQuery, email);


        TypedQuery<Integer> query 
            = entityManager.createQuery(queryString, Integer.class);
        return query.getResultList();
    }
}
