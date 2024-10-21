package us.dot.its.jpo.ode.api.models;

import java.time.ZonedDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import us.dot.its.jpo.geojsonconverter.DateJsonMapper;

@Getter
@Setter
@EqualsAndHashCode
@Document("CmUserCreationRequest")
public class UserCreationRequest {
    
    @Id
    public String id;
    public String firstName;
    public String lastName;
    public String email;
    public String role;

    public long requestSubmittedAt;

    public UserCreationRequest(){
        this.requestSubmittedAt = ZonedDateTime.now().toInstant().toEpochMilli();
    }

    public UserCreationRequest(String firstName, String lastName, String email, String role){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.requestSubmittedAt = ZonedDateTime.now().toInstant().toEpochMilli();
    }

    public void updateRequestSubmittedAt(){
        this.requestSubmittedAt = ZonedDateTime.now().toInstant().toEpochMilli();
    }

    @Override
    public String toString() {
        ObjectMapper mapper = DateJsonMapper.getInstance();
        String testReturn = "";
        try {
            testReturn = (mapper.writeValueAsString(this));
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }
        return testReturn;
    }
    

}
