package us.dot.its.jpo.ode.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import us.dot.its.jpo.ode.api.converters.StringToZonedDateTimeConverter;

import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoClientConfiguration{
    private List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();

    @Override
    protected String getDatabaseName() {
        return "ConflictMonitor";
    }

    @Override
    public MongoCustomConversions customConversions() {
        converters.add(new StringToZonedDateTimeConverter());
        return new MongoCustomConversions(converters);
    }
}
