package com.trihydro.rsuinfobridge.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "Roadside Unit information")
public class RsuDto {
    @Schema(description = "Unique identifier for the RSU", example = "1")
    private String id;

    @Schema(description = "IPv4 address of the RSU", example = "10.10.10.10")
    private String ipv4Address;

    @Schema(description = "SNMP protocol version", example = "NTCIP1218")
    private String snmpProtocol;

    @Schema(description = "SNMP username for authentication", example = "myusername")
    private String snmpUsername;

    @Schema(description = "SNMP password for authentication", example = "mypassword")
    private String snmpPassword;

    @Schema(description = "Authentication protocol used", example = "SHA")
    private String authenticationProtocol;

    @Schema(description = "Privacy protocol used", example = "AES")
    private String privacyProtocol;

    @Schema(description = "Latitude coordinate of the RSU location", example = "39.73915")
    private double latitude;

    @Schema(description = "Longitude coordinate of the RSU location", example = "-104.9847")
    private double longitude;

    @Schema(description = "Indicates whether TIM (Traveler Information Message) deposit is enabled", example = "true")
    private boolean timDepositEnabled;

    @Schema(description = "Manufacturer name", example = "Commsignia")
    private String manufacturerName;
}
