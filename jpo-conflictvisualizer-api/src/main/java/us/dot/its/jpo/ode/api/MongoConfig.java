package us.dot.its.jpo.ode.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;

import us.dot.its.jpo.ode.api.converters.StringToZonedDateTimeConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoClientConfiguration{
    private List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();

    // @Value("${spring.data.mongodb.uri}")
    // private String uri;

    @Value("${spring.data.mongodb.database}")
    private String db;

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private String port;

    @Override
    protected String getDatabaseName() {
        return "ConflictMonitor";
    }

    @Override
    public void configureClientSettings(MongoClientSettings.Builder builder) {
        // customization hook
        String uri = "mongodb://"+host+":"+port+"/"+db;
        System.out.println("Connecting to MongoDB at: " + uri);
        builder.applyConnectionString(new ConnectionString(uri));
    }

    @Override
    public MongoCustomConversions customConversions() {
        converters.add(new StringToZonedDateTimeConverter());
        return new MongoCustomConversions(converters);
    }
}
