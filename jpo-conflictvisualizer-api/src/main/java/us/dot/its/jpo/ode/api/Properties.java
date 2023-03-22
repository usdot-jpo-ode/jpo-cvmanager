package us.dot.its.jpo.ode.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties()
public class Properties {

    private static int maximumResponseSize;
    private static String cors;

    public int getMaximumResponseSize() {
        return maximumResponseSize;
    }

    @Value("${maximumResponseSize}")
    public void setMaximumResponseSize(int maximumResponseSize) {
        this.maximumResponseSize = maximumResponseSize;
    }

    public String getCors() {
        return cors;
    }

    @Value("${cors}")
    public void setCors(String cors) {
        this.cors = cors;
    }

    @Bean
    public ObjectMapper defaultMapper() {
        ObjectMapper objectMapper = DateJsonMapper.getInstance();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        return objectMapper;
    }

    

}
