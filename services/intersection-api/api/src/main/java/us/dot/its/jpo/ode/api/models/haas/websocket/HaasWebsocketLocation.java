package us.dot.its.jpo.ode.api.models.haas.websocket;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HaasWebsocketLocation {
    @JsonProperty("id")
    @Field("id")
    private String id;

    @JsonProperty("type")
    @Field("type")
    private String type;

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

    @JsonProperty("lat")
    @Field("lat")
    private double lat;

    @JsonProperty("lon")
    @Field("lon")
    private double lon;

    @JsonProperty("alt")
    @Field("alt")
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThingReference {
        @JsonProperty("id")
        @Field("id")
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Feature {
        @JsonProperty("type")
        @Field("type")
        private String type;

        @JsonProperty("geometry")
        @Field("geometry")
        private List<Geometry> geometry;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Geometry {
        @JsonProperty("lat")
        @Field("lat")
        private double lat;

        @JsonProperty("lon")
        @Field("lon")
        private double lon;
    }
}