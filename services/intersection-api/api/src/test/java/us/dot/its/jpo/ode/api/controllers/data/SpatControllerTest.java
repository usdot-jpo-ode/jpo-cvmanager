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
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepository;
import us.dot.its.jpo.ode.api.controllers.data.SpatController;
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
        ProcessedSpat spat = MockSpatGenerator.getProcessedSpats().getFirst();

        List<ProcessedSpat> spats = new ArrayList<>();
        spats.add(spat);

        when(permissionService.hasIntersection(spat.getIntersectionId(), "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);

        PageRequest page = PageRequest.of(1, 1);
        when(processedSpatRepo.find(spat.getIntersectionId(),
                spat.getUtcTimeStamp().toEpochSecond() - 1,
                spat.getUtcTimeStamp().toEpochSecond() + 1, false, PageRequest.of(1, 1)))
                .thenReturn(new PageImpl<>(spats, page, 1L));

        ResponseEntity<Page<ProcessedSpat>> result = controller
                .findSpats(
                        spat.getIntersectionId(),
                        spat.getUtcTimeStamp().toEpochSecond() - 1,
                        spat.getUtcTimeStamp().toEpochSecond() + 1, false, false, 1, 1, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(spats);
    }
}