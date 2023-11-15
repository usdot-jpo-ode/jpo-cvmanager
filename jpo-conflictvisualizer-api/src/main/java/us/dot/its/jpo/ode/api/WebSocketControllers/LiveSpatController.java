package us.dot.its.jpo.ode.api.WebSocketControllers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.socket.WebSocketSession;

import us.dot.its.jpo.ode.api.controllers.LiveFeedController;
import us.dot.its.jpo.ode.api.models.LiveFeedSessionIndex;

public class LiveSpatController extends LiveFeedController{
    private static ConcurrentHashMap<LiveFeedSessionIndex, CopyOnWriteArrayList<WebSocketSession>> sessionIndex = new ConcurrentHashMap<>();

    @Override
    public ConcurrentHashMap<LiveFeedSessionIndex, CopyOnWriteArrayList<WebSocketSession>> getSessionIndex(){
        return sessionIndex;
    }

    
}
