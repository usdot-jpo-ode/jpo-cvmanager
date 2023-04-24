package us.dot.its.jpo.ode.api.controllers;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.keycloak.admin.client.Keycloak;
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

import us.dot.its.jpo.ode.api.accessors.users.UserCreationRequest;
import us.dot.its.jpo.ode.api.accessors.users.UserRepository;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import us.dot.its.jpo.ode.api.Properties;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    UserRepository userRepo;

    @Autowired
    Properties props;

    @Autowired
    Keycloak keycloak;

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
    @PreAuthorize("hasRole('ADMIN')")
    public @ResponseBody ResponseEntity<String> new_user_creation_request(
            @RequestBody UserCreationRequest newUserCreationRequest) {
        try {
            
            // UserCreationRequest request = new UserCreationRequest(newUserCreationRequest.getFirstName(), newUserCreationRequest.getLastName(), newUserCreationRequest.getEmail());
            newUserCreationRequest.updateRequestSubmittedAt();
            userRepo.save(newUserCreationRequest);
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                    .body(newUserCreationRequest.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN)
                    .body(ExceptionUtils.getStackTrace(e));
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/users/accept_user_creation_request", method = RequestMethod.POST, produces = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public @ResponseBody ResponseEntity<String> accept_user_creation_request(
            @RequestBody UserCreationRequest newUserCreationRequest) {
        try {

            System.out.println(keycloak);
            UserRepresentation user = new UserRepresentation();
            user.setUsername(newUserCreationRequest.getEmail());
            user.setEmail(newUserCreationRequest.getEmail());
            user.setFirstName(newUserCreationRequest.getFirstName());
            user.setLastName(newUserCreationRequest.getLastName());
            user.setEnabled(true);

            List<String> groups = new ArrayList<>();
            
            if(newUserCreationRequest.getRole().equals("USER")){
                groups.add("user");
            } else if(newUserCreationRequest.getRole().equals("ADMIN")){
                groups.add("admin");
            }

            user.setGroups(groups);

            System.out.println("Requesting New User Creation");
            Response response = keycloak.realm(realm).users().create(user);
            if (response.getStatus() != 201) {
                System.out.println("User Creation Successful");
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
    @DeleteMapping(value = "/users/delete_user_creation_request")
    @PreAuthorize("hasRole('ADMIN')")
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