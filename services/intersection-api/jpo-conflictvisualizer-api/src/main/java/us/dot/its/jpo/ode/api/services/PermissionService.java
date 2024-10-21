package us.dot.its.jpo.ode.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole;
import us.dot.its.jpo.ode.api.models.postgres.tables.Users;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service("PermissionService")
public class PermissionService {

    @Autowired
    PostgresService postgresService;
    Logger logger = LoggerFactory.getLogger(PermissionService.class);


    @Autowired
    ConflictMonitorApiProperties properties;

    // Allow Connection if the user is a SuperUser
    public boolean isSuperUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!isAuthValid(auth)){
            return false;
        }

        String username = getUsername(auth);
        List<Users> users = postgresService.findUser(username);

        for(Users user: users){

            if(user.isSuper_user()){
                return true;
            }
        }
        
        return false;
    }
    
    // Allow Connection if the user is apart of at least one organization with a matching roll.
    public boolean hasRole(String role){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!isAuthValid(auth)){
            return false;
        }

        String username = getUsername(auth);
        

        List<UserOrgRole> roles = postgresService.findUserOrgRoles(username);
        
        for(UserOrgRole userOrgRole: roles){
            if(userOrgRole.getRole_name().toUpperCase().equals(role)){
                return true;
            }
        }
        return false;
    }

    
    // Allow Connection if the users organization controls the specified intersection
    public boolean hasIntersection(Integer intersectionID){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!isAuthValid(auth)){
            return false;
        }

        if(!properties.getEnableOrganizationIntersectionChecking()){
            // Skip Validation if not enabled
            return true;
        }

        String username = getUsername(auth);
        List<Integer> allowedIntersectionIds = postgresService.getAllowedIntersectionIdByEmail(username);
        allowedIntersectionIds.add(-1); // all users all allowed to access the empty intersection ID.

        if(allowedIntersectionIds.contains(intersectionID)){
            return true;
        }

        return false;

    }


    // Allow Connection if the users organization controls the specified RSU unit
    public boolean hasRSU(String rsuIP){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!isAuthValid(auth)){
            return false;
        }

        if(!properties.getEnableOrganizationIntersectionChecking()){
            // Skip Validation if not enabled
            return true;
        }

        String username = getUsername(auth);
        List<String> allowedIntersectionIds = postgresService.getAllowedRSUIPByEmail(username);
        if(allowedIntersectionIds.contains(rsuIP)){
            return true;
        }

        return false;

    } 


    // helper method to make sure authentication is valid
    public boolean isAuthValid(Authentication auth){
        if(!auth.isAuthenticated()){
            return false;
        }

        if(!(auth instanceof JwtAuthenticationToken)){
            return false;
        }

        return true;
    }

    public String getUsername(Authentication auth){
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        String username = jwtAuth.getToken().getClaimAsString("preferred_username");
        return username;
    }

    
}