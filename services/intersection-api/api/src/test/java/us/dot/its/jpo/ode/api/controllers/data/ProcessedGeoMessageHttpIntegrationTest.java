package us.dot.its.jpo.ode.api.controllers.data;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import us.dot.its.jpo.ode.api.TestcontainersConfiguration;
import us.dot.its.jpo.ode.api.accessors.geo.ProcessedGeoMessageRepository;

/** Exercises the HTTP route with the production JWT converter and PermissionService. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProcessedGeoMessageHttpIntegrationTest {
    private static final String USER_TOKEN = "geo-query-user-token";
    private static final String SUPER_TOKEN = "geo-query-super-token";
    private static final String NO_ROLE_TOKEN = "geo-query-no-role-token";
    private static final String INVALID_TOKEN = "geo-query-invalid-token";
    private static final String URL = "/rsu-geo-msg-data";
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

    @BeforeEach
    void stubJwtDecoder() {
        when(jwtDecoder.decode(USER_TOKEN)).thenReturn(jwt(USER_TOKEN, false, true));
        when(jwtDecoder.decode(SUPER_TOKEN)).thenReturn(jwt(SUPER_TOKEN, true, false));
        when(jwtDecoder.decode(NO_ROLE_TOKEN)).thenReturn(jwt(NO_ROLE_TOKEN, false, false));
        when(jwtDecoder.decode(INVALID_TOKEN)).thenThrow(new BadJwtException("invalid token"));
    }

    @Test
    void userAndSuperUserCanQueryWithOrWithoutOptionalOrganizationHeader() throws Exception {
        when(repository.find(anyString(), anyList(), anyString(), anyString()))
                .thenAnswer(invocation -> Stream.of(feature()));

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("Feature"))
                .andExpect(jsonPath("$[0].properties.id").value("message-1"));

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                        .header("Organization", "TestOrg")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].properties.id").value("message-1"));

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + SUPER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk());

        verify(repository, times(3)).find(
                eq("ProcessedBsm"),
                eq(List.of(
                        List.of(-106.0, 39.0),
                        List.of(-104.0, 39.0),
                        List.of(-104.0, 41.0),
                        List.of(-106.0, 41.0),
                        List.of(-106.0, 39.0))),
                eq("2024-01-01T00:00:00Z"),
                eq("2024-01-01T01:00:00Z"));
    }

    @Test
    void absentOrInvalidBearerTokenUsesSharedSecurityResponse() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());

        verify(repository, never()).find(anyString(), anyList(), anyString(), anyString());
    }

    @Test
    void insufficientRoleAndOrganizationMismatchAreForbidden() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + NO_ROLE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                        .header("Organization", "OtherOrg")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());

        verify(repository, never()).find(anyString(), anyList(), anyString(), anyString());
    }

    @Test
    void corsPreflightAllowsPostAndAuthorizationHeader() throws Exception {
        mockMvc.perform(options(URL)
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, org.hamcrest.Matchers.containsString("POST")));
    }

    @Test
    void malformedAndInvalidRequestsReturnProblemDetails400() throws Exception {
        authorizeUser();

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        String unsupportedType = BODY.replace("\"BSM\"", "\"TIM\"");
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unsupportedType))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(repository, never()).find(anyString(), anyList(), anyString(), anyString());
    }

    @Test
    void mongoAvailabilityAndUnexpectedFailuresReturnProblemDetails503And500() throws Exception {
        authorizeUser();
        when(repository.find(anyString(), anyList(), anyString(), anyString()))
                .thenThrow(new DataAccessResourceFailureException("private mongo endpoint detail"))
                .thenThrow(new IllegalStateException("private processing detail"));

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(content().string(not(containsString("private mongo endpoint detail"))));

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(content().string(not(containsString("private processing detail"))));
    }

    private void authorizeUser() {
        when(jwtDecoder.decode(USER_TOKEN)).thenReturn(jwt(USER_TOKEN, false, true));
    }

    private static Jwt jwt(String token, boolean superUser, boolean withUserRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "http://localhost:8084/realms/cvmanager");
        claims.put("sub", token);
        claims.put("email", "geo-query@example.com");
        claims.put("cvmanager_data", Map.of(
                "super_user", superUser,
                "organizations", withUserRole
                        ? List.of(Map.of(
                                "org_id", 7,
                                "org_name", "TestOrg",
                                "org_email", "test-org@example.com",
                                "role", "user"))
                        : List.of()));
        return Jwt.withTokenValue(token)
                .header("alg", "RS256")
                .issuedAt(Instant.now().minusSeconds(30))
                .expiresAt(Instant.now().plusSeconds(600))
                .claims(values -> values.putAll(claims))
                .build();
    }

    private static Document feature() {
        return new Document("type", "Feature")
                .append("geometry", new Document("type", "Point")
                        .append("coordinates", List.of(-105.0, 40.0)))
                .append("properties", new Document("schemaVersion", 2)
                        .append("id", "message-1")
                        .append("timeStamp", "2024-01-01T00:00:10Z")
                        .append("messageType", "BSM"));
    }
}
