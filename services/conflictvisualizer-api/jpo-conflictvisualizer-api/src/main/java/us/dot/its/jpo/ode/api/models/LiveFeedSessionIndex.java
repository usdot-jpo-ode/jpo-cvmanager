package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@ToString
@Setter
@EqualsAndHashCode
@Getter
public class LiveFeedSessionIndex {
    private int intersectionID = -1;
    private String roadRegulatorID = "-1";

    public LiveFeedSessionIndex(){

    }

    public LiveFeedSessionIndex(int intersectionID, String roadRegulatorID){
        this.intersectionID = intersectionID;
        this.roadRegulatorID = roadRegulatorID;
    }
}
