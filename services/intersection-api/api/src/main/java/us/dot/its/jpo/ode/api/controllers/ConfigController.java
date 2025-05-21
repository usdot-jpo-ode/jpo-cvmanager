package us.dot.its.jpo.ode.api.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import us.dot.its.jpo.conflictmonitor.monitor.models.config.Config;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfig;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfigMap;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.IntersectionConfig;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.IntersectionConfigMap;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.accessors.config.DefaultConfig.DefaultConfigRepository;
import us.dot.its.jpo.ode.api.accessors.config.IntersectionConfig.IntersectionConfigRepository;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.PostgresService;

import org.springframework.http.MediaType;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Bad Request - Request body did not match expected format"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@RequestMapping("/intersections/configuration")
public class ConfigController {

    private final DefaultConfigRepository defaultConfigRepository;
    private final IntersectionConfigRepository intersectionConfigRepository;
    private final ConflictMonitorApiProperties props;
    private final PostgresService postgresService;
    private final PermissionService permissionService;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String defaultConfigTemplate = "%s/config/default/%s";
    private final String intersectionConfigTemplate = "%s/config/intersection/%s/%s";
    private final String defaultConfigAllTemplate = "%s/config/defaults";
    private final String intersectionConfigAllTemplate = "%s/config/intersections";

    @Autowired
    public ConfigController(
            DefaultConfigRepository defaultConfigRepository,
            IntersectionConfigRepository intersectionConfigRepository,
            ConflictMonitorApiProperties props,
            PostgresService postgresService,
            PermissionService permissionService) {
        this.defaultConfigRepository = defaultConfigRepository;
        this.intersectionConfigRepository = intersectionConfigRepository;
        this.props = props;
        this.postgresService = postgresService;
        this.permissionService = permissionService;
    }

    @Operation(summary = "Set Default Config", description = "Set a default configuration parameter, this will change this parameter on all non-overridden intersections. Requires SUPER_USER permissions.")
    @PostMapping(value = "/default", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser()")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER"),
            @ApiResponse(responseCode = "404", description = "Configuration setting not found"),
    })
    public @ResponseBody <T> ResponseEntity<DefaultConfig<?>> default_config(@RequestBody DefaultConfig<T> config) {
        try {
            String resourceURL = String.format(defaultConfigTemplate, props.getCmServerURL(), config.getKey());

            // Request does not require authentication, ConflictMonitor API is only
            // accessible internally
            @SuppressWarnings("rawtypes")
            ResponseEntity<DefaultConfig> response = restTemplate.getForEntity(resourceURL, DefaultConfig.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                DefaultConfig<T> previousConfig = response.getBody();
                previousConfig.setValue(config.getValue());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<DefaultConfig<T>> requestEntity = new HttpEntity<>(previousConfig, headers);

                restTemplate.postForEntity(resourceURL, requestEntity, DefaultConfig.class);
                defaultConfigRepository.save(previousConfig);
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                        .body(previousConfig);
            } else {
                throw new ResponseStatusException(response.getStatusCode(),
                        "Conflict Monitor API was unable to change setting on conflict monitor.");
            }
        } catch (Exception e) {
            log.error("Failed to set default config param", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unable to identify Message Type from ASN.1");
        }
    }

    @Operation(summary = "Create or Modify Intersection Config Parameter Overrides", description = "Create or modify an overridden intersection parameter. Requires SUPER_USER or OPERATOR permissions")
    @PostMapping(value = "/intersection", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('OPERATOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or OPERATOR role with access to the intersection requested"),
            @ApiResponse(responseCode = "404", description = "Configuration setting not found to modify/override"),
    })
    public @ResponseBody <T> ResponseEntity<IntersectionConfig<T>> intersection_config(
            @RequestBody IntersectionConfig<T> config) {
        if (!permissionService.hasIntersection(config.getIntersectionID(), "OPERATOR")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User does not have permission to delete intersection configuration parameters");
        }
        try {
            String resourceURL = String.format(intersectionConfigTemplate, props.getCmServerURL(),
                    config.getIntersectionID(), config.getKey());
            @SuppressWarnings("rawtypes")
            ResponseEntity<IntersectionConfig> response = restTemplate.getForEntity(resourceURL,
                    IntersectionConfig.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                IntersectionConfig<T> previousConfig = response.getBody();

                if (previousConfig == null) {
                    previousConfig = config;
                } else {
                    previousConfig.setValue(config.getValue());
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<IntersectionConfig<?>> requestEntity = new HttpEntity<>(previousConfig, headers);

                restTemplate.postForEntity(resourceURL, requestEntity, IntersectionConfig.class);

                intersectionConfigRepository.save(previousConfig);
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                        .body(previousConfig);
            } else {
                log.error("Failed error code returned from ConflictMonitor API: {}, with response: {}",
                        response.getStatusCode(), response.getBody().toString());
                throw new ResponseStatusException(response.getStatusCode(),
                        "Conflict Monitor API was unable to change setting on conflict monitor");
            }
        } catch (Exception e) {
            log.error("Failed to set intersection config param", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format(
                            "Exception updating intersection configuration parameter: %s", e.getMessage()),
                    e);
        }
    }

    @Operation(summary = "Delete Intersection Config Parameter", description = "Delete an intersection parameter override. Requires SUPER_USER or OPERATOR permissions.")
    @DeleteMapping(value = "/intersection", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('OPERATOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or OPERATOR role with access to the intersection requested"),
            @ApiResponse(responseCode = "404", description = "Configuration setting override not found to delete"),
    })
    public @ResponseBody ResponseEntity<String> intersection_config_delete(@RequestBody IntersectionConfig<?> config) {
        if (!permissionService.hasIntersection(config.getIntersectionID(), "OPERATOR")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User does not have permission to delete intersection configuration parameters");
        }

        Query query = intersectionConfigRepository.getQuery(config.getKey(), config.getIntersectionID());

        try {
            String resourceURL = String.format(intersectionConfigTemplate, props.getCmServerURL(),
                    config.getIntersectionID(), config.getKey());
            restTemplate.delete(resourceURL);
            intersectionConfigRepository.delete(query);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format(
                            "Exception deleting intersection configuration parameter: %s", e.getMessage()),
                    e);
        }
    }

    @Operation(summary = "Retrieve All Default Config Parameters", description = "Retrieve all default configuration parameters")
    @RequestMapping(value = "/default/all", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role"),
    })
    public @ResponseBody ResponseEntity<List<DefaultConfig<?>>> default_config_all() {

        String resourceURL = String.format(defaultConfigAllTemplate, props.getCmServerURL());
        ResponseEntity<DefaultConfigMap> response = restTemplate.getForEntity(resourceURL, DefaultConfigMap.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            DefaultConfigMap configMap = response.getBody();
            List<DefaultConfig<?>> results = new ArrayList<>(configMap.values());
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(results);
        } else {
            throw new ResponseStatusException(response.getStatusCode(),
                    String.format("The ConflictMonitor API was unable to retrieve default configuration parameters: ",
                            response.getBody()));
        }
    }

    @Operation(summary = "Retrieve All Overridden Intersection Config Parameters", description = "Retrieve all overridden intersection configuration parameters")
    @RequestMapping(value = "/intersection/all", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role"),
    })
    public @ResponseBody ResponseEntity<List<IntersectionConfig<?>>> intersection_config_all(
            @RequestHeader(name = "Organization", required = false) String organization) {

        String resourceURL = String.format(intersectionConfigAllTemplate, props.getCmServerURL());
        ResponseEntity<IntersectionConfigMap> response = restTemplate.getForEntity(resourceURL,
                IntersectionConfigMap.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            IntersectionConfigMap configMap = response.getBody();
            ArrayList<IntersectionConfig<?>> results = new ArrayList<>(configMap.listConfigs());

            if (organization == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = PermissionService.getUsername(auth);
                List<Integer> allowedIntersectionIds = postgresService.getAllowedIntersectionIdsByEmail(username);
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(results
                        .stream()
                        .filter(intersection -> allowedIntersectionIds.contains(intersection.getIntersectionID()))
                        .collect(Collectors.toList()));
            } else {
                List<Integer> allowedIntersectionIds = postgresService
                        .getAllowedIntersectionIdsByOrganization(organization);
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(results
                        .stream()
                        .filter(intersection -> allowedIntersectionIds.contains(intersection.getIntersectionID()))
                        .collect(Collectors.toList()));
            }
        } else {
            throw new ResponseStatusException(response.getStatusCode(),
                    String.format(
                            "The ConflictMonitor API was unable to retrieve overridden configuration parameters: ",
                            response.getBody()));
        }
    }

    @Operation(summary = "Retrieve All Unique Intersection Config Parameters", description = "Retrieve all intersection configuration parameters, showing defaults where no override exists, otherwise showing the overridden parameter")
    @RequestMapping(value = "/intersection/unique", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER, or USER role"),
    })
    public @ResponseBody ResponseEntity<List<Config<?>>> intersection_config_unique(
            @RequestParam(name = "intersection_id", required = true) int intersectionID) {

        // Query Default Configuration
        String defaultResourceURL = String.format(defaultConfigAllTemplate, props.getCmServerURL());
        List<DefaultConfig<?>> defaultList = new ArrayList<>();
        ResponseEntity<DefaultConfigMap> defaultConfigResponse = restTemplate.getForEntity(defaultResourceURL,
                DefaultConfigMap.class);
        if (defaultConfigResponse.getStatusCode().is2xxSuccessful()) {
            DefaultConfigMap configMap = defaultConfigResponse.getBody();
            defaultList = new ArrayList<>(configMap.values());
        }

        // Query Intersection Configuration
        List<IntersectionConfig<?>> intersectionList = new ArrayList<>();
        String intersectionResourceURL = String.format(intersectionConfigAllTemplate, props.getCmServerURL());
        ResponseEntity<IntersectionConfigMap> intersectionConfigResponse = restTemplate
                .getForEntity(intersectionResourceURL, IntersectionConfigMap.class);
        if (intersectionConfigResponse.getStatusCode().is2xxSuccessful()) {
            IntersectionConfigMap configMap = intersectionConfigResponse.getBody();
            ArrayList<IntersectionConfig<?>> results = new ArrayList<>(configMap.listConfigs());

            for (IntersectionConfig<?> config : results) {
                if (config.getIntersectionID() == intersectionID) {
                    intersectionList.add(config);
                }
            }
        }

        List<Config<?>> finalConfig = new ArrayList<>();
        for (DefaultConfig<?> defaultConfig : defaultList) {
            Config<?> addConfig = defaultConfig;
            for (IntersectionConfig<?> intersectionConfig : intersectionList) {
                if (intersectionConfig.getKey().equals(defaultConfig.getKey())) {
                    addConfig = intersectionConfig;
                    break;
                }
            }
            finalConfig.add(addConfig);
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(finalConfig);
    }
}