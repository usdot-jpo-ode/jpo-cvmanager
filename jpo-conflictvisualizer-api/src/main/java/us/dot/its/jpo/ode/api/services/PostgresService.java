package us.dot.its.jpo.ode.api.services;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole;

@Service
public class PostgresService {

    @PersistenceContext
    private EntityManager entityManager;
    
    private final String findUserOrgRolesQuery = 
        "SELECT new us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole(u.email, o.name, r.name) " +
        "FROM Users u JOIN UserOrganization uo on u.user_id = uo.user_id " +
        "JOIN Organizations o on uo.organization_id = o.organization_id "+
        "JOIN Roles r on uo.role_id = r.role_id " +
        "where u.email = '%s'";

    

    public List<UserOrgRole> findUserOrgRoles(String email){
        String queryString = String.format(findUserOrgRolesQuery, email);

        TypedQuery<UserOrgRole> query 
            = entityManager.createQuery(queryString, UserOrgRole.class);
        return query.getResultList();
    }
}
