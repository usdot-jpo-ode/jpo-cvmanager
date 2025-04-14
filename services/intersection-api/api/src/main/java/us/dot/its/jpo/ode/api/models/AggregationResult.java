package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AggregationResult<T> {
    private List<T> results;
    private List<AggregationResultCount> metadata;
}