package us.dot.its.jpo.ode.api.converters;


import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import com.mongodb.lang.NonNull;

@WritingConverter
public class ZonedDateTimeToStringConverter implements Converter<ZonedDateTime, String> {

    // DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    
    @Override
    public String convert(@NonNull ZonedDateTime source) {
        return source.format(formatter);
    }
}