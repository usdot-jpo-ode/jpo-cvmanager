package us.dot.its.jpo.ode.api.models;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@ToString
@Setter
@EqualsAndHashCode
@Getter
public class ChartData<T> {
    
    private List<T> labels;
    private List<Double> values;

}
