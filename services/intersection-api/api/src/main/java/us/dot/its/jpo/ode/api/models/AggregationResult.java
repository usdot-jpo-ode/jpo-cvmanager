package us.dot.its.jpo.ode.api.models;

import java.util.List;

public class AggregationResult<T> {
    private List<T> data;
    private List<AggregationMetadata> metadata;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public List<AggregationMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<AggregationMetadata> metadata) {
        this.metadata = metadata;
    }
}