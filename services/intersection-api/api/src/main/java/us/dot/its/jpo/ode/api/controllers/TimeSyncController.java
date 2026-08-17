package us.dot.its.jpo.ode.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RestController
@ConditionalOnProperty(name = { "enable.api" }, havingValue = "true", matchIfMissing = false)
@Tag(name = "Time Sync", description = """
        Endpoints for retrieving the current time in milliseconds. \
        All endpoints under /timesync/* are rate-limited using configurable per-user and global limits \
        (defaults: 360 requests/hour per user, keyed on Authorization token or remote IP for unauthenticated requests, \
        and 18000 requests/hour globally across all callers). \
        Exceeding either configured limit returns HTTP 429.""")
@ApiResponses(value = {
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded — per-user and global limits are configurable (defaults: 360 req/hr per user, 18000 req/hr global)"),
                @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@RequestMapping("/timesync")
@RequiredArgsConstructor
public class TimeSyncController {
    @Operation(summary = "Retrieve current time in milliseconds", description = "Returns the current time in milliseconds since the Unix epoch.")
    @RequestMapping(value = "/utc-millis", method = RequestMethod.GET, produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current time retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    })
    public @ResponseBody Long getCurrentTimeMillis() {
        return Instant.now().toEpochMilli();
    }
}