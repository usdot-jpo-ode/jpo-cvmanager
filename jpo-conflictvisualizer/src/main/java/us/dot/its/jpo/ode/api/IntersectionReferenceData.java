package us.dot.its.jpo.ode.api;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class IntersectionReferenceData {
    int intersectionID;
    String roadRegulatorID;
    String rsuIP;

    public String toString(){
        return "IntersectionID: " + intersectionID + "rsuIP: " + rsuIP + "Road Regulator ID: " + roadRegulatorID;
    }
}
