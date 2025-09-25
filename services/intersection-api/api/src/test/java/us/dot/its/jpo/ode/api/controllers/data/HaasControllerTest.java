package us.dot.its.jpo.ode.api.controllers.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.ode.api.accessors.haas.HaasLocationDataRepository;
import us.dot.its.jpo.ode.api.models.LimitedGeoJsonResponse;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;
import us.dot.its.jpo.ode.api.models.haas.HaasLocationResult;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.mockdata.MockHaasGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureEmbeddedDatabase
@ActiveProfiles("test")
public class HaasControllerTest {

    private final HaasController controller;

    @MockitoBean
    HaasLocationDataRepository haasLocationDataRepository;

    @MockitoBean
    PermissionService permissionService;

    @Autowired
    public HaasControllerTest(HaasController controller) {
        this.controller = controller;
    }

    @Test
    public void testGetLocations() {
        HaasLocation location = MockHaasGenerator.getHaasLocations().getFirst();

        List<HaasLocation> locations = new ArrayList<>();
        locations.add(location);

        when(permissionService.isSuperUser()).thenReturn(true);

        HaasLocationResult mockResult = new HaasLocationResult(locations, false);
        when(haasLocationDataRepository.findWithLimit(true, null, null, 1000))
                .thenReturn(mockResult);

        ResponseEntity<LimitedGeoJsonResponse> result = controller
                .getLocations(true, null, null, false, 1000);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getData().getFeatures()).hasSize(1);
        assertThat(result.getBody().getMetadata().isTruncated()).isFalse();
    }

    @Test
    public void testGetLocationsWithTruncation() {
        HaasLocation location = MockHaasGenerator.getHaasLocations().getFirst();

        List<HaasLocation> locations = new ArrayList<>();
        locations.add(location);

        when(permissionService.isSuperUser()).thenReturn(true);

        HaasLocationResult mockResult = new HaasLocationResult(locations, true);
        when(haasLocationDataRepository.findWithLimit(true, null, null, 1))
                .thenReturn(mockResult);

        ResponseEntity<LimitedGeoJsonResponse> result = controller
                .getLocations(true, null, null, false, 1);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getData().getFeatures()).hasSize(1);
        assertThat(result.getBody().getMetadata().isTruncated()).isTrue();
        assertThat(result.getBody().getMetadata().getLimit()).isEqualTo(1);
        assertThat(result.getBody().getMetadata().getReturnedCount()).isEqualTo(1);
    }
}