package us.dot.its.jpo.ode.api.controllers;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

// import jakarta.ws.rs.core.Response;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.ode.api.accessors.users.UserRepository;
import us.dot.its.jpo.ode.api.models.EmailSettings;
import us.dot.its.jpo.ode.api.models.UserCreationRequest;
import us.dot.its.jpo.ode.api.services.EmailService;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    UserRepository userRepo;

    @Autowired
    ConflictMonitorApiProperties props;

    @Autowired
    Keycloak keycloak;

    @Autowired
    EmailService email;

    @Value("${keycloak.realm}")
    private String realm;

    public String getCurrentTime() {
        return ZonedDateTime.now().toInstant().toEpochMilli() + "";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/users/find_user_creation_request", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<UserCreationRequest>> findUserCreationRequests(
        @RequestParam(name = "id", required = false) String id,
        @RequestParam(name = "firstName", required = false) String firstName,
        @RequestParam(name = "lastName", required = false) String lastName,
        @RequestParam(name = "email", required = false) String email,
        @RequestParam(name = "role", required = false) String role,
        @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
        @RequestParam(name = "end_time_utc_millis", required = false) Long endTime) {


        Query query = userRepo.getQuery(id, firstName, lastName, email, role, startTime, endTime);
        long count = userRepo.getQueryResultCount(query);
        if (count <= props.getMaximumResponseSize()) {
            logger.info("Returning User Creation Requests with Size: " + count);
            return ResponseEntity.ok(userRepo.find(query));
        } else {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "The requested query has more results than allowed by server. Please reduce the query bounds and try again.");

        }
        
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/users/create_user_creation_request", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody ResponseEntity<String> new_user_creation_request(
            @RequestBody UserCreationRequest newUserCreationRequest) {
        try {
            System.out.println("Creating new User Request");

            newUserCreationRequest.updateRequestSubmittedAt();
            userRepo.save(newUserCreationRequest);

            try{
                email.sendSimpleMessage(newUserCreationRequest.getEmail(),"User Request Received","Thank you for submitting a request for access to the Conflict Visualizer."+
                " An admin will review your request shortly.");
            } catch(Exception e){
                logger.info("Failed to send email to new user: " + newUserCreationRequest.getEmail() + "Exception: " + e.getMessage());
            }

            try{
                List<UserRepresentation> admins = email.getSimpleEmailList("receiveNewUserRequests", "ADMIN", null);
                email.emailList(admins, "New User Creation Request", "A new user would like access to the conflict monitor.\n\n User info: \n" + 
                "First Name: " + newUserCreationRequest.getFirstName() + "\n" + 
                "Last Name: " + newUserCreationRequest.getLastName() + "\n" + 
                "Email: " + newUserCreationRequest.getEmail() + "\n" +
                "Desired Role: " + newUserCreationRequest.getRole() + "\n\n\n" + 
                "Please Log into the Conflict Monitor Management Console to accept or reject the new user request.\n\n"
                );
            } catch(Exception e){
                logger.info("Failed to send email to admin group. Exception: " + e.getMessage());
            }
            

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                    .body(newUserCreationRequest.toString());
        } catch (Exception e) {
            System.out.println("Cannot Create New User in Database");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN)
                    .body(ExceptionUtils.getStackTrace(e));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/users/accept_user_creation_request", method = RequestMethod.POST, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('ADMIN')")
    public @ResponseBody ResponseEntity<String> accept_user_creation_request(
            @RequestBody UserCreationRequest newUserCreationRequest) {
        try {

            UserRepresentation user = new UserRepresentation();
            user.setUsername(newUserCreationRequest.getEmail());
            user.setEmail(newUserCreationRequest.getEmail());
            user.setFirstName(newUserCreationRequest.getFirstName());
            user.setLastName(newUserCreationRequest.getLastName());
            user.setEnabled(true);
            
            
            List<String> groups = new ArrayList<>();

            EmailSettings settings = new EmailSettings();

            
            if(newUserCreationRequest.getRole().equals("USER")){
                settings.setReceiveNewUserRequests(false);
                groups.add("USER");
            } else if(newUserCreationRequest.getRole().equals("ADMIN")){
                groups.add("ADMIN");
                settings.setReceiveNewUserRequests(true); 
            }

            Map<String, List<String>> attributes = settings.toAttributes();
            user.setGroups(groups);
            user.setAttributes(attributes);
            




            logger.info("Requesting New User Creation");
            Response response = keycloak.realm(realm).users().create(user);
            logger.info(response.getStatus() + " " +  response.getHeaders());

            if (response.getStatus() == 201) {
                logger.info("User Creation Successful");

                Query query = userRepo.getQuery(null, null, null, newUserCreationRequest.getEmail(), null, null, null);
                userRepo.delete(query);
                
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                    .body(newUserCreationRequest.toString());
            }else{
                
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).contentType(MediaType.APPLICATION_JSON)
                    .body(newUserCreationRequest.toString());
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN)
                    .body(ExceptionUtils.getStackTrace(e));
        }
    }


    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/users/update_user_email_preference", method = RequestMethod.POST, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN')")
    public @ResponseBody ResponseEntity<String> update_user_email_preference(
            @RequestBody EmailSettings newEmailSettings) {
        try {

            SecurityContext securityContext = SecurityContextHolder.getContext();
            Authentication authentication = securityContext.getAuthentication();
            
            if (authentication.getPrincipal() instanceof KeycloakPrincipal) {
                KeycloakPrincipal principal = (KeycloakPrincipal) authentication.getPrincipal();
                UserResource userResource = keycloak.realm(realm).users().get(principal.getName());
                UserRepresentation user = userResource.toRepresentation();
                user.setAttributes(newEmailSettings.toAttributes());
                userResource.update(user);
            }
            

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                .body(newEmailSettings.toString());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN)
                    .body(ExceptionUtils.getStackTrace(e));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/users/get_user_email_preference", method = RequestMethod.POST, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN')")
    public @ResponseBody ResponseEntity<EmailSettings> get_user_email_preference() {
        try {
            EmailSettings settings = new EmailSettings();
            SecurityContext securityContext = SecurityContextHolder.getContext();
            Authentication authentication = securityContext.getAuthentication();
            if (authentication.getPrincipal() instanceof KeycloakPrincipal) {
                KeycloakPrincipal principal = (KeycloakPrincipal) authentication.getPrincipal();
                UserRepresentation user = keycloak.realm(realm).users().get(principal.getName()).toRepresentation();
                Map<String, List<String>> attributes = user.getAttributes();
                settings = EmailSettings.fromAttributes(attributes);
            }

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                .body(settings);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN)
                    .body(null);
        }
    }


    


    @CrossOrigin(origins = "http://localhost:3000")
    @DeleteMapping(value = "/users/delete_user_creation_request")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('ADMIN')")
    public @ResponseBody ResponseEntity<String> intersection_config_delete(@RequestBody UserCreationRequest request) {
        Query query = userRepo.getQuery(request.getId(), request.getFirstName(), request.getLastName(), request.getEmail(),null, null, null);
        try {
            userRepo.delete(query);
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(request.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN)
                    .body(ExceptionUtils.getStackTrace(e));
        }
    }
}