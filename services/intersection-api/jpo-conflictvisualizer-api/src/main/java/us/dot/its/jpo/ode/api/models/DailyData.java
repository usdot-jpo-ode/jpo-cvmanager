package us.dot.its.jpo.ode.api.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyData extends ChartData {
    
    public static DailyData fromIDCountDays(List<IDCount> idCountList, List<String> dates){
        
        Map<String, Double> valueMap = new HashMap<>();

        for(IDCount elem: idCountList){
            valueMap.put(elem.id, (double)elem.count);
        }

        ArrayList<Double> counts = new ArrayList<>();
        for(String date : dates){
            if(valueMap.containsKey(date)){
                counts.add(valueMap.get(date));
            }else{
                counts.add(0.0);
            }
        }    

        DailyData data = new DailyData();
        

        data.setLabels(dates);
        data.setValues(counts);

        return data;
    }

    


}
