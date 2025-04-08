package us.dot.its.jpo.ode.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.controllers.MapController;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.mockdata.MockMapGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class MapTest {

    private final MapController controller;

    @MockBean
    ProcessedMapRepository processedMapRepo;

    @MockBean
    PermissionService permissionService;

    @Autowired
    public MapTest(MapController controller) {
        this.controller = controller;
    }

    @Test
    public void testProcessedMap() {
        when(permissionService.hasIntersection(null, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        List<ProcessedMap<LineString>> list = MockMapGenerator.getProcessedMaps();

        Query query = processedMapRepo.getQuery(null, null, null, false, false);
        when(processedMapRepo.findProcessedMaps(query)).thenReturn(list);

        ResponseEntity<List<ProcessedMap<LineString>>> result = controller.findMaps(null, null, null, false, false,
                false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(list);
    }
}