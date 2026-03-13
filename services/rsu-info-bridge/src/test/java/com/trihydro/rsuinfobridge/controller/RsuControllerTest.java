package com.trihydro.rsuinfobridge.controller;

import com.trihydro.rsuinfobridge.models.tables.Rsu;
import com.trihydro.rsuinfobridge.models.tables.RsuOption;
import com.trihydro.rsuinfobridge.models.tables.SnmpCredential;
import com.trihydro.rsuinfobridge.models.tables.SnmpProtocol;
import com.trihydro.rsuinfobridge.repository.RsuRepository;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RsuControllerTest {
    @MockitoBean
    RsuRepository rsuRepository;

    @Autowired
    MockMvc mockMvc;

    // ==================== Happy path tests ====================

    @Test
    void testGetAll_Success() throws Exception {
        // Arrange
        List<Rsu> rsus = getMockData();
        when(rsuRepository.findAll()).thenReturn(rsus);

        // Act
        ResultActions resultActions = mockMvc.perform(get("/rsus"));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                // RSU 1
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].ipv4Address").value("10.10.10.10"))
                .andExpect(jsonPath("$[0].snmpProtocol").value("NTCIP1218"))
                .andExpect(jsonPath("$[0].snmpUsername").value("myusername"))
                .andExpect(jsonPath("$[0].snmpPassword").value("mypassword"))
                .andExpect(jsonPath("$[0].authenticationProtocol").value("SHA"))
                .andExpect(jsonPath("$[0].privacyProtocol").value("AES"))
                .andExpect(jsonPath("$[0].latitude").value(39.73915))
                .andExpect(jsonPath("$[0].longitude").value(-104.9847))
                .andExpect(jsonPath("$[0].timDepositEnabled").value(true))
                // RSU 2
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].ipv4Address").value("10.10.10.11"))
                .andExpect(jsonPath("$[1].snmpProtocol").value("NTCIP1218"))
                .andExpect(jsonPath("$[1].snmpUsername").value("myusername2"))
                .andExpect(jsonPath("$[1].snmpPassword").value("mypassword2"))
                .andExpect(jsonPath("$[1].authenticationProtocol").value("SHA"))
                .andExpect(jsonPath("$[1].privacyProtocol").value("AES"))
                .andExpect(jsonPath("$[1].latitude").value(40.0))
                .andExpect(jsonPath("$[1].longitude").value(105.0))
                .andExpect(jsonPath("$[1].timDepositEnabled").value(true));
    }

    @Test
    void testGetAllWithTimDepositEnabled_Success() throws Exception {
        // Arrange
        List<Rsu> rsus = getMockData();
        when(rsuRepository.findByRsuOptionTimDepositIsTrue()).thenReturn(rsus);

        // Act
        ResultActions resultActions = mockMvc.perform(get("/rsus?timDepositEnabledOnly=true"));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                // RSU 1
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].ipv4Address").value("10.10.10.10"))
                .andExpect(jsonPath("$[0].snmpProtocol").value("NTCIP1218"))
                .andExpect(jsonPath("$[0].snmpUsername").value("myusername"))
                .andExpect(jsonPath("$[0].snmpPassword").value("mypassword"))
                .andExpect(jsonPath("$[0].authenticationProtocol").value("SHA"))
                .andExpect(jsonPath("$[0].privacyProtocol").value("AES"))
                .andExpect(jsonPath("$[0].latitude").value(39.73915))
                .andExpect(jsonPath("$[0].longitude").value(-104.9847))
                .andExpect(jsonPath("$[0].timDepositEnabled").value(true))
                // RSU 2
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].ipv4Address").value("10.10.10.11"))
                .andExpect(jsonPath("$[1].snmpProtocol").value("NTCIP1218"))
                .andExpect(jsonPath("$[1].snmpUsername").value("myusername2"))
                .andExpect(jsonPath("$[1].snmpPassword").value("mypassword2"))
                .andExpect(jsonPath("$[1].authenticationProtocol").value("SHA"))
                .andExpect(jsonPath("$[1].privacyProtocol").value("AES"))
                .andExpect(jsonPath("$[1].latitude").value(40.0))
                .andExpect(jsonPath("$[1].longitude").value(105.0))
                .andExpect(jsonPath("$[1].timDepositEnabled").value(true));
    }

    // ==================== Unhappy path tests ====================

    @Test
    void testGetAll_EmptyList() throws Exception {
        // Arrange
        when(rsuRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ResultActions resultActions = mockMvc.perform(get("/rsus"));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetAllWithTimDepositEnabled_EmptyList() throws Exception {
        // Arrange
        when(rsuRepository.findByRsuOptionTimDepositIsTrue()).thenReturn(Collections.emptyList());

        // Act
        ResultActions resultActions = mockMvc.perform(get("/rsus?timDepositEnabledOnly=true"));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetAll_RsuWithNullGeographyAndRsuOption() throws Exception {
        // Arrange - RSU with null geography and rsuOption (optional relations)
        // Note: snmpCredential and snmpProtocol are required by RsuDto validation, so they must be provided
        SnmpProtocol snmpProtocol = new SnmpProtocol();
        snmpProtocol.setId(1);
        snmpProtocol.setProtocolCode("NTCIP1218");

        SnmpCredential snmpCredential = new SnmpCredential();
        snmpCredential.setId(1);
        snmpCredential.setUsername("testuser");
        snmpCredential.setPassword("testpass");

        Rsu rsu = Rsu.builder()
                .id(1)
                .ipv4Address(InetAddress.getByName("10.0.0.1"))
                .snmpCredential(snmpCredential)
                .snmpProtocol(snmpProtocol)
                .geography(null)
                .rsuOption(null)
                .build();
        when(rsuRepository.findAll()).thenReturn(List.of(rsu));

        // Act
        ResultActions resultActions = mockMvc.perform(get("/rsus"));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].ipv4Address").value("10.0.0.1"))
                .andExpect(jsonPath("$[0].snmpProtocol").value("NTCIP1218"))
                .andExpect(jsonPath("$[0].snmpUsername").value("testuser"))
                .andExpect(jsonPath("$[0].snmpPassword").value("testpass"))
                .andExpect(jsonPath("$[0].authenticationProtocol").value("SHA"))
                .andExpect(jsonPath("$[0].privacyProtocol").value("AES"))
                // When geography is null, mapper returns 0.0 for latitude and longitude
                .andExpect(jsonPath("$[0].latitude").value(0.0))
                .andExpect(jsonPath("$[0].longitude").value(0.0))
                // When rsuOption is null, mapper returns false for timDepositEnabled
                .andExpect(jsonPath("$[0].timDepositEnabled").value(false));
    }

    @Test
    void testGetAll_RsuWithMinimalRequiredFields() throws Exception {
        // Arrange - RSU with all required fields for RsuDto validation
        // id and ipv4Address are @NotBlank, so they must be provided
        SnmpProtocol snmpProtocol = new SnmpProtocol();
        snmpProtocol.setId(1);
        snmpProtocol.setProtocolCode("NTCIP1218");

        SnmpCredential snmpCredential = new SnmpCredential();
        snmpCredential.setId(1);
        snmpCredential.setUsername("user");
        snmpCredential.setPassword("pass");

        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(-100.0, 40.0);
        Point point = geometryFactory.createPoint(coordinate);

        RsuOption rsuOption = new RsuOption();
        rsuOption.setTimDeposit(true);

        Rsu rsu = Rsu.builder()
                .id(1)
                .ipv4Address(InetAddress.getByName("192.168.1.1"))
                .snmpProtocol(snmpProtocol)
                .snmpCredential(snmpCredential)
                .geography(point)
                .rsuOption(rsuOption)
                .build();
        when(rsuRepository.findAll()).thenReturn(List.of(rsu));

        // Act
        ResultActions resultActions = mockMvc.perform(get("/rsus"));

        // Assert - all required fields are present per RsuDto validation
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].ipv4Address").value("192.168.1.1"))
                .andExpect(jsonPath("$[0].snmpProtocol").value("NTCIP1218"))
                .andExpect(jsonPath("$[0].snmpUsername").value("user"))
                .andExpect(jsonPath("$[0].snmpPassword").value("pass"))
                .andExpect(jsonPath("$[0].latitude").value(40.0))
                .andExpect(jsonPath("$[0].longitude").value(-100.0))
                .andExpect(jsonPath("$[0].timDepositEnabled").value(true));
    }

    @Test
    void testPostNotAllowed() throws Exception {
        // Act
        ResultActions resultActions = mockMvc.perform(post("/rsus"));

        // Assert
        resultActions.andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testGetAll_InvalidTimDepositEnabledParam() throws Exception {
        // Act
        ResultActions resultActions = mockMvc.perform(get("/rsus?timDepositEnabledOnly=notaboolean"));

        // Assert
        resultActions.andExpect(status().isBadRequest());
    }

    // ==================== Test helpers ====================

    List<Rsu> getMockData() throws UnknownHostException {
        List<Rsu> rsus = new java.util.ArrayList<>();

        SnmpProtocol snmpProtocol = new SnmpProtocol();
        snmpProtocol.setId(1);
        snmpProtocol.setProtocolCode("NTCIP1218");

        SnmpCredential snmpCredential1 = new SnmpCredential();
        snmpCredential1.setId(1);
        snmpCredential1.setUsername("myusername");
        snmpCredential1.setPassword("mypassword");

        SnmpCredential snmpCredential2 = new SnmpCredential();
        snmpCredential2.setId(2);
        snmpCredential2.setUsername("myusername2");
        snmpCredential2.setPassword("mypassword2");

        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate1 = new Coordinate(-104.9847, 39.73915);
        Point point1 = geometryFactory.createPoint(coordinate1);

        Coordinate coordinate2 = new Coordinate(105.0, 40.0);
        Point point2 = geometryFactory.createPoint(coordinate2);

        RsuOption rsuOption = new RsuOption();
        rsuOption.setTimDeposit(true);

        Rsu rsu1 = Rsu.builder()
                .id(1)
                .ipv4Address(InetAddress.getByName("10.10.10.10"))
                .snmpProtocol(snmpProtocol)
                .snmpCredential(snmpCredential1)
                .geography(point1)
                .rsuOption(rsuOption)
                .build();
        rsus.add(rsu1);

        Rsu rsu2 = Rsu.builder()
                .id(2)
                .ipv4Address(InetAddress.getByName("10.10.10.11"))
                .snmpProtocol(snmpProtocol)
                .snmpCredential(snmpCredential2)
                .geography(point2)
                .rsuOption(rsuOption)
                .build();
        rsus.add(rsu2);

        return rsus;
    }
}
