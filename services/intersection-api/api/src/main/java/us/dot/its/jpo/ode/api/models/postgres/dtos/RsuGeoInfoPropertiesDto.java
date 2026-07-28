package us.dot.its.jpo.ode.api.models.postgres.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Flat properties block inside a GeoJSON Feature returned by the
 * /devices/rsus/info endpoint.
 * Mirrors the column set selected by the Python rsuinfo.get_rsu_data query.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RsuGeoInfoPropertiesDto {

    @JsonProperty("rsu_id")
    private Integer rsuId;

    @JsonProperty("milepost")
    private Double milepost;

    @JsonProperty("ipv4_address")
    private String ipv4Address;

    @JsonProperty("serial_number")
    private String serialNumber;

    @JsonProperty("primary_route")
    private String primaryRoute;

    @JsonProperty("tim_deposit")
    private Boolean timDeposit;

    @JsonProperty("snmp_monitoring")
    private Boolean snmpMonitoring;

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("manufacturer_name")
    private String manufacturerName;
}
