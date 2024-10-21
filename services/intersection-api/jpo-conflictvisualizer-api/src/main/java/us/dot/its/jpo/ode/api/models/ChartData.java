package us.dot.its.jpo.ode.api.models;

import java.util.ArrayList;
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



    public static ChartData fromIDCountList(List<IDCount> list){
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        for(IDCount elem: list){
            labels.add(elem.getId());
            values.add((double)elem.getCount());
        }

        ChartData data = new ChartData<>();
        data.setLabels(labels);
        data.setValues(values);

        return data;

    }
}
