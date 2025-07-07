package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@ToString
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class IDCount {
    private String id;
    private double count;
}
