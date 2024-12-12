package us.dot.its.jpo.ode.mockdata;

import us.dot.its.jpo.ode.api.models.UserCreationRequest;

public class MockUserCreationRequestGenerator {
    
    public static UserCreationRequest getUserCreationRequest(){
        UserCreationRequest request = new UserCreationRequest();
        request.setEmail("test@test.com");
        request.setFirstName("testFirstName");
        request.setLastName("testLastName");
        request.setRole("USER");
        return request;
    }


}
