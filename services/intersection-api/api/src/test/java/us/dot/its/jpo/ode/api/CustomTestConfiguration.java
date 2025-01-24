package us.dot.its.jpo.ode.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import us.dot.its.jpo.ode.api.services.EmailService;
import us.dot.its.jpo.ode.api.services.PostgresService;
import us.dot.its.jpo.ode.api.services.ReportService;
import us.dot.its.jpo.ode.api.tasks.EmailTask;
import us.dot.its.jpo.ode.api.tasks.ReportTask;

@TestConfiguration
@ComponentScan(
    basePackages = "us.dot.its.jpo.ode.api",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = EmailService.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PostgresService.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ReportService.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = EmailTask.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ReportTask.class)
    }
)
public class CustomTestConfiguration {
}