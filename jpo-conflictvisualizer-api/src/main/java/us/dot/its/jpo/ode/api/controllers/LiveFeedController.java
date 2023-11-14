package us.dot.its.jpo.ode.api.controllers;


import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import us.dot.its.jpo.ode.api.models.LiveFeedSessionIndex;


@Component
public class LiveFeedController extends TextWebSocketHandler {

	// private static CopyOnWriteArrayList<WebSocketSession>  = new CopyOnWriteArrayList<>();

	//Index Sessions based upon intersection to increase runtime and reduce data flow.
	private static ConcurrentHashMap<LiveFeedSessionIndex, CopyOnWriteArrayList<WebSocketSession>> sessionIndex = new ConcurrentHashMap<>();

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message)
			throws InterruptedException, IOException {

		
		String payload = message.getPayload();
		JSONObject jsonObject = new JSONObject(payload);

		if(jsonObject.keySet().contains("intersectionID") && jsonObject.keySet().contains("roadRegulatorID")){
			int intersectionID = jsonObject.getInt("intersectionID");
			String roadRegulatorID = jsonObject.getString("roadRegulatorID");
			
			System.out.println("Switching Session to Index:" + intersectionID + " " + roadRegulatorID);

			// Parse Components of Session
			LiveFeedSessionIndex index = new LiveFeedSessionIndex();
			index.setIntersectionID(intersectionID);
			index.setRoadRegulatorID(roadRegulatorID);

			// Move session to new Index
			removeSession(session);
			addSessionToIndex(session, index);
		}else{
			System.out.println("Unable to Find required Keys in Message payload");
		}
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception{
		LiveFeedSessionIndex index = new LiveFeedSessionIndex();
		index.setIntersectionID(-1);
		index.setRoadRegulatorID("-1");
		addSessionToIndex(session, index);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
		removeSession(session);
	}

	public static void broadcast(LiveFeedSessionIndex index, String message){

		// Don't send unregistered index
		if(index.getIntersectionID() == -1 && index.getRoadRegulatorID() == "-1"){
			return;
		}

		// only send messages to UI's with the specified intersections.
		if(sessionIndex.containsKey(index)){
			for(WebSocketSession session: sessionIndex.get(index)){
				try {
					session.sendMessage(new TextMessage(message));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// Add a session to the session index using the appropriate key
	public static void addSessionToIndex(WebSocketSession session, LiveFeedSessionIndex index){
		if(sessionIndex.containsKey(index)){
			sessionIndex.get(index).add(session);
		}else{
			CopyOnWriteArrayList<WebSocketSession> sessionList = new CopyOnWriteArrayList<>();
			sessionList.add(session);
			sessionIndex.put(index,sessionList);
		}
	}

	// Remove all references of a session from the session index
	public static void removeSession(WebSocketSession session){
		for(CopyOnWriteArrayList<WebSocketSession> list: sessionIndex.values()){
			if(list.contains(session)){
				list.remove(session);
			}
		}
	}

}