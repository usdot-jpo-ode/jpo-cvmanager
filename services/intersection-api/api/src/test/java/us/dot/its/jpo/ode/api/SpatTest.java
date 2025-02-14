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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepository;
import us.dot.its.jpo.ode.api.controllers.SpatController;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.mockdata.MockSpatGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureEmbeddedDatabase
@ActiveProfiles("test")
public class SpatTest {

    private final SpatController controller;

    @MockBean
    ProcessedSpatRepository processedSpatRepo;

    @MockBean
    PermissionService permissionService;

    @Autowired
    public SpatTest(SpatController controller) {
        this.controller = controller;
    }

    @Test
    public void testProcessedSpat() {
        List<ProcessedSpat> list = MockSpatGenerator.getProcessedSpats();

        List<Integer> allowedInteresections = new ArrayList<>();
        allowedInteresections.add(null);

        when(permissionService.hasIntersection(null, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        Query query = processedSpatRepo.getQuery(null, null, null, false, false);
        when(processedSpatRepo.findProcessedSpats(query)).thenReturn(list);

        ResponseEntity<List<ProcessedSpat>> result = controller.findSpats(null, null, null, false, false, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(list);
    }
}