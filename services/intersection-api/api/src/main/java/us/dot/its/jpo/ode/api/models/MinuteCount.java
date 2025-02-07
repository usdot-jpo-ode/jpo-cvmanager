package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@ToString
@Setter
@EqualsAndHashCode
@Getter
public class MinuteCount {
    private long minute; //utc millisecond indicating the start of the minute
    private int count;
}
