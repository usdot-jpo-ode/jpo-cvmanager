package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class IntersectionReferenceData {
    int intersectionID;
    String rsuIP;
    double longitude;
    double latitude;
    String intersectionName;

    public String toString() {
        return "IntersectionID: " + intersectionID + "rsuIP: " + rsuIP + " Name: " + intersectionName;
    }
}
