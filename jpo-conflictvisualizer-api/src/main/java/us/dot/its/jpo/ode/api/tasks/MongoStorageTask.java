package us.dot.its.jpo.ode.api.tasks;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.bson.Document;

@Component
public class MongoStorageTask {
    

    @Autowired
    private MongoTemplate mongoTemplate;


    
    public void updateMongoIndex(String collectionName){

    }


    @Bean
    public void testMongoIndexUpdate(){
        Set<String> collectionNames = mongoTemplate.getCollectionNames();

        long totalDocumentSize = 0;

        for(String collectionName: collectionNames){
            Document collStatsCommand = new Document("collStats", collectionName);

            Document result = mongoTemplate.executeCommand(collStatsCommand);

            Document blockStore = getDocumentKey(getDocumentKey(result, "wiredTiger"), "block-manager");
            Long freeStorage = null;
            Long allocatedStorage = null;


            // Try Catch to Handle Datasets where data size varies widely
            try{
                freeStorage = blockStore.getLong("file bytes available for reuse");
            }catch(java.lang.ClassCastException e){
                freeStorage = blockStore.getInteger("file bytes available for reuse").longValue();
            }

            try{
                allocatedStorage = blockStore.getLong("file size in bytes");
            }catch(java.lang.ClassCastException e){
                allocatedStorage = blockStore.getInteger("file size in bytes").longValue();
            }

            if(freeStorage != null && allocatedStorage != null){
                Long trueSize = allocatedStorage - freeStorage;
                totalDocumentSize += trueSize;
            }

            
            

            
            
            break;
        }
        
    }

    public Document getDocumentKey(Document doc, String key){
        if(doc != null){
            Object obj = doc.get(key);
            if(obj != null){
                return (Document)obj;
            }
        }
        return null;
    }
}
