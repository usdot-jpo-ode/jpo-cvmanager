package us.dot.its.jpo.ode.api.models.scms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

@Schema(description = "A single SCMS health record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScmsHealthDto {
    @Schema(description = "Whether SCMS certificates associated with an RSU are up-to-date (true) or out-of-date (false)", example = "true")
    private Boolean health;

    @Schema(description = "The expiration time of the SCMS certificates associated with an RSU (ISO-8601, UTC)", example = "2026-04-10T13:28:01Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant expiration;
}
