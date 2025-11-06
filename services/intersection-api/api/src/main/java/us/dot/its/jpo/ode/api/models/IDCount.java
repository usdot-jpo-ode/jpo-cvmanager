package us.dot.its.jpo.ode.api.models;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IDCount {
    private String id;
    private double count;
}
