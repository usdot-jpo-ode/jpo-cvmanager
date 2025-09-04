package us.dot.its.jpo.ode.api.controllers.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.asn.j2735.r2024.BasicSafetyMessage.BasicSafetyMessage;
import us.dot.its.jpo.ode.api.accessors.bsm.OdeBsmJsonRepository;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.mockdata.MockBsmGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class BsmControllerTest {

    private final BsmController controller;

    @MockitoBean
    OdeBsmJsonRepository odeBsmJsonRepo;

    @MockitoBean
    PermissionService permissionService;

    @Autowired
    public BsmControllerTest(BsmController controller) {
        this.controller = controller;
    }

    @Test
    public void testBsmJson() {

        when(permissionService.hasRole("USER")).thenReturn(true);

        List<BasicSafetyMessage> list = new ArrayList<>();

        PageRequest page = PageRequest.of(0, 1);
        when(odeBsmJsonRepo.find(null, null, null, null, null, null, null,
                PageRequest.of(0, 1)))
                .thenReturn(new PageImpl<>(list, page, 1L));

        ResponseEntity<Page<BasicSafetyMessage>> result = controller.findBSMs(null, null, null,
                null, null, null, null,
                0, 1,
                false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).isEqualTo(list);
    }

    @Test
    void testFindOdeBsmWithTestData() {
        when(permissionService.hasRole("USER")).thenReturn(true);
        boolean testData = true;

        ResponseEntity<Page<BasicSafetyMessage>> response = controller
                .findBSMs(null, null, null, null, null, null, null,
                        0, 10,
                        testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void testFindOdeBsmsWithPagination() {
        List<BasicSafetyMessage> events = MockBsmGenerator.getJsonBsms();

        when(permissionService.hasRole("USER")).thenReturn(true);

        Page<BasicSafetyMessage> mockPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        when(odeBsmJsonRepo.find(any(), any(), any(), any(), any(), any(), any(),
                any(PageRequest.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<BasicSafetyMessage>> response = controller
                .findBSMs(null, null, null, null, null, null, null,
                        0, 10,
                        false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEqualTo(events);
        verify(odeBsmJsonRepo, times(1))
                .find(any(), any(), any(), any(), any(), any(), any(), any(PageRequest.class));
    }

    @Test
    public void testCountOdeBsmsWithTestData() {
        boolean testData = true;

        when(permissionService.hasRole("USER")).thenReturn(true);

        ResponseEntity<Long> response = controller.countBSMs(null, null, null, null, null,
                null, null, testData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(10L); // Test data should return 10 items
    }

    @Test
    public void testCountOdeBsms() {
        Long startTime = 1000L;
        Long endTime = 2000L;
        Long expectedCount = 5L;

        when(permissionService.hasIntersection(null, "USER")).thenReturn(true);
        when(permissionService.hasRole("USER")).thenReturn(true);
        when(odeBsmJsonRepo.count(null, null, startTime, endTime, null, null, null))
                .thenReturn(expectedCount);

        ResponseEntity<Long> response = controller.countBSMs(null, null,
                startTime, endTime, null, null, null, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedCount);
        verify(odeBsmJsonRepo, times(1)).count(null, null, startTime, endTime, null, null, null);
    }
}