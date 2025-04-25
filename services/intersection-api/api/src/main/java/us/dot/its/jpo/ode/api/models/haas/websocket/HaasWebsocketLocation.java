package us.dot.its.jpo.ode.api.models.haas.websocket;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HaasWebsocketLocation {
    private String type;
    private String id;

    @JsonProperty("detailed_type")
    @Field("detailed_type")
    private String detailedType;

    @JsonProperty("external_id")
    @Field("external_id")
    private String externalId;

    @JsonProperty("start_time")
    @Field("start_time")
    private String startTime;

    @JsonProperty("end_time")
    @Field("end_time")
    private String endTime;

    private double lat;
    private double lon;
    private double alt;

    @JsonProperty("street_name")
    @Field("street_name")
    private String streetName;

    @JsonProperty("location_type")
    @Field("location_type")
    private String locationType;

    @JsonProperty("is_active")
    @Field("is_active")
    private boolean isActive;

    @JsonProperty("things_active")
    @Field("things_active")
    private List<ThingReference> thingsActive;

    @JsonProperty("things_inactive")
    @Field("things_inactive")
    private List<ThingReference> thingsInactive;

    @JsonProperty("features")
    @Field("features")
    private List<Feature> features;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThingReference {
        private String id;

        @JsonProperty("external_id")
        @Field("external_id")
        private String externalId;

        @JsonProperty("start_time")
        @Field("start_time")
        private String startTime;

        @JsonProperty("end_time")
        @Field("end_time")
        private String endTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Feature {
        private String type;
        private List<Geometry> geometry;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Geometry {
        private double lat;
        private double lon;
    }
}