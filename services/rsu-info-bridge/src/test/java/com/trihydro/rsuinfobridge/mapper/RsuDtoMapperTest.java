package com.trihydro.rsuinfobridge.mapper;

import com.trihydro.rsuinfobridge.models.dtos.RsuDto;
import com.trihydro.rsuinfobridge.models.tables.Rsu;
import com.trihydro.rsuinfobridge.models.tables.RsuOption;
import com.trihydro.rsuinfobridge.models.tables.SnmpCredential;
import com.trihydro.rsuinfobridge.models.tables.SnmpProtocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mapstruct.factory.Mappers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RsuDtoMapperTest {

    private RsuDtoMapper mapper;

    @BeforeEach
    void setup() {
        mapper = Mappers.getMapper(RsuDtoMapper.class);
    }

    @Test
    void toDto_mapsAllFields() throws UnknownHostException {
        // Arrange
        SnmpProtocol snmpProtocol = new SnmpProtocol();
        snmpProtocol.setProtocolCode("NTCIP1218");

        SnmpCredential snmpCredential = new SnmpCredential();
        snmpCredential.setUsername("testuser");
        snmpCredential.setPassword("testpass");


        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(-104.9847, 39.73915);
        Point point = geometryFactory.createPoint(coordinate);

        RsuOption rsuOption = new RsuOption();
        rsuOption.setTimDeposit(true);

        Rsu rsu = Rsu.builder()
                .id(42)
                .ipv4Address(InetAddress.getByName("10.0.0.1"))
                .snmpProtocol(snmpProtocol)
                .snmpCredential(snmpCredential)
                .geography(point)
                .rsuOption(rsuOption)
                .build();

        // Act
        RsuDto dto = mapper.toDto(rsu);

        // Assert
        assertEquals("42", dto.getId());
        assertEquals("10.0.0.1", dto.getIpv4Address());
        assertEquals("NTCIP1218", dto.getSnmpProtocol());
        assertEquals("testuser", dto.getSnmpUsername());
        assertEquals("testpass", dto.getSnmpPassword());
        assertEquals("SHA", dto.getAuthenticationProtocol());
        assertEquals("AES", dto.getPrivacyProtocol());
        assertEquals(39.73915, dto.getLatitude());
        assertEquals(-104.9847, dto.getLongitude());
        assertTrue(dto.isTimDepositEnabled());
    }

    @Test
    void toDto_returnsNullForNullInput() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toDto_handlesNullGeography() throws UnknownHostException {
        // Arrange
        Rsu rsu = Rsu.builder()
                .id(1)
                .ipv4Address(InetAddress.getByName("10.0.0.1"))
                .geography(null)
                .build();

        // Act
        RsuDto dto = mapper.toDto(rsu);

        // Assert
        assertEquals(0.0, dto.getLatitude());
        assertEquals(0.0, dto.getLongitude());
    }

    @Test
    void toDto_handlesNullRsuOption() throws UnknownHostException {
        // Arrange
        Rsu rsu = Rsu.builder()
                .id(1)
                .ipv4Address(InetAddress.getByName("10.0.0.1"))
                .rsuOption(null)
                .build();

        // Act
        RsuDto dto = mapper.toDto(rsu);

        // Assert
        assertFalse(dto.isTimDepositEnabled());
    }

    @Test
    void toDto_handlesNullIpv4Address() {
        // Arrange
        Rsu rsu = Rsu.builder()
                .id(1)
                .ipv4Address(null)
                .build();

        // Act
        RsuDto dto = mapper.toDto(rsu);

        // Assert
        assertNull(dto.getIpv4Address());
    }

    @Test
    void toDto_handlesNullId() {
        // Arrange
        Rsu rsu = Rsu.builder()
                .id(null)
                .build();

        // Act
        RsuDto dto = mapper.toDto(rsu);

        // Assert
        assertNull(dto.getId());
    }

    @Test
    void toDto_handlesNullSnmpCredential() throws UnknownHostException {
        // Arrange
        Rsu rsu = Rsu.builder()
                .id(1)
                .ipv4Address(InetAddress.getByName("10.0.0.1"))
                .snmpCredential(null)
                .build();

        // Act
        RsuDto dto = mapper.toDto(rsu);

        // Assert
        assertNull(dto.getSnmpUsername());
        assertNull(dto.getSnmpPassword());
    }

    @Test
    void toDto_handlesNullSnmpProtocol() throws UnknownHostException {
        // Arrange
        Rsu rsu = Rsu.builder()
                .id(1)
                .ipv4Address(InetAddress.getByName("10.0.0.1"))
                .snmpProtocol(null)
                .build();

        // Act
        RsuDto dto = mapper.toDto(rsu);

        // Assert
        assertNull(dto.getSnmpProtocol());
    }

    @Test
    void toDto_timDepositFalseWhenOptionIsFalse() throws UnknownHostException {
        // Arrange
        RsuOption rsuOption = new RsuOption();
        rsuOption.setTimDeposit(false);

        Rsu rsu = Rsu.builder()
                .id(1)
                .ipv4Address(InetAddress.getByName("10.0.0.1"))
                .rsuOption(rsuOption)
                .build();

        // Act
        RsuDto dto = mapper.toDto(rsu);

        // Assert
        assertFalse(dto.isTimDepositEnabled());
    }

    @Test
    void toDto_setsConstantProtocols() throws UnknownHostException {
        // Arrange
        Rsu rsu = Rsu.builder()
                .id(1)
                .ipv4Address(InetAddress.getByName("10.0.0.1"))
                .build();

        // Act
        RsuDto dto = mapper.toDto(rsu);

        // Assert
        assertEquals(RsuDtoMapper.AUTHENTICATION_PROTOCOL, dto.getAuthenticationProtocol());
        assertEquals(RsuDtoMapper.PRIVACY_PROTOCOL, dto.getPrivacyProtocol());
    }

    @Test
    void toDtoList_mapsAllElements() throws UnknownHostException {
        // Arrange
        SnmpProtocol snmpProtocol = new SnmpProtocol();
        snmpProtocol.setProtocolCode("NTCIP1218");

        SnmpCredential cred1 = new SnmpCredential();
        cred1.setUsername("user1");
        cred1.setPassword("pass1");

        SnmpCredential cred2 = new SnmpCredential();
        cred2.setUsername("user2");
        cred2.setPassword("pass2");

        RsuOption option = new RsuOption();
        option.setTimDeposit(true);

        Rsu rsu1 = Rsu.builder()
                .id(1)
                .ipv4Address(InetAddress.getByName("10.0.0.1"))
                .snmpProtocol(snmpProtocol)
                .snmpCredential(cred1)
                .rsuOption(option)
                .build();

        Rsu rsu2 = Rsu.builder()
                .id(2)
                .ipv4Address(InetAddress.getByName("10.0.0.2"))
                .snmpProtocol(snmpProtocol)
                .snmpCredential(cred2)
                .rsuOption(option)
                .build();

        // Act
        List<RsuDto> dtos = mapper.toDtoList(List.of(rsu1, rsu2));

        // Assert
        assertEquals(2, dtos.size());
        assertEquals("1", dtos.get(0).getId());
        assertEquals("10.0.0.1", dtos.get(0).getIpv4Address());
        assertEquals("user1", dtos.get(0).getSnmpUsername());
        assertEquals("2", dtos.get(1).getId());
        assertEquals("10.0.0.2", dtos.get(1).getIpv4Address());
        assertEquals("user2", dtos.get(1).getSnmpUsername());
    }

    @Test
    void toDtoList_returnsNullForNullInput() {
        assertNull(mapper.toDtoList(null));
    }

    @Test
    void toDtoList_returnsEmptyListForEmptyInput() {
        List<RsuDto> dtos = mapper.toDtoList(List.of());
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }
}