package us.dot.its.jpo.ode.api.models;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;

public interface DataRepo<T> {
    void add(T item);

    void count(Query query);

    void fullCount(Query query);

    List<T> find(Query query);
}
