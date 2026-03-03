package com.trihydro.rsuinfobridge.service;

import com.trihydro.rsuinfobridge.models.dtos.RsuDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RsuService {
    private final static String AUTHENTICATION_PROTOCOL = "SHA";
    private final static String PRIVACY_PROTOCOL = "AES";

    public List<RsuDto> getAll(boolean timDepositEnabledOnly) {
        return getMockData();
    }

    private List<RsuDto> getMockData() {
        List<RsuDto> rsus = new java.util.ArrayList<>();

        RsuDto rsu1 = RsuDto.builder()
                .id("1")
                .ipv4Address("10.10.10.10")
                .snmpProtocol("NTCIP1218")
                .snmpUsername("myusername")
                .snmpPassword("mypassword")
                .authenticationProtocol(AUTHENTICATION_PROTOCOL)
                .privacyProtocol(PRIVACY_PROTOCOL)
                .latitude(39.73915)
                .longitude(-104.9847)
                .timDepositEnabled(true)
                .build();
        rsus.add(rsu1);

        RsuDto rsu2 = RsuDto.builder()
                .id("2")
                .ipv4Address("10.10.10.11")
                .snmpProtocol("NTCIP1218")
                .snmpUsername("myusername2")
                .snmpPassword("mypassword2")
                .authenticationProtocol(AUTHENTICATION_PROTOCOL)
                .privacyProtocol(PRIVACY_PROTOCOL)
                .latitude(40.0)
                .longitude(105.0)
                .timDepositEnabled(true)
                .build();
        rsus.add(rsu2);

        return rsus;
    }
}
