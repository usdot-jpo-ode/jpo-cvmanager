package us.dot.its.jpo.ode.api.controllers.devices.rsus;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import us.dot.its.jpo.ode.api.TestcontainersConfiguration;
import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.geojson.GeoJsonPointDto;
import us.dot.its.jpo.ode.api.models.postgres.dtos.RsuGeoInfoDto;
import us.dot.its.jpo.ode.api.models.postgres.dtos.RsuGeoInfoPropertiesDto;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.RsuInfoService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Import(TestcontainersConfiguration.class)
class InfoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RsuInfoService rsuInfoService;

    @MockitoBean
    PermissionService permissionService;

    private RsuGeoInfoDto buildFeature(int rsuId) {
        RsuGeoInfoPropertiesDto props = new RsuGeoInfoPropertiesDto(
                rsuId,
                12.5,
                "10.0.0." + rsuId,
                "SN-" + rsuId,
                "I-999",
                true,
                false,
                "ITS RS4",
                "Commsignia");
        GeoJsonPointDto geometry = new GeoJsonPointDto(new double[] { -104.9903, 39.7392 });
        return new RsuGeoInfoDto(rsuId, geometry, props);
    }

    // --- GET /devices/rsus/info ---

    @Test
    void getRsuInfo_SuperUser_ReturnsOkWithRsuList() throws Exception {
        given(permissionService.isSuperUser()).willReturn(true);
        given(rsuInfoService.getRsuGeoInfoByOrganization("TestOrg"))
                .willReturn(List.of(buildFeature(1), buildFeature(2)));

        mockMvc.perform(get("/devices/rsus/info")
                .with(jwt())
                .header("Organization", "TestOrg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getRsuInfo_ReturnsCorrectGeoJsonStructure() throws Exception {
        given(permissionService.isSuperUser()).willReturn(true);
        given(rsuInfoService.getRsuGeoInfoByOrganization("TestOrg"))
                .willReturn(List.of(buildFeature(1)));

        mockMvc.perform(get("/devices/rsus/info")
                .with(jwt())
                .header("Organization", "TestOrg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("Feature"))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].geometry.type").value("Point"))
                .andExpect(jsonPath("$[0].geometry.coordinates[0]").value(-104.9903))
                .andExpect(jsonPath("$[0].geometry.coordinates[1]").value(39.7392))
                .andExpect(jsonPath("$[0].properties.rsu_id").value(1))
                .andExpect(jsonPath("$[0].properties.ipv4_address").value("10.0.0.1"))
                .andExpect(jsonPath("$[0].properties.serial_number").value("SN-1"))
                .andExpect(jsonPath("$[0].properties.primary_route").value("I-999"))
                .andExpect(jsonPath("$[0].properties.milepost").value(12.5))
                .andExpect(jsonPath("$[0].properties.tim_deposit").value(true))
                .andExpect(jsonPath("$[0].properties.snmp_monitoring").value(false))
                .andExpect(jsonPath("$[0].properties.model_name").value("ITS RS4"))
                .andExpect(jsonPath("$[0].properties.manufacturer_name").value("Commsignia"));
    }

    @Test
    void getRsuInfo_EmptyOrganization_ReturnsOkWithEmptyList() throws Exception {
        given(permissionService.isSuperUser()).willReturn(true);
        given(rsuInfoService.getRsuGeoInfoByOrganization("EmptyOrg"))
                .willReturn(List.of());

        mockMvc.perform(get("/devices/rsus/info")
                .with(jwt())
                .header("Organization", "EmptyOrg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getRsuInfo_HasUserRole_ReturnsOk() throws Exception {
        given(permissionService.isSuperUser()).willReturn(false);
        given(permissionService.hasRole(UserRole.USER)).willReturn(true);
        given(rsuInfoService.getRsuGeoInfoByOrganization("TestOrg"))
                .willReturn(List.of(buildFeature(5)));

        mockMvc.perform(get("/devices/rsus/info")
                .with(jwt())
                .header("Organization", "TestOrg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRsuInfo_MissingOrganizationHeader_ReturnsBadRequest() throws Exception {
        given(permissionService.isSuperUser()).willReturn(true);

        mockMvc.perform(get("/devices/rsus/info")
                .with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRsuInfo_Forbidden_ReturnsForbidden() throws Exception {
        given(permissionService.isSuperUser()).willReturn(false);
        given(permissionService.hasRole(UserRole.USER)).willReturn(false);

        mockMvc.perform(get("/devices/rsus/info")
                .with(jwt())
                .header("Organization", "TestOrg"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRsuInfo_Unauthenticated_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/devices/rsus/info")
                .header("Organization", "TestOrg"))
                .andExpect(status().isForbidden());
    }
}
