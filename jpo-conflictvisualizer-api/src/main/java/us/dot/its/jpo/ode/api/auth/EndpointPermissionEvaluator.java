package us.dot.its.jpo.ode.api.auth;

import java.io.Serializable;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import us.dot.its.jpo.ode.api.models.postgres.derived.UserOrgRole;
import us.dot.its.jpo.ode.api.services.PostgresService;

public class EndpointPermissionEvaluator implements PermissionEvaluator {

  @Autowired
  Keycloak keycloak;

  @Value("${keycloak.realm}")
  private String realm;

  @Autowired
  private PostgresService postgresService;
    
  
  @Override
  public boolean hasPermission(
    Authentication auth, Object targetDomainObject, Object permission) {
      if ((auth == null) || (targetDomainObject == null) || !(permission instanceof String)){
          return false;
      }
      String targetType = targetDomainObject.getClass().getSimpleName().toUpperCase();
      
      return hasPrivilege(auth, targetType, permission.toString().toUpperCase());
  }

  @Override
  public boolean hasPermission(
    Authentication auth, Serializable targetId, String targetType, Object permission) {
      if ((auth == null) || (targetType == null) || !(permission instanceof String)) {
          return false;
      }
      return hasPrivilege(auth, targetType.toUpperCase(), 
        permission.toString().toUpperCase());
  }

  private boolean hasPrivilege(Authentication auth, String targetType, String permission) {
    if(!auth.isAuthenticated()){
      return false;
    }
            
    JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
    String username = jwtAuth.getToken().getClaimAsString("preferred_username");

    List<UserOrgRole> roles = postgresService.findUserOrgRoles(username);

    System.out.println(jwtAuth);
    System.out.println("Username: " + username);
    System.out.println("Target Type: " + targetType);
    System.out.println("Permission: " + permission);
    
    for(UserOrgRole userOrgRole: roles){
        System.out.println(userOrgRole);
    }

    return true;
  }
}
