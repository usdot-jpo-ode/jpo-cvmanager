package us.dot.its.jpo.ode.api.controllers.devices.rsus;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import us.dot.its.jpo.ode.api.TestcontainersConfiguration;
import us.dot.its.jpo.ode.api.models.devices.management.RsuUpgradeRequest;
import us.dot.its.jpo.ode.api.models.postgres.dtos.FirmwareUpgradeCheckResponseDto;
import us.dot.its.jpo.ode.api.models.postgres.dtos.FirmwareUpgradeResultDto;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.RsuUpgradeService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Import(TestcontainersConfiguration.class)
class UpgradeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    RsuUpgradeService rsuUpgradeService;

    @MockitoBean
    PermissionService permissionService;

    // POST /devices/rsus/upgrade (startUpgrade) Tests

    @Test
    void startUpgrade_Success() throws Exception {
        List<String> rsuIps = List.of("10.0.0.10", "10.0.0.11");
        Map<String, FirmwareUpgradeResultDto> serviceResponse = Map.of(
                "rsu1", new FirmwareUpgradeResultDto(200, "ok"));

        given(permissionService.isSuperUser()).willReturn(true);
        given(rsuUpgradeService.startFirmwareUpgradeForRsus(anyList())).willReturn(serviceResponse);

        RsuUpgradeRequest request = new RsuUpgradeRequest();
        request.setRsuIps(rsuIps);

        mockMvc.perform(post("/devices/rsus/upgrade")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rsu1.code").value(200));
    }

    @Test
    void startUpgrade_WithoutOrganizationHeader_ReturnsOk() throws Exception {
        given(permissionService.isSuperUser()).willReturn(true);
        given(rsuUpgradeService.startFirmwareUpgradeForRsus(anyList())).willReturn(Map.of());

        RsuUpgradeRequest request = new RsuUpgradeRequest();
        request.setRsuIps(List.of("10.0.0.10"));

        mockMvc.perform(post("/devices/rsus/upgrade")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void startUpgrade_EmptyRsuIpList_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/devices/rsus/upgrade")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rsu_ips\": []}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void startUpgrade_NullRsuIpField_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/devices/rsus/upgrade")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void startUpgrade_Forbidden_ReturnsForbidden() throws Exception {
        given(permissionService.isSuperUser()).willReturn(false);
        given(permissionService.hasRsus(any(), anyString())).willReturn(false);

        mockMvc.perform(post("/devices/rsus/upgrade")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rsu_ips\": [\"10.0.0.10\"]}"))
                .andExpect(status().isForbidden());
    }

    // POST /devices/rsus/upgrade/check (checkUpgrade) Tests

    @Test
    void checkUpgrade_Success() throws Exception {
        FirmwareUpgradeCheckResponseDto serviceResponse = new FirmwareUpgradeCheckResponseDto(
                true, 42L, "RSU Firmware v2.0", "2.0");

        given(permissionService.isSuperUser()).willReturn(true);
        given(rsuUpgradeService.checkFirmwareUpgrade(anyString())).willReturn(serviceResponse);

        mockMvc.perform(post("/devices/rsus/upgrade/check")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rsu_ip\": \"10.0.0.10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upgrade_available").value(true))
                .andExpect(jsonPath("$.upgrade_id").value(42))
                .andExpect(jsonPath("$.upgrade_name").value("RSU Firmware v2.0"))
                .andExpect(jsonPath("$.upgrade_version").value("2.0"));
    }

    @Test
    void checkUpgrade_WithoutOrganizationHeader_ReturnsOk() throws Exception {
        given(permissionService.isSuperUser()).willReturn(true);
        given(rsuUpgradeService.checkFirmwareUpgrade(anyString()))
                .willReturn(new FirmwareUpgradeCheckResponseDto(false, -1L, "", ""));

        mockMvc.perform(post("/devices/rsus/upgrade/check")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rsu_ip\": \"10.0.0.10\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void checkUpgrade_BlankRsuIp_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/devices/rsus/upgrade/check")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rsu_ip\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkUpgrade_Forbidden_ReturnsForbidden() throws Exception {
        given(permissionService.isSuperUser()).willReturn(false);
        given(permissionService.hasRsu(anyString(), anyString())).willReturn(false);

        mockMvc.perform(post("/devices/rsus/upgrade/check")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rsu_ip\": \"10.0.0.10\"}"))
                .andExpect(status().isForbidden());
    }
}
