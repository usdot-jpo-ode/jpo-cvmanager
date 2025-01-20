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
import us.dot.its.jpo.ode.api.converters.ZonedDateTimeToStringConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoClientConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    private List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();

    @Value("${spring.data.mongodb.database}")
    private String db;

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private String port;

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    @Value("${spring.data.mongodb.authenticationDatabase}")
    private String authenticationDatabase;

    @Value("${spring.data.mongodb.uri}")
    private String overrideURI;

    @Override
    protected String getDatabaseName() {
        return db;
    }

    @Override
    public void configureClientSettings(MongoClientSettings.Builder builder) {

        String uri = "";

        if (overrideURI != null && !overrideURI.isEmpty() && !overrideURI.equals("null")) {
            uri = overrideURI;
        } else {
            uri = "mongodb://" + username + ":" + password + "@" + host + ":" + port + "/" + db + "?authSource="
                    + authenticationDatabase;
        }

        
        logger.info("MongoDB Connection String: {}", uri);
        builder.applyConnectionString(new ConnectionString(uri));
    }

    @Override
    public MongoCustomConversions customConversions() {
        converters.add(new StringToZonedDateTimeConverter());
        converters.add(new ZonedDateTimeToStringConverter());
        return new MongoCustomConversions(converters);
    }
}
