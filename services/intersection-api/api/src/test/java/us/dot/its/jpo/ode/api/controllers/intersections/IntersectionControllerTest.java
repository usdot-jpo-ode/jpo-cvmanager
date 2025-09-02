package us.dot.its.jpo.ode.api.controllers.intersections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.models.IntersectionReferenceData;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.PostgresService;

public class IntersectionControllerTest {

    private IntersectionController controller;

    @Mock
    ProcessedMapRepository processedMapRepo;

    @Mock
    PostgresService postgresService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new IntersectionController(
                processedMapRepo,
                postgresService);
    }

    @Test
    void testGetIntersectionsTestData() {
        ResponseEntity<List<IntersectionReferenceData>> response = controller.getIntersections(null, true);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getIntersectionID()).isEqualTo(12109);
    }

    @Test
    void testGetIntersectionsByOrganization() {
        IntersectionReferenceData intersection = new IntersectionReferenceData();
        intersection.setIntersectionID(1);
        List<IntersectionReferenceData> allIntersections = Collections.singletonList(intersection);

        when(processedMapRepo.getIntersectionIDs()).thenReturn(allIntersections);
        when(postgresService.getAllowedIntersectionIdsByOrganization("org"))
                .thenReturn(Collections.singletonList(1));

        ResponseEntity<List<IntersectionReferenceData>> response = controller.getIntersections("org", false);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(intersection);
    }

    @Test
    void testGetIntersectionsByEmail() {
        IntersectionReferenceData intersection = new IntersectionReferenceData();
        intersection.setIntersectionID(2);
        List<IntersectionReferenceData> allIntersections = Arrays.asList(intersection);

        when(processedMapRepo.getIntersectionIDs()).thenReturn(allIntersections);

        String username = "testuser";
        try (MockedStatic<PermissionService> mockedStatic = Mockito.mockStatic(PermissionService.class)) {
            mockedStatic.when(() -> PermissionService.getUsername(any())).thenReturn(username);
            when(postgresService.getAllowedIntersectionIdsByEmail(username))
                    .thenReturn(Collections.singletonList(2));

            ResponseEntity<List<IntersectionReferenceData>> response = controller.getIntersections(null, false);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains(intersection);
        }
    }

    @Test
    void testGetIntersectionsByLocationTestData() {
        ResponseEntity<List<IntersectionReferenceData>> response = controller.getIntersectionsByLocation(null, 0.0, 0.0,
                true);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getIntersectionID()).isEqualTo(12109);
    }

    @Test
    void testGetIntersectionsByLocationByOrganization() {
        IntersectionReferenceData intersection = new IntersectionReferenceData();
        intersection.setIntersectionID(3);
        List<IntersectionReferenceData> allIntersections = Collections.singletonList(intersection);

        when(processedMapRepo.getIntersectionsContainingPoint(1.0, 2.0)).thenReturn(allIntersections);
        when(postgresService.getAllowedIntersectionIdsByOrganization("org"))
                .thenReturn(Collections.singletonList(3));

        ResponseEntity<List<IntersectionReferenceData>> response = controller.getIntersectionsByLocation("org", 1.0,
                2.0, false);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(intersection);
    }

    @Test
    void testGetIntersectionsByLocationByEmail() {
        IntersectionReferenceData intersection = new IntersectionReferenceData();
        intersection.setIntersectionID(4);
        List<IntersectionReferenceData> allIntersections = Arrays.asList(intersection);

        when(processedMapRepo.getIntersectionsContainingPoint(5.0, 6.0)).thenReturn(allIntersections);

        String username = "testuser2";
        try (MockedStatic<PermissionService> mockedStatic = Mockito.mockStatic(PermissionService.class)) {
            mockedStatic.when(() -> PermissionService.getUsername(any())).thenReturn(username);
            when(postgresService.getAllowedIntersectionIdsByEmail(username))
                    .thenReturn(Collections.singletonList(4));

            ResponseEntity<List<IntersectionReferenceData>> response = controller.getIntersectionsByLocation(null, 5.0,
                    6.0, false);
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            assertThat(response.getBody()).contains(intersection);
        }
    }
}