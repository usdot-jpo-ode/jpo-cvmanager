package us.dot.its.jpo.ode.api;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import us.dot.its.jpo.geojsonconverter.DateJsonMapper;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public WebConfig() {
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(DateJsonMapper.getInstance()));
    }
}