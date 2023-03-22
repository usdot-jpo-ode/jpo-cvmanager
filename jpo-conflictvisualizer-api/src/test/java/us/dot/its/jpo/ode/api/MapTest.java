package us.dot.its.jpo.ode.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.controllers.MapController;
import us.dot.its.jpo.ode.mockdata.MockMapGenerator;


@SpringBootTest
@RunWith(SpringRunner.class)
public class MapTest {

  @Autowired
  MapController controller;

  @MockBean
  ProcessedMapRepository processedMapRepo;
    


  @Test
  public void testProcessedMap() {

    List<ProcessedMap> list = MockMapGenerator.getProcessedMaps();
    
    Query query = processedMapRepo.getQuery(null, null, null, false);
    when(processedMapRepo.findProcessedMaps(query)).thenReturn(list);

    ResponseEntity<List<ProcessedMap>> result = controller.findMaps(null, null, null, false, false);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    // assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(result.getBody()).isEqualTo(list);
  }
}