package us.dot.its.jpo.ode.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import us.dot.its.jpo.ode.api.WebSocketControllers.LiveMapController;
import us.dot.its.jpo.ode.api.WebSocketControllers.LiveSpatController;
import us.dot.its.jpo.ode.api.controllers.LiveFeedController;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("Adding Live to Websocket Registry");
		registry.addHandler(new LiveSpatController(), "/live/spat").setAllowedOrigins("*");
	}

}