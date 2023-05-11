package us.dot.its.jpo.ode.api.controllers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.ZonedDateTime;
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
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepository;
import us.dot.its.jpo.ode.api.models.IDCount;

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

    public String getCurrentTime() {
        return ZonedDateTime.now().toInstant().toEpochMilli() + "";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/reports/generate", method = RequestMethod.GET, produces = "application/octet-stream")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public byte[] generateReport(
            @RequestParam(name = "intersection_id", required = true) Integer intersectionID,
            @RequestParam(name = "start_time_utc_millis", required = false) Long startTime,
            @RequestParam(name = "end_time_utc_millis", required = false) Long endTime) {


        // ReportBuilder builder = new ReportBuilder();

        // String html = builder.parseThymeleafTemplate();
        byte[] b = new byte[8];
        return b;
        // return builder.testBuildPDF();
        // return builder.generatePdfFromHtml(html);
    }


    @Bean
    public void test(){
        System.out.println("Generating Test PDF");
        List<IDCount> counts = processedMapRepo.getMapBroadcastRates(12109, 0L, 1683817601000L);
        List<IDCount> spatCounts = processedSpatRepo.getSpatBroadcastRates(12109, 0L, 1683817601000L);
        ReportBuilder builder;
        try {
            builder = new ReportBuilder(new FileOutputStream("test.pdf"));
            builder.addMapBroadcastRate(counts);
            builder.addSpatBroadcastRate(spatCounts);
            builder.write();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Test PDF Generation Complete");
        
        
    }
}