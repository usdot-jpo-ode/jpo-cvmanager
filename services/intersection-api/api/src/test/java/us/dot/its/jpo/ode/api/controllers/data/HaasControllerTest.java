package us.dot.its.jpo.ode.api.controllers.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.ode.api.accessors.haas.HaasLocationDataRepository;
import us.dot.its.jpo.ode.api.models.PaginatedGeoJsonResponse;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.mockdata.MockHaasGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureEmbeddedDatabase
@ActiveProfiles("test")
public class HaasControllerTest {

    private final HaasController controller;

    @MockBean
    HaasLocationDataRepository haasLocationDataRepository;

    @MockBean
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

        PageRequest page = PageRequest.of(0, 10000);
        when(haasLocationDataRepository.find(true, null, null, page))
                .thenReturn(new PageImpl<>(locations, page, 1L));

        ResponseEntity<PaginatedGeoJsonResponse> result = controller
                .getLocations(true, null, null, false, 0, 10000, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getData().getFeatures()).hasSize(1);
    }
}