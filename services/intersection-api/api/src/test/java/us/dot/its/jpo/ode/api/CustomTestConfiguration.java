package us.dot.its.jpo.ode.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import us.dot.its.jpo.ode.api.services.EmailService;

@TestConfiguration
@ComponentScan(
    basePackages = "us.dot.its.jpo.ode.api",
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = EmailService.class)
)
public class CustomTestConfiguration {
}