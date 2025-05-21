package us.dot.its.jpo.ode.api;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.controllers.data.MapController;
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
        ProcessedMap<LineString> map = MockMapGenerator.getProcessedMaps().getFirst();

        List<ProcessedMap<LineString>> maps = new ArrayList<>();
        maps.add(map);

        when(permissionService.hasIntersection(map.getProperties().getIntersectionId(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(processedMapRepo.find(map.getProperties().getIntersectionId(),
                map.getProperties().getTimeStamp().toEpochSecond() - 1,
                map.getProperties().getTimeStamp().toEpochSecond() + 1, false, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(maps, page, 1L));

        ResponseEntity<Page<ProcessedMap<LineString>>> result = controller
                .findMaps(
                        map.getProperties().getIntersectionId(),
                        map.getProperties().getTimeStamp().toEpochSecond() - 1,
                        map.getProperties().getTimeStamp().toEpochSecond() + 1, false, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(maps);
    }
}