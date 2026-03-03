package com.trihydro.rsuinfobridge.controller;

import com.trihydro.rsuinfobridge.models.dtos.RsuDto;
import com.trihydro.rsuinfobridge.service.RsuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class RsuControllerTest {
    RsuService rsuService;
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        rsuService = mock(RsuService.class);
    }

    @Test
    void testGetAll_Success() throws Exception {
        // Arrange
        List<RsuDto> rsus = getMockData();
        when(rsuService.getAll(false)).thenReturn(rsus);
        mockMvc = initializeMockMvc();

        // Act
        ResultActions resultActions = mockMvc.perform(get("/rsus"));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].ipv4Address").value("10.10.10.10"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].ipv4Address").value("10.10.10.11"));
    }

    @Test
    void testGetAllWithTimDepositEnabled_Success() throws Exception {
        // Arrange
        List<RsuDto> rsus = getMockData();
        when(rsuService.getAll(true)).thenReturn(rsus);
        mockMvc = initializeMockMvc();

        // Act
        ResultActions resultActions = mockMvc.perform(get("/rsus?timDepositEnabledOnly=true"));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].timDepositEnabled").value(true))
                .andExpect(jsonPath("$[1].timDepositEnabled").value(true));
    }

    List<RsuDto> getMockData() {
        String AUTHENTICATION_PROTOCOL = "SHA";
        String PRIVACY_PROTOCOL = "AES";

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

    MockMvc initializeMockMvc() {
        return MockMvcBuilders.standaloneSetup(new RsuController(rsuService)).build();
    }
}
