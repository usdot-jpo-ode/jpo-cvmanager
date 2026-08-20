package us.dot.its.jpo.ode.api.models.emails.contents.message_counts;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Message counts for a specific message type, including ingress, egress, and percentage difference between the two")
@Data
public class MessageCountCountsItem {
    @Schema(description = "Ingress message counts")
    private int in;
    @Schema(description = "Egress message counts")
    private int out;
    @Schema(description = "Percentage difference between egress and ingress message counts " +
            "(absolute value, always positive), where 0% difference means identical counts. For commonly " +
            "deduplicated message types (like MAP), the percent difference accounts for expected 3600:1 " +
            "deduplication. In cases where the in counts are zero or the out counts are greater than the " +
            "in counts, the diff percent is set to 6%, enough to trigger an error (>5%)")
    @JsonProperty("diff_percent")
    private double diffPercent;
}
