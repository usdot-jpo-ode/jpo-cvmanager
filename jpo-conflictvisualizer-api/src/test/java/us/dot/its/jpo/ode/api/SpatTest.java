package us.dot.its.jpo.ode.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepository;
import us.dot.its.jpo.ode.api.controllers.SpatController;
import us.dot.its.jpo.ode.mockdata.MockSpatGenerator;


@SpringBootTest
@RunWith(SpringRunner.class)
public class SpatTest {

  @Autowired
  SpatController controller;

  @MockBean
  ProcessedSpatRepository processedSpatRepo;
    


  @Test
  public void testProcessedSpat() {

    MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));

    List<ProcessedSpat> list = MockSpatGenerator.getProcessedSpats();
    
    Query query = processedSpatRepo.getQuery(null, null, null, false,  false);
    when(processedSpatRepo.findProcessedSpats(query)).thenReturn(list);

    ResponseEntity<List<ProcessedSpat>> result = controller.findSpats(null, null, null, false,false,false);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    // assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(result.getBody()).isEqualTo(list);
  }
}