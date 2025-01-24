package us.dot.its.jpo.ode.api.converters;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.mongodb.lang.NonNull;

@ReadingConverter
public class StringToZonedDateTimeConverter implements Converter<String, ZonedDateTime> {
    private static final Logger logger = LoggerFactory.getLogger(StringToZonedDateTimeConverter.class);

    DateTimeFormatter[] formats = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSVV"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SVV"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssVV"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSVV")
    };

    Pattern[] patterns = {
        Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[a-zA-Z]+$"),
        Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{2}[a-zA-Z]+$"),
        Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{1}[a-zA-Z]+$"),
        Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[a-zA-Z]+$"),
        Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}[a-zA-Z]+$"),
    };

    @Override
    public ZonedDateTime convert(@NonNull String source) {
        try{
            for(int i =0; i< patterns.length; i++){
                if(checkMatch(source, patterns[i])){
                    return ZonedDateTime.parse(source, formats[i]);
                }
            }
            
        } catch (DateTimeParseException e) {
            logger.error("DateTime Parse Exception. Could not parse Timestamp: {} Timestamp may be invalid, or valid parser is missing", source, e);
        } 

        return ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"));
    }

    public boolean checkMatch(String source, Pattern pattern){
        return pattern.matcher(source).matches();
    }
}