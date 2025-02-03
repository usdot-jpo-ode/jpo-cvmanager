package us.dot.its.jpo.ode.mockdata;

import java.util.ArrayList;
import java.util.List;

import us.dot.its.jpo.ode.api.models.IDCount;

public class MockIDCountGenerator {


    
    public static List<IDCount> getDateIDCounts(){
        List<IDCount> counts = new ArrayList<>();

        IDCount dayOne = new IDCount();
        dayOne.setCount((int)Math.random() * 10);
        dayOne.setId("2023-06-01");

        IDCount dayTwo = new IDCount();
        dayTwo.setCount((int)Math.random() * 10);
        dayTwo.setId("2023-06-03");

        IDCount dayThree = new IDCount();
        dayThree.setCount((int)Math.random() * 10);
        dayThree.setId("2023-06-03");

        counts.add(dayOne);
        counts.add(dayTwo);
        counts.add(dayThree);


        return counts;

    }


}
