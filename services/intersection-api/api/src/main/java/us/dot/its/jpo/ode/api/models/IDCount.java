package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@ToString
@Setter
@EqualsAndHashCode
@Getter
public class IDCount {
    private String id;
    private double count;
}
