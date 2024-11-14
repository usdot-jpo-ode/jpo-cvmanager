// package us.dot.its.jpo.ode.api.controllers;

// import lombok.extern.slf4j.Slf4j;
// import org.keycloak.KeycloakSecurityContext;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.context.request.RequestContextHolder;
// import org.springframework.web.context.request.ServletRequestAttributes;
 
// import javax.servlet.http.HttpServletRequest;
 
// @RestController
// @RequestMapping(value = "/api")
// @Slf4j
// public class SampleController {
 
//     // url - http://localhost:9000/api/keycloak/user
//     // permits only the authenticated user having either the ROLE_ADMIN or ROLE_USER
//     // throws forbidden exception for the invalidated token or non authorized user
//     @GetMapping("/keycloak/user")
//     @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
//     public ResponseEntity<String> getUser() {
//         log.info("Returning user information");
//         final String name = getSecurityContext().getToken().getPreferredUsername();
//         return ResponseEntity.ok("hello " + name);
//     }
 
//     // url - http://localhost:9000/api/keycloak/admin
//     // permits only the authenticated user having the ROLE_ADMIN
//     // throws forbidden exception for the invalidated token or non authorized user
//     @GetMapping("/keycloak/admin")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<String> getAdmin() {
//         log.info("Returning administrator information");
//         final String name = getSecurityContext().getToken().getPreferredUsername();
//         return ResponseEntity.ok("hello Admin:" + name);
//     }
 
//     // url - http://localhost:9000/api/public/anonymous
//     // permits everyone without a bearer token i.e. offers public access
//     @GetMapping("/public/anonymous")
//     public ResponseEntity<String> getAnonymous() {
//         log.info("Returning anonymous information");
//         return ResponseEntity.ok("hello anonymous user");
//     }
 
//     // helper method to return the KeycloakSecurityContext object to fetch details from access token
//     private KeycloakSecurityContext getSecurityContext() {
//         final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//         return (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
//     }
// }