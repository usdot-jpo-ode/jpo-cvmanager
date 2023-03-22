package us.dot.its.jpo.ode.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfig;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.IntersectionConfig;
import us.dot.its.jpo.conflictmonitor.monitor.models.config.UnitsEnum;
import us.dot.its.jpo.ode.api.accessors.config.DefaultConfig.DefaultConfigRepository;
import us.dot.its.jpo.ode.api.accessors.config.IntersectionConfig.IntersectionConfigRepository;
import us.dot.its.jpo.ode.api.controllers.ConfigController;


@SpringBootTest
@RunWith(SpringRunner.class)
public class ConfigTest {

  @Autowired
  ConfigController controller;

  @MockBean
  DefaultConfigRepository defaultConfigRepo;

  @MockBean
  IntersectionConfigRepository intersectionConfigRepo;

  @Test
  public void testDefaultSignalStateRedLightRunningMinimumSpeed_found() {
    List<DefaultConfig> list = new ArrayList<>();
    list.add(new DefaultConfig("ss-red-light-running-minimum-speed", "signal-state", 5.0, "Double",UnitsEnum.MILES_PER_HOUR, "Minimum Red Light Speed"));
    
    Query query = defaultConfigRepo.getQuery("ss-red-light-running-minimum-speed");
    when(defaultConfigRepo.find(query)).thenReturn(list);

    ResponseEntity<DefaultConfig<Double>> result = controller.default_signal_state_red_light_running_minimum_speed();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(result.getBody()).isEqualTo(list.get(0));
  }

  @Test
  public void testDefaultSignalStateRedLightRunningMinimumSpeed_notFound() {
    int roadRegulatorID = 67890;
    int intersectionID = 2;
    List<DefaultConfig> list = new ArrayList<>();
    Query query = intersectionConfigRepo.getQuery("ss-red-light-running-minimum-speed", roadRegulatorID, intersectionID);
    when(defaultConfigRepo.find(query)).thenReturn(list);

    ResponseEntity<DefaultConfig<Double>> result = controller.default_signal_state_red_light_running_minimum_speed();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertEquals(null, result.getBody());
  }


  @Test
  public void testIntersectionSignalStateRedLightRunningMinimumSpeed_found() {
    int roadRegulatorID = 12345;
    int intersectionID = 1;
    List<IntersectionConfig> list = new ArrayList<>();
    list.add(new IntersectionConfig("ss-red-light-running-minimum-speed", "signal-state", roadRegulatorID, intersectionID, "1", 5.0, "Double",UnitsEnum.MILES_PER_HOUR, "Minimum Red Light Speed"));
    
    Query query = intersectionConfigRepo.getQuery("ss-red-light-running-minimum-speed", roadRegulatorID, intersectionID);
    when(intersectionConfigRepo.find(query)).thenReturn(list);

    ResponseEntity<IntersectionConfig<Double>> result = controller.intersection_signal_state_red_light_running_minimum_speed(roadRegulatorID, intersectionID);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(result.getBody()).isEqualTo(list.get(0));
  }

  @Test
  public void testIntersectionSignalStateRedLightRunningMinimumSpeed_notFound() {
    int roadRegulatorID = 67890;
    int intersectionID = 2;
    List<IntersectionConfig> list = new ArrayList<>();
    Query query = intersectionConfigRepo.getQuery("ss-red-light-running-minimum-speed", roadRegulatorID, intersectionID);
    when(intersectionConfigRepo.find(query)).thenReturn(list);

    ResponseEntity<IntersectionConfig<Double>> result = controller.intersection_signal_state_red_light_running_minimum_speed(roadRegulatorID, intersectionID);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertEquals(null, result.getBody());
  }
}