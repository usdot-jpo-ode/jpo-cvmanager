package us.dot.its.jpo.ode.api.models;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecondData extends ChartData {
    
    
    
    
    public static SecondData fromIDCountSeconds(List<IDCount> idCountList, List<Long> secondMarks){
        Map<String, Double> valueMap = new HashMap<>();

        for(IDCount elem: idCountList){
            valueMap.put(elem.id, (double)elem.count);
        }

        ArrayList<Double> counts = new ArrayList<>();

        for(long secondMark : secondMarks){
            if(valueMap.containsKey(secondMark)){
                counts.add(valueMap.get(secondMark));
            }else{
                counts.add(0.0);
            }
        }    

        SecondData data = new SecondData();
        
        data.setValues(counts);
        data.setLabels(secondMarks);

        return data;
    }


    


}
