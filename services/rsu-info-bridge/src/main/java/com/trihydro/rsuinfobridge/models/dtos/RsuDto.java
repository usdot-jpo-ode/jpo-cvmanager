package com.trihydro.rsuinfobridge.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RsuDto {
    private String id;
    private String ipv4Address;
    private String snmpProtocol;
    private String snmpUsername;
    private String snmpPassword;
    private String authenticationProtocol; // optional
    private String privacyProtocol; // optional
    private double latitude;
    private double longitude;
    private boolean timDepositEnabled;
}
