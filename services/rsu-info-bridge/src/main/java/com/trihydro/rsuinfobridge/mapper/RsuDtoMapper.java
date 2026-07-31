package com.trihydro.rsuinfobridge.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.trihydro.rsuinfobridge.models.dtos.RsuDto;
import com.trihydro.rsuinfobridge.models.tables.Rsu;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RsuDtoMapper {
    String AUTHENTICATION_PROTOCOL = "SHA";
    String PRIVACY_PROTOCOL = "AES";

    /**
     * Convert Rsu entity to RsuDto
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "ipv4Address.hostAddress", target = "ipv4Address")
    @Mapping(source = "snmpProtocol.protocolCode", target = "snmpProtocol")
    @Mapping(source = "snmpCredential.username", target = "snmpUsername")
    @Mapping(source = "snmpCredential.password", target = "snmpPassword")
    @Mapping(target = "authenticationProtocol", constant = AUTHENTICATION_PROTOCOL)
    @Mapping(target = "privacyProtocol", constant = PRIVACY_PROTOCOL)
    @Mapping(source = "geography.y", target = "latitude")
    @Mapping(source = "geography.x", target = "longitude")
    @Mapping(source = "rsuOption.timDeposit", target = "timDepositEnabled")
    @Mapping(source = "model.manufacturer.name", target = "manufacturerName")
    RsuDto toDto(Rsu rsu);

    List<RsuDto> toDtoList(List<Rsu> rsus);
}
