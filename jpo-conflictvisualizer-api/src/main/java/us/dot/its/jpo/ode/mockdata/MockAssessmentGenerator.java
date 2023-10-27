package us.dot.its.jpo.ode.mockdata;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAggregator;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAggregator;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.SignalStateAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAggregator;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;

public class MockAssessmentGenerator {
    public static ConnectionOfTravelAssessment getConnectionOfTravelAssessment(){
        ConnectionOfTravelAggregator aggregator = new ConnectionOfTravelAggregator();
        aggregator.add(MockEventGenerator.getConnectionOfTravelEvent());
        aggregator.add(MockEventGenerator.getConnectionOfTravelEvent());
        ConnectionOfTravelAssessment assessment = aggregator.getConnectionOfTravelAssessment();
        return assessment;
    }

    public static LaneDirectionOfTravelAssessment getLaneDirectionOfTravelAssessment(){
        LaneDirectionOfTravelAggregator aggregator = new LaneDirectionOfTravelAggregator();
        aggregator.add(MockEventGenerator.getLaneDirectionOfTravelEvent());
        aggregator.setTolerance(30);
        LaneDirectionOfTravelAssessment assessment = aggregator.getLaneDirectionOfTravelAssessment();
        return assessment;
    }

    public static SignalStateAssessment getSignalStateAssessment(){
        SignalStateAssessment assessment = new SignalStateAssessment();
        return assessment;
    }

    public static StopLinePassageAssessment getSignalStateEventAssessment(){
        StopLinePassageAggregator aggregator = new StopLinePassageAggregator();
        StopLinePassageEvent event = MockEventGenerator.getStopLinePassageEvent();
        aggregator.add(event);
        StopLinePassageAssessment assessment = aggregator.getSignalStateEventAssessment();
        return assessment;
    }
}
