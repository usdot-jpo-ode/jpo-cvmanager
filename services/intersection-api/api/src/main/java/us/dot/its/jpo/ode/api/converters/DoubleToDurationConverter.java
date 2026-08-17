package us.dot.its.jpo.ode.api.converters;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.mongodb.lang.NonNull;

@ReadingConverter
public class DoubleToDurationConverter implements Converter<Double, Duration> {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    @Override
    public Duration convert(@NonNull Double source) {
        long seconds = source.longValue();
        long nanoseconds = (long) (1E9 * (source - seconds));
        return Duration.ofSeconds(seconds, nanoseconds);
    }
}