package us.dot.its.jpo.ode.mockdata;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAggregator;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAggregator;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAggregator;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLineStopAggregator;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLineStopAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;

public class MockAssessmentGenerator {
    public static ConnectionOfTravelAssessment getConnectionOfTravelAssessment(){
        ConnectionOfTravelAggregator aggregator = new ConnectionOfTravelAggregator();
        aggregator.add(MockEventGenerator.getConnectionOfTravelEvent());
        aggregator.add(MockEventGenerator.getConnectionOfTravelEvent());
        ConnectionOfTravelAssessment assessment = aggregator.getConnectionOfTravelAssessment(1);
        return assessment;
    }

    public static LaneDirectionOfTravelAssessment getLaneDirectionOfTravelAssessment(){
        LaneDirectionOfTravelAggregator aggregator = new LaneDirectionOfTravelAggregator();
        aggregator.add(MockEventGenerator.getLaneDirectionOfTravelEvent());
        LaneDirectionOfTravelAssessment assessment = aggregator.getLaneDirectionOfTravelAssessment(20, 100, 1);
        return assessment;
    }

    public static StopLineStopAssessment getStopLineStopAssessment(){
        StopLineStopAggregator aggregator = new StopLineStopAggregator();
        StopLineStopEvent event = MockEventGenerator.getStopLineStopEvent();
        aggregator.add(event);
        aggregator.add(event);
        StopLineStopAssessment assessment = aggregator.getStopLineStopAssessment(1);
        return assessment;
    }

    public static StopLinePassageAssessment getStopLinePassageAssessment(){
        StopLinePassageAggregator aggregator = new StopLinePassageAggregator();
        StopLinePassageEvent event = MockEventGenerator.getStopLinePassageEvent();
        aggregator.add(event);
        aggregator.add(event);
        StopLinePassageAssessment assessment = aggregator.getStopLinePassageAssessment(1);
        return assessment;
    }
}
