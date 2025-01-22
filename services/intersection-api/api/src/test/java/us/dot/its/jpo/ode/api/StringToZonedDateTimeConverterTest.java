package us.dot.its.jpo.ode.api;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import us.dot.its.jpo.ode.api.converters.StringToZonedDateTimeConverter;

@SpringBootTest
public class StringToZonedDateTimeConverterTest {

    String[] inputTimeStrings = {
        "2025-01-21T14:45:00.123Z",
        "2025-01-21T14:45:00.12Z",
        "2025-01-21T14:45:00.1Z",
        "2025-01-21T14:45:00Z",
        "2025-01-21T14:45:00.123456Z",
        "2025/01/21 14:45:00.123456Z"   // Failure Case
    };
        
    ZonedDateTime[] outputZonedDateTimes = {
        ZonedDateTime.of(2025, 1, 21, 21, 14, 45, 123000000, ZoneId.of("UTC")),
        ZonedDateTime.of(2025, 1, 21, 21, 14, 45, 120000000, ZoneId.of("UTC")),
        ZonedDateTime.of(2025, 1, 21, 21, 14, 45, 100000000, ZoneId.of("UTC")),
        ZonedDateTime.of(2025, 1, 21, 21, 14, 45, 0, ZoneId.of("UTC")),
        ZonedDateTime.of(2025, 1, 21, 21, 14, 45, 123456000, ZoneId.of("UTC")),
        ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")) // Failure Case
    };

  @Test
  public void testTimestampConversion() {

    StringToZonedDateTimeConverter converter = new StringToZonedDateTimeConverter();


    for(int i =0; i< inputTimeStrings.length; i++){
        System.out.println(inputTimeStrings[i]);
        ZonedDateTime convertedTime = converter.convert(inputTimeStrings[i]);

        

        assertThat(convertedTime.equals(outputZonedDateTimes[i]));

    }
  }

}

