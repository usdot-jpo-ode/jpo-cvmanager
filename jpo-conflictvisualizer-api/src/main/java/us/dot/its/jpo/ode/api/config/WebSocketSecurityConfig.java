// package us.dot.its.jpo.ode.api.config;

// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
// import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

// @Configuration
// public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

//     @Override
//     protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
//         messages.anyMessage().authenticated();
//     }

//     @Override
//     protected boolean sameOriginDisabled() {
//         return true;
//     }

// }