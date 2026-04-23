package us.dot.its.jpo.ode.api.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import us.dot.its.jpo.ode.api.models.postgres.projections.ScmsHealthRsuProjection;
import us.dot.its.jpo.ode.api.models.scms.ScmsHealthDto;
import us.dot.its.jpo.ode.api.models.scms.ScmsHealthResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MapStruct mapper for SCMS health data.
 *
 * <p>MapStruct generates {@link #toDto(ScmsHealthRsuProjection)} with compile-time field checking.
 * If a field is added to {@link ScmsHealthDto} without a corresponding mapping, MapStruct emits
 * a compile error (see {@code unmappedTargetPolicy}).</p>
 *
 * <p>{@link #toResponse(List)} is manually implemented because MapStruct does not yet support
 * {@code List → Map} conversions keyed by a property. It delegates to the generated {@code toDto()}
 * to preserve compile-time field checking.</p>
 *
 * <p>The {@code expiration} field is mapped directly as {@link java.time.Instant}. Jackson's
 * {@code JavaTimeModule} (auto-registered by Spring Boot) serializes it as ISO-8601 UTC.</p>
 *
 * @see <a href="https://github.com/mapstruct/mapstruct/discussions/3263">MapStruct Discussion #3263</a>
 * @see <a href="https://github.com/mapstruct/mapstruct/issues/3580">MapStruct Issue #3580</a>
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ScmsHealthMapper {

    /**
     * Maps a single projection to DTO. MapStruct generates this method.
     */
    ScmsHealthDto toDto(ScmsHealthRsuProjection projection);

    /**
     * Converts projections to a response keyed by IP address.
     * Delegates to {@link #toDto} to preserve compile-time field checking.
     */
    default ScmsHealthResponse toResponse(List<ScmsHealthRsuProjection> projections) {
        if (projections == null) {
            return null;
        }
        Map<String, ScmsHealthDto> scmsHealthByIp = new HashMap<>();
        for (ScmsHealthRsuProjection projection : projections) {
            String ip = projection.getIpv4Address().getHostAddress();
            ScmsHealthDto dto = projection.getHealth() != null ? toDto(projection) : null;
            scmsHealthByIp.put(ip, dto);
        }
        return new ScmsHealthResponse(scmsHealthByIp);
    }
}
