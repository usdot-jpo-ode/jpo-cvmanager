package us.dot.its.jpo.ode.mockdata;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAggregator;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAggregator;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.SignalStateAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.SignalStateEventAggregator;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.SignalStateEventAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateEvent;

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

    public static SignalStateEventAssessment getSignalStateEventAssessment(){
        SignalStateEventAggregator aggregator = new SignalStateEventAggregator();
        SignalStateEvent event = MockEventGenerator.getSignalStateEvent();
        aggregator.add(event);
        SignalStateEventAssessment assessment = aggregator.getSignalStateEventAssessment();
        return assessment;
    }
}
