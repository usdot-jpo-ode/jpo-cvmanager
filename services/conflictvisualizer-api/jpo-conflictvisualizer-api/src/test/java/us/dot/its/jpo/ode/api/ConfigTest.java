// package us.dot.its.jpo.ode.api;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.Assert.assertEquals;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.when;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Set;

// import org.junit.Test;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.runner.RunWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.test.context.junit4.SpringRunner;
// import org.springframework.web.client.RestTemplate;


// import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfig;
// import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfigMap;
// import us.dot.its.jpo.conflictmonitor.monitor.models.config.UnitsEnum;
// import us.dot.its.jpo.ode.api.accessors.config.DefaultConfig.DefaultConfigRepository;
// import us.dot.its.jpo.ode.api.accessors.config.IntersectionConfig.IntersectionConfigRepository;
// import us.dot.its.jpo.ode.api.controllers.ConfigController;


// @SpringBootTest
// @RunWith(SpringRunner.class)
// public class ConfigTest {

//     @Mock
//     RestTemplate restTemplate;

//     @InjectMocks
//     @Autowired
//     ConfigController controller;

//     @MockBean
//     DefaultConfigRepository defaultConfigRepo;

//     @MockBean
//     IntersectionConfigRepository intersectionConfigRepo;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

// //   @Test
// //   public void testIntersectionSignalStateRedLightRunningMinimumSpeed_found() {
// //     MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));
// //     int roadRegulatorID = 12345;
// //     int intersectionID = 1;
// //     List<IntersectionConfig> list = new ArrayList<>();
// //     list.add(new IntersectionConfig<Double>("ss-red-light-running-minimum-speed", "signal-state", roadRegulatorID, intersectionID, 5.0, "Double", UnitsEnum.MILES_PER_HOUR, "Minimum Red Light Speed"));
// //     // public IntersectionConfig(String key, String category, int roadRegulatorID, int intersectionID, T value, String type, UnitsEnum units, String description){
    
// //     Query query = intersectionConfigRepo.getQuery("ss-red-light-running-minimum-speed", roadRegulatorID, intersectionID);
// //     when(intersectionConfigRepo.find(query)).thenReturn(list);

// //     ResponseEntity<IntersectionConfig<Double>> result = controller.intersection_config(roadRegulatorID, intersectionID);
// //     assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
// //     assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
// //     assertThat(result.getBody()).isEqualTo(list.get(0));
// //   }

// //   @Test
// //   public void testIntersectionSignalStateRedLightRunningMinimumSpeed_notFound() {
// //     MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));
// //     int roadRegulatorID = 67890;
// //     int intersectionID = 2;
// //     List<IntersectionConfig> list = new ArrayList<>();
// //     Query query = intersectionConfigRepo.getQuery("ss-red-light-running-minimum-speed", roadRegulatorID, intersectionID);
// //     when(intersectionConfigRepo.find(query)).thenReturn(list);

// //     ResponseEntity<IntersectionConfig<Double>> result = controller.intersection_signal_state_red_light_running_minimum_speed(roadRegulatorID, intersectionID);
// //     assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
// //     assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
// //     assertEquals(null, result.getBody());
// //   }

//     // @Test
//     // public void testDefaultConfigAll() {

//     //     MockKeyCloakAuth.setSecurityContextHolder("cm_user", Set.of("USER"));
        
//     //     DefaultConfigMap defaultConfigMap = new DefaultConfigMap();

//     //     DefaultConfig<Double> test = new DefaultConfig<Double>("ss-red-light-running-minimum-speed", "signal-state", 5.0, "Double", UnitsEnum.MILES_PER_HOUR, "Minimum Red Light Speed");

//     //     defaultConfigMap.put("ss-red-light-running-minimum-speed", test);
//     //     defaultConfigMap.put("test", test);

        

//     //     when(restTemplate.getForEntity(anyString(), eq(DefaultConfigMap.class)))
//     //             .thenReturn(new ResponseEntity<>(defaultConfigMap, HttpStatus.OK));

    
//     //     ResponseEntity<List<DefaultConfig>> responseEntity = controller.default_config_all();

//     //     List<DefaultConfig> responseMap = responseEntity.getBody();

//     //     System.out.println(responseMap.size());
//     //     System.out.println(responseMap.get(0));

//     //     assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//     //     assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
//     //     assertEquals(test, responseMap.get(0));
//     // }

// }