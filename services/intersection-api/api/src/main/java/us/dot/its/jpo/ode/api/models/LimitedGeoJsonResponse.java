package us.dot.its.jpo.ode.api.models;

import lombok.Data;
import us.dot.its.jpo.ode.api.models.geojson.GeoJsonFeatureCollection;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;
import us.dot.its.jpo.ode.api.converters.HaasLocationConverter;

import java.util.List;

@Data
public class LimitedGeoJsonResponse {
    private GeoJsonFeatureCollection data;
    private ResponseMetadata metadata;

    @Data
    public static class ResponseMetadata {
        private int limit;
        private int returnedCount;
        private boolean truncated;
        private String message;
    }

    public static LimitedGeoJsonResponse fromList(List<HaasLocation> locations, int limit, boolean hasMoreResults) {
        LimitedGeoJsonResponse response = new LimitedGeoJsonResponse();
        response.setData(HaasLocationConverter.toGeoJson(locations));

        ResponseMetadata metadata = new ResponseMetadata();
        metadata.setLimit(limit);
        metadata.setReturnedCount(locations.size());
        metadata.setTruncated(hasMoreResults);
        metadata.setMessage(hasMoreResults
                ? "Response was truncated due to limit. Increase the 'limit' parameter to get more results."
                : "All available results returned.");
        response.setMetadata(metadata);

        return response;
    }
}