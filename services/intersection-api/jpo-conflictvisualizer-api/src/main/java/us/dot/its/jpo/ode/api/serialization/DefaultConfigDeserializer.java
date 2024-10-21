package us.dot.its.jpo.ode.api.serialization;

import us.dot.its.jpo.conflictmonitor.monitor.models.config.DefaultConfig;
import us.dot.its.jpo.conflictmonitor.monitor.serialization.deserialization.GenericJsonDeserializer;

public class DefaultConfigDeserializer extends GenericJsonDeserializer<DefaultConfig>{

    public DefaultConfigDeserializer() {
        super(DefaultConfig.class);
        //TODO Auto-generated constructor stub
    }
    
}
