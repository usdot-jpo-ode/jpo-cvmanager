package us.dot.its.jpo.ode.api.controllers.intersections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.conflictmonitor.monitor.models.config.Config;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfig;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfigMap;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.IntersectionConfig;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.IntersectionConfigMap;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.accessors.config.default_config.DefaultConfigRepository;
import us.dot.its.jpo.ode.api.accessors.config.intersection_config.IntersectionConfigRepository;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.PostgresService;

public class ConfigControllerTest {

    private ConfigController controller;

    @Mock
    DefaultConfigRepository defaultConfigRepository;

    @Mock
    IntersectionConfigRepository intersectionConfigRepository;

    @Mock
    ConflictMonitorApiProperties props;

    @Mock
    PostgresService postgresService;

    @Mock
    PermissionService permissionService;

    @Mock
    RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ConfigController(
                defaultConfigRepository,
                intersectionConfigRepository,
                props,
                postgresService,
                permissionService);

        ReflectionTestUtils.setField(controller, "restTemplate", restTemplate);
        when(props.getCmServerURL()).thenReturn("http://localhost");
    }

    @Test
    void testDefaultConfigSuccess() {
        DefaultConfig<String> inputConfig = new DefaultConfig<>();
        inputConfig.setKey("testKey");
        inputConfig.setValue("newValue");

        DefaultConfig<String> previousConfig = new DefaultConfig<>();
        previousConfig.setKey("testKey");
        previousConfig.setValue("oldValue");

        String resourceURL = "http://localhost/config/default/testKey";
        when(props.getCmServerURL()).thenReturn("http://localhost");
        when(restTemplate.getForEntity(resourceURL, DefaultConfig.class))
                .thenReturn(new ResponseEntity(previousConfig, HttpStatus.OK));
        when(restTemplate.postForEntity(eq(resourceURL), any(HttpEntity.class), eq(DefaultConfig.class)))
                .thenReturn(new ResponseEntity(previousConfig, HttpStatus.OK));
        doNothing().when(defaultConfigRepository).save(any(DefaultConfig.class));

        ResponseEntity<DefaultConfig<?>> response = controller.default_config(inputConfig);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getKey()).isEqualTo("testKey");
        assertThat(response.getBody().getValue()).isEqualTo("newValue");
        verify(defaultConfigRepository, times(1)).save(any(DefaultConfig.class));
    }

    @Test
    void testDefaultConfigApiError() {
        DefaultConfig<String> inputConfig = new DefaultConfig<>();
        inputConfig.setKey("testKey");
        inputConfig.setValue("newValue");

        String resourceURL = "http://localhost/config/default/testKey";
        when(props.getCmServerURL()).thenReturn("http://localhost");
        when(restTemplate.getForEntity(eq(resourceURL), eq(DefaultConfig.class)))
                .thenReturn(new ResponseEntity(null, HttpStatus.NOT_FOUND));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            controller.default_config(inputConfig);
        });

        assertThat(ex.getReason()).contains("Conflict Monitor API was unable to change setting on conflict monitor.");
    }

    @Test
    void testDefaultConfigException() {
        DefaultConfig<String> inputConfig = new DefaultConfig<>();
        inputConfig.setKey("testKey");
        inputConfig.setValue("newValue");

        String resourceURL = "http://localhost/config/default/testKey";
        when(props.getCmServerURL()).thenReturn("http://localhost");
        when(restTemplate.getForEntity(resourceURL, DefaultConfig.class))
                .thenThrow(new RuntimeException("Some error"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            controller.default_config(inputConfig);
        });

        assertThat(ex.getReason()).isEqualTo("Unable to identify Message Type from ASN.1");
    }

    @Test
    void testIntersectionConfigSuccess() {
        IntersectionConfig<String> inputConfig = new IntersectionConfig<>();
        inputConfig.setIntersectionID(1);
        inputConfig.setKey("testKey");
        inputConfig.setValue("newValue");

        IntersectionConfig<String> previousConfig = new IntersectionConfig<>();
        previousConfig.setIntersectionID(1);
        previousConfig.setKey("testKey");
        previousConfig.setValue("oldValue");

        String resourceURL = "http://localhost/config/intersection/1/testKey";
        when(permissionService.hasIntersection(1, "OPERATOR")).thenReturn(true);
        when(restTemplate.getForEntity(resourceURL, IntersectionConfig.class))
                .thenReturn(new ResponseEntity(previousConfig, HttpStatus.OK));
        when(restTemplate.postForEntity(eq(resourceURL), any(HttpEntity.class), eq(IntersectionConfig.class)))
                .thenReturn(new ResponseEntity(previousConfig, HttpStatus.OK));
        doNothing().when(intersectionConfigRepository).save(any(IntersectionConfig.class));

        ResponseEntity<IntersectionConfig<String>> response = controller.intersection_config(inputConfig);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getKey()).isEqualTo("testKey");
        assertThat(response.getBody().getValue()).isEqualTo("newValue");
        verify(intersectionConfigRepository, times(1)).save(any(IntersectionConfig.class));
    }

    @Test
    void testIntersectionConfigForbidden() {
        IntersectionConfig<String> inputConfig = new IntersectionConfig<>();
        inputConfig.setIntersectionID(1);
        inputConfig.setKey("testKey");
        inputConfig.setValue("newValue");

        when(permissionService.hasIntersection(1, "OPERATOR")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            controller.intersection_config(inputConfig);
        });

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testIntersectionConfigApiError() {
        IntersectionConfig<String> inputConfig = new IntersectionConfig<>();
        inputConfig.setIntersectionID(1);
        inputConfig.setKey("testKey");
        inputConfig.setValue("newValue");

        String resourceURL = "http://localhost/config/intersection/1/testKey";
        when(permissionService.hasIntersection(1, "OPERATOR")).thenReturn(true);
        when(restTemplate.getForEntity(eq(resourceURL), eq(IntersectionConfig.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                        .body(inputConfig));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            controller.intersection_config(inputConfig);
        });

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testIntersectionConfigException() {
        IntersectionConfig<String> inputConfig = new IntersectionConfig<>();
        inputConfig.setIntersectionID(1);
        inputConfig.setKey("testKey");
        inputConfig.setValue("newValue");

        String resourceURL = "http://localhost/config/intersection/1/testKey";
        when(permissionService.hasIntersection(1, "OPERATOR")).thenReturn(true);
        when(restTemplate.getForEntity(eq(resourceURL), eq(IntersectionConfig.class)))
                .thenThrow(new RuntimeException("Some error"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            controller.intersection_config(inputConfig);
        });

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ex.getReason()).contains("Exception updating intersection configuration parameter");
    }

    @Test
    void testIntersectionConfigDeleteSuccess() {
        IntersectionConfig<String> inputConfig = new IntersectionConfig<>();
        inputConfig.setIntersectionID(1);
        inputConfig.setKey("testKey");

        Query query = mock(Query.class);
        String resourceURL = "http://localhost/config/intersection/1/testKey";
        when(permissionService.hasIntersection(1, "OPERATOR")).thenReturn(true);
        when(intersectionConfigRepository.getQuery("testKey", 1)).thenReturn(query);
        doNothing().when(restTemplate).delete(resourceURL);
        doNothing().when(intersectionConfigRepository).delete(query);

        ResponseEntity<String> response = controller.intersection_config_delete(inputConfig);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(intersectionConfigRepository, times(1)).delete(query);
    }

    @Test
    void testIntersectionConfigDeleteForbidden() {
        IntersectionConfig<String> inputConfig = new IntersectionConfig<>();
        inputConfig.setIntersectionID(1);
        inputConfig.setKey("testKey");

        when(permissionService.hasIntersection(1, "OPERATOR")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            controller.intersection_config_delete(inputConfig);
        });

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testIntersectionConfigDeleteException() {
        IntersectionConfig<String> inputConfig = new IntersectionConfig<>();
        inputConfig.setIntersectionID(1);
        inputConfig.setKey("testKey");

        Query query = mock(Query.class);
        String resourceURL = "http://localhost/config/intersection/1/testKey";
        when(permissionService.hasIntersection(1, "OPERATOR")).thenReturn(true);
        when(intersectionConfigRepository.getQuery("testKey", 1)).thenReturn(query);
        doThrow(new RuntimeException("delete error")).when(restTemplate).delete(resourceURL);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            controller.intersection_config_delete(inputConfig);
        });

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ex.getReason()).contains("Exception deleting intersection configuration parameter");
    }

    @Test
    void testDefaultConfigAllSuccess() {
        DefaultConfigMap configMap = new DefaultConfigMap();
        DefaultConfig<String> config1 = new DefaultConfig<>();
        config1.setKey("key1");
        config1.setValue("val1");
        configMap.put("key1", config1);

        String resourceURL = "http://localhost/config/defaults";
        when(restTemplate.getForEntity(resourceURL, DefaultConfigMap.class))
                .thenReturn(new ResponseEntity<>(configMap, HttpStatus.OK));

        ResponseEntity<List<DefaultConfig<?>>> response = controller.default_config_all();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(config1);
    }

    @Test
    void testDefaultConfigAllApiError() {
        String resourceURL = "http://localhost/config/defaults";
        when(restTemplate.getForEntity(resourceURL, DefaultConfigMap.class))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            controller.default_config_all();
        });

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testIntersectionConfigAllSuccessByUser() {
        IntersectionConfigMap configMap = new IntersectionConfigMap();
        IntersectionConfig<String> config1 = new IntersectionConfig<String>("key", "category", 0, 1, "overrideVal",
                "type",
                null, "description");
        configMap.putConfig(config1);

        String username = "testuser";
        String resourceURL = "http://localhost/config/intersections";
        when(restTemplate.getForEntity(resourceURL, IntersectionConfigMap.class))
                .thenReturn(new ResponseEntity<>(configMap, HttpStatus.OK));
        when(postgresService.getAllowedIntersectionIdsByEmail(eq(username)))
                .thenReturn(Collections.singletonList(1));

        try (MockedStatic<PermissionService> mockedStatic = Mockito.mockStatic(PermissionService.class)) {
            mockedStatic.when(() -> PermissionService.getUsername(any()))
                    .thenReturn(username);

            ResponseEntity<List<IntersectionConfig<?>>> response = controller.intersection_config_all(null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains(config1);
        }
    }

    @Test
    void testIntersectionConfigAllSuccessByOrg() {
        IntersectionConfigMap configMap = new IntersectionConfigMap();
        IntersectionConfig<String> config1 = new IntersectionConfig<String>("key", "category", 0, 1, "overrideVal",
                "type",
                null, "description");
        configMap.putConfig(config1);

        String resourceURL = "http://localhost/config/intersections";
        when(restTemplate.getForEntity(resourceURL, IntersectionConfigMap.class))
                .thenReturn(new ResponseEntity<>(configMap, HttpStatus.OK));
        when(postgresService.getAllowedIntersectionIdsByOrganization(eq("org")))
                .thenReturn(Collections.singletonList(1));

        ResponseEntity<List<IntersectionConfig<?>>> response = controller.intersection_config_all("org");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(config1);
    }

    @Test
    void testIntersectionConfigAllApiError() {
        String resourceURL = "http://localhost/config/intersections";
        when(restTemplate.getForEntity(resourceURL, IntersectionConfigMap.class))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            controller.intersection_config_all(null);
        });

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testIntersectionConfigUniqueSuccess() {
        DefaultConfigMap defaultConfigMap = new DefaultConfigMap();
        DefaultConfig<String> defaultConfig = new DefaultConfig<>();
        defaultConfig.setKey("key1");
        defaultConfig.setValue("defaultVal");
        defaultConfigMap.put("key1", defaultConfig);

        IntersectionConfigMap intersectionConfigMap = new IntersectionConfigMap();
        IntersectionConfig<String> config1 = new IntersectionConfig<String>("key", "category", 0, 1, "overrideVal",
                "type",
                null, "description");
        intersectionConfigMap.putConfig(config1);

        String defaultResourceURL = "http://localhost/config/defaults";
        String intersectionResourceURL = "http://localhost/config/intersections";
        when(restTemplate.getForEntity(defaultResourceURL, DefaultConfigMap.class))
                .thenReturn(new ResponseEntity<>(defaultConfigMap, HttpStatus.OK));
        when(restTemplate.getForEntity(intersectionResourceURL, IntersectionConfigMap.class))
                .thenReturn(new ResponseEntity<>(intersectionConfigMap, HttpStatus.OK));

        ResponseEntity<List<Config<?>>> response = controller.intersection_config_unique(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get(0).getValue()).isEqualTo("defaultVal");
    }

    @Test
    void testIntersectionConfigUniqueApiError() {
        String defaultResourceURL = "http://localhost/config/defaults";
        String intersectionResourceURL = "http://localhost/config/intersections";
        when(restTemplate.getForEntity(eq(defaultResourceURL), eq(DefaultConfigMap.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
        when(restTemplate.getForEntity(eq(intersectionResourceURL), eq(IntersectionConfigMap.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        ResponseEntity<List<Config<?>>> response = controller.intersection_config_unique(1);

        // Should return OK with empty list if default configs not found
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}
