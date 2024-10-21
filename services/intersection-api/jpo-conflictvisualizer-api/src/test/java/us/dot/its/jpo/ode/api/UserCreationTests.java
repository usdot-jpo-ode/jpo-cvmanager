package us.dot.its.jpo.ode.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import us.dot.its.jpo.ode.api.accessors.users.UserRepository;
import us.dot.its.jpo.ode.api.controllers.UserController;
import us.dot.its.jpo.ode.api.models.UserCreationRequest;
import us.dot.its.jpo.ode.mockdata.MockUserCreationRequestGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserCreationTests {

    @MockBean
    UserRepository userRepo;

    @Autowired
    UserController controller;

    @Test
    public void testfindUserCreationRequests() {

        MockKeyCloakAuth.setSecurityContextHolder("cm_admin", Set.of("ADMIN"));

        UserCreationRequest request = MockUserCreationRequestGenerator.getUserCreationRequest();
        List<UserCreationRequest> list = new ArrayList<>();
        list.add(request);

        Query query = userRepo.getQuery(request.getId(), request.getFirstName(), request.getLastName(), request.getEmail(), request.getRole(), 0L, null);

        when(userRepo.find(query)).thenReturn(list);

        ResponseEntity<List<UserCreationRequest>> result = controller.findUserCreationRequests(request.getId(), request.getFirstName(), request.getLastName(), request.getEmail(), request.getRole(), 0L, null);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(list);
    }
}