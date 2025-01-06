package us.dot.its.jpo.ode.api.converters;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.mongodb.lang.NonNull;

@ReadingConverter
public class StringToZonedDateTimeConverter implements Converter<String, ZonedDateTime> {
    private static final Logger logger = LoggerFactory.getLogger(StringToZonedDateTimeConverter.class);

    DateTimeFormatter formats[] = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSVV"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SVV"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssVV"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSVV")
    };

    @Override
    public ZonedDateTime convert(@NonNull String source) {
        for (DateTimeFormatter format : formats) {
            try {
                return ZonedDateTime.parse(source, format);
            } catch (Exception e) {
                logger.error("Error parsing ZonedDateTime: {}", e);
                // Block of code to handle errors
            }
        }
        return ZonedDateTime.of(0, 0, 0, 0, 0, 0, 0, null);
    }
}