package us.dot.its.jpo.ode.api.models.emails;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import us.dot.its.jpo.ode.api.models.postgres.tables.UserEmailNotification;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserEmailNotificationDto {

    @NotNull
    public String category;

    public String description;

    @NotNull
    @JsonProperty("required_role")
    private String requiredRole;

    @NotNull
    public Boolean immediate;

    @NotNull
    public Boolean hourly;

    @NotNull
    public Boolean daily;

    @NotNull
    public Boolean weekly;

    @NotNull
    public Boolean monthly;

    @NotNull
    @JsonProperty("supports_immediate")
    public Boolean supportsImmediate;

    @NotNull
    @JsonProperty("supports_hourly")
    public Boolean supportsHourly;

    @NotNull
    @JsonProperty("supports_daily")
    public Boolean supportsDaily;

    @NotNull
    @JsonProperty("supports_weekly")
    public Boolean supportsWeekly;

    @NotNull
    @JsonProperty("supports_monthly")
    public Boolean supportsMonthly;

    public Boolean getSubscribed() {
        return immediate ||
                hourly ||
                daily ||
                weekly ||
                monthly;
    }

    public Boolean isFrequencyEqual(UserEmailNotificationDto other) {
        return this.immediate.equals(other.immediate)
                && this.hourly.equals(other.hourly)
                && this.daily.equals(other.daily)
                && this.weekly.equals(other.weekly)
                && this.monthly.equals(other.monthly);
    }

    public Boolean isFrequencyEqual(UserEmailNotification other) {
        return (this.immediate != null && other.getImmediate() != null && this.immediate.equals(other.getImmediate()))
                && (this.hourly != null && other.getHourly() != null && this.hourly.equals(other.getHourly()))
                && (this.daily != null && other.getDaily() != null && this.daily.equals(other.getDaily()))
                && (this.weekly != null && other.getWeekly() != null && this.weekly.equals(other.getWeekly()))
                && (this.monthly != null && other.getMonthly() != null && this.monthly.equals(other.getMonthly()));
    }
}