package us.dot.its.jpo.ode.api;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {

    @Value("${sendgrid.username}")
    private String sendGridUsername;

    @Value("${sendgrid.password}")
    private String sendGridPassword;

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(sendGridPassword);
    }
}
