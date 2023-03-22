package us.dot.its.jpo.ode.api.accessors.bsm;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.ode.model.OdeBsmData;

@Component
public class OdeBsmJsonRepositoryImpl  implements OdeBsmJsonRepository{

    @Autowired
    private MongoTemplate mongoTemplate;

    private ObjectMapper mapper = DateJsonMapper.getInstance();

    public Query getQuery(String originIp, String vehicleId, Long startTime, Long endTime){
        Query query = new Query();

        if(originIp != null){
            query.addCriteria(Criteria.where("metadata.originIp").is(originIp));
        }

        

        if(vehicleId != null){
            query.addCriteria(Criteria.where("payload.data.coreData.id").is(vehicleId));
        }

        String startTimeString = Instant.ofEpochMilli(0).toString();
        String endTimeString = Instant.now().toString();

        if(startTime != null){
            startTimeString = Instant.ofEpochMilli(startTime).toString(); 
        }
        if(endTime != null){
            endTimeString = Instant.ofEpochMilli(endTime).toString();
        }

        query.addCriteria(Criteria.where("metadata.odeReceivedAt").gte(startTimeString).lte(endTimeString));
        return query;
    }

    public long getQueryResultCount(Query query){
        return mongoTemplate.count(query, OdeBsmData.class, "OdeBsmJson");
    }

    public List<OdeBsmData> findOdeBsmData(Query query) {
        List<Map> documents = mongoTemplate.find(query, Map.class, "OdeBsmJson");
        List<OdeBsmData> convertedList = new ArrayList<>();
        for(Map document : documents){
            document.remove("_id");
            OdeBsmData bsm = mapper.convertValue(document, OdeBsmData.class);
            convertedList.add(bsm);
        }
        return convertedList;
    }

}