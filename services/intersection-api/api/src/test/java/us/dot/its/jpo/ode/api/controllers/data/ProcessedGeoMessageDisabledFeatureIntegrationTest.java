package us.dot.its.jpo.ode.api.controllers.data;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import us.dot.its.jpo.ode.api.TestcontainersConfiguration;
import us.dot.its.jpo.ode.api.accessors.geo.ProcessedGeoMessageRepository;

/** Confirms the RSU feature flag gates this route after shared authentication. */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "geo-query.enabled=false")
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProcessedGeoMessageDisabledFeatureIntegrationTest {
    private static final String TOKEN = "geo-query-disabled-user-token";
    private static final String BODY = """
            {
              "geometry": [[-106,39],[-104,39],[-104,41],[-106,41],[-106,39]],
              "start": "2024-01-01T00:00:00Z",
              "end": "2024-01-01T01:00:00Z",
              "msg_type": "BSM"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private ProcessedGeoMessageRepository repository;

    @Test
    void authorizedRequestReturns501WithoutQueryingMongo() throws Exception {
        when(jwtDecoder.decode(TOKEN)).thenReturn(Jwt.withTokenValue(TOKEN)
                .header("alg", "RS256")
                .issuedAt(Instant.now().minusSeconds(30))
                .expiresAt(Instant.now().plusSeconds(600))
                .claim("iss", "http://localhost:8084/realms/cvmanager")
                .claim("sub", TOKEN)
                .claim("email", "geo-query@example.com")
                .claim("cvmanager_data", Map.of(
                        "super_user", true,
                        "organizations", List.of()))
                .build());

        mockMvc.perform(post("/rsu-geo-msg-data")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.status").value(501));

        verify(repository, never()).find(anyString(), anyList(), anyString(), anyString());
    }
}
