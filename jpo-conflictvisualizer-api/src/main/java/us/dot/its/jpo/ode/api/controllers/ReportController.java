package us.dot.its.jpo.ode.api.controllers;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import us.dot.its.jpo.ode.api.Properties;
import us.dot.its.jpo.ode.api.ReportBuilder;
import us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.IntersectionReferenceAlignmentEvent.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalGroupAlignmentEvent.SignalGroupAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateConflictEvent.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateEvent.SignalStateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateStopEvent.SignalStateStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.TimeChangeDetailsEvent.TimeChangeDetailsEventRepository;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepository;
import us.dot.its.jpo.ode.api.models.DailyData;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.SecondData;

@RestController
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(MapController.class);
    

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    Properties props;

    @Autowired
    ProcessedMapRepository processedMapRepo;

    @Autowired
    ProcessedSpatRepository processedSpatRepo;

    @Autowired
    SignalStateEventRepository signalStateEventRepo;

    @Autowired
    SignalStateStopEventRepository signalStateStopEventRepo;

    @Autowired
    ConnectionOfTravelEventRepository connectionOfTravelEventRepo;

    @Autowired
    IntersectionReferenceAlignmentEventRepository intersectionReferenceAlignmentEventRepo;

    @Autowired
    LaneDirectionOfTravelEventRepository laneDirectionOfTravelEventRepo;

    @Autowired
    SignalGroupAlignmentEventRepository signalGroupAlignmentEventRepo;

    @Autowired
    SignalStateConflictEventRepository signalStateConflictEventRepo;

    @Autowired
    TimeChangeDetailsEventRepository timeChangeDetailsEventRepo;



    public String getCurrentTime() {
        return ZonedDateTime.now().toInstant().toEpochMilli() + "";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/reports/generate", method = RequestMethod.GET, produces = "application/octet-stream")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public byte[] generateReport(
            @RequestParam(name = "intersection_id", required = true) int intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = true) long startTime,
            @RequestParam(name = "end_time_utc_millis", required = true) long endTime) {

            
            
            

            List<IDCount> mapCounts = processedMapRepo.getMapBroadcastRates(intersectionID, startTime, endTime);
            List<IDCount> spatCounts = processedSpatRepo.getSpatBroadcastRates(intersectionID, startTime, endTime);
            List<IDCount> signalstateEventCounts = signalStateEventRepo.getSignalStateEventsByDay(intersectionID, startTime, endTime);
            List<IDCount> signalStateStopEventCounts = signalStateStopEventRepo.getSignalStateStopEventsByDay(intersectionID, startTime, endTime);
            List<IDCount> laneDirectionOfTravelEventCounts = laneDirectionOfTravelEventRepo.getLaneDirectionOfTravelEventsByDay(intersectionID, startTime, endTime);
            List<IDCount> connectionOfTravelEventCounts = connectionOfTravelEventRepo.getConnectionOfTravelEventsByDay(intersectionID, startTime, endTime);
            List<IDCount> signalStateConflictEventCounts = signalStateConflictEventRepo.getSignalStateConflictEventsByDay(intersectionID, startTime, endTime);
            List<IDCount> timeChangeDetailsEventCounts= timeChangeDetailsEventRepo.getTimeChangeDetailsEventsPerDay(intersectionID, startTime, endTime);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ReportBuilder builder = new ReportBuilder(stream);
            // builder.addMapBroadcastRate(mapCounts);
            // builder.addSpatBroadcastRate(spatCounts);
            // builder.addSignalStateEvents(signalstateEventCounts);
            // builder.addSignalStateStopEvents(signalStateStopEventCounts);
            // builder.addLaneDirectionOfTravelEvent(laneDirectionOfTravelEventCounts);
            // builder.addConnectionOfTravelEvent(connectionOfTravelEventCounts);
            // builder.addSignalStateConflictEvent(signalStateConflictEventCounts);
            // builder.addSpatTimeChangeDetailsEvent(timeChangeDetailsEventCounts);
            builder.write();            

            return stream.toByteArray();
    }

    

    


    @Bean
    public void test(){
        System.out.println("Generating Test PDF");

        int intersectionID = 12109;
        long startTime = 1683504000000L;
        // long startTime = 1678233600000L;
        long endTime = 1684178369000L;

        // List<IDCount> mapCounts = processedMapRepo.getMapBroadcastRates(intersectionID, startTime, endTime);
        // List<IDCount> spatCounts = processedSpatRepo.getSpatBroadcastRates(intersectionID, startTime, endTime);
        List<IDCount> signalstateEventCounts = signalStateEventRepo.getSignalStateEventsByDay(intersectionID, startTime, endTime);
        List<IDCount> signalStateStopEventCounts = signalStateStopEventRepo.getSignalStateStopEventsByDay(intersectionID, startTime, endTime);
        List<IDCount> laneDirectionOfTravelEventCounts = laneDirectionOfTravelEventRepo.getLaneDirectionOfTravelEventsByDay(intersectionID, startTime, endTime);
        List<IDCount> connectionOfTravelEventCounts = connectionOfTravelEventRepo.getConnectionOfTravelEventsByDay(intersectionID, startTime, endTime);
        List<IDCount> signalStateConflictEventCounts = signalStateConflictEventRepo.getSignalStateConflictEventsByDay(intersectionID, startTime, endTime);
        List<IDCount> timeChangeDetailsEventCounts= timeChangeDetailsEventRepo.getTimeChangeDetailsEventsPerDay(intersectionID, startTime, endTime);

        
        


        try {
            ReportBuilder builder = new ReportBuilder(new FileOutputStream("test.pdf"));
            List<String> dateStrings = builder.getDayStringsInRange(startTime, endTime);
            // List<Long> secondStrings = builder.getSecondsStringInRange(startTime, endTime);
            // builder.addMapBroadcastRate(mapCounts);
            // builder.addSpatBroadcastRate(SecondData.fromIDCountSeconds(spatCounts, secondStrings));
            builder.addSignalStateEvents(DailyData.fromIDCountDays(signalstateEventCounts, dateStrings));
            builder.addSignalStateEvents(DailyData.fromIDCountDays(signalStateStopEventCounts, dateStrings));
            builder.addLaneDirectionOfTravelEvent(DailyData.fromIDCountDays(laneDirectionOfTravelEventCounts, dateStrings));
            builder.addConnectionOfTravelEvent(DailyData.fromIDCountDays(connectionOfTravelEventCounts, dateStrings));
            builder.addSignalStateConflictEvent(DailyData.fromIDCountDays(signalStateConflictEventCounts, dateStrings));
            builder.addSpatTimeChangeDetailsEvent(DailyData.fromIDCountDays(timeChangeDetailsEventCounts, dateStrings));
            
            // builder.addTestImage();
            // builder.addSignalStateStopEvents(signalStateStopEventCounts);
            // builder.addLaneDirectionOfTravelEvent(laneDirectionOfTravelEventCounts);
            // builder.addConnectionOfTravelEvent(connectionOfTravelEventCounts);
            // builder.addSignalStateConflictEvent(signalStateConflictEventCounts);
            // builder.addSpatTimeChangeDetailsEvent(timeChangeDetailsEventCounts);
            // builder.addTestImage();
            // builder.addTestBarChart();
            // builder.addTestHeatmap();

            builder.write();
            

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Test PDF Generation Complete");
        
        // String templateString = ReportBuilder.parseThymeleafTemplate();
        // ReportBuilder.generatePdfFromHtml(templateString);
        
    }
}