package us.dot.its.jpo.ode.api.accessors.users;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;

import us.dot.its.jpo.ode.api.models.UserCreationRequest;


public interface UserRepository{
    Query getQuery(String id, String firstName, String lastName, String email, String role,  Long startTime, Long endTime);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);
    
    List<UserCreationRequest> find(Query query);

    void delete(Query query);

    void save(UserCreationRequest request);
}