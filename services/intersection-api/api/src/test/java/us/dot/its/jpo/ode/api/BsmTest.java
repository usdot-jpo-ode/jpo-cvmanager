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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.ode.api.accessors.bsm.OdeBsmJsonRepository;
import us.dot.its.jpo.ode.api.controllers.BsmController;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.model.OdeBsmData;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class BsmTest {

    private final BsmController controller;

    @MockBean
    OdeBsmJsonRepository odeBsmJsonRepository;

    @MockBean
    PermissionService permissionService;

    @Autowired
    public BsmTest(BsmController controller) {
        this.controller = controller;
    }

    @Test
    public void testBsmJson() {

        when(permissionService.hasRole("USER")).thenReturn(true);

        List<OdeBsmData> list = new ArrayList<>();

        ResponseEntity<List<OdeBsmData>> result = controller.findBSMs(null, null, null, null, null, null, null, false);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(list);
    }
}