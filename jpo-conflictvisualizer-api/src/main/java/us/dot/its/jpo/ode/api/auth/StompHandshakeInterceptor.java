package us.dot.its.jpo.ode.api.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.common.VerificationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StompHandshakeInterceptor implements HandshakeInterceptor {

    private final KeycloakSpringBootProperties configuration;

    @Override
    public boolean beforeHandshake(ServerHttpRequest req, ServerHttpResponse resp, WebSocketHandler h, Map<String, Object> atts) {
        
        try {

            for(String key: atts.keySet()){
                System.out.println("Attribute Key" + key);
                
            }

            System.out.println(configuration.getRealm());

            String token = getToken(req);

            System.out.println("Token: " + token);
            
            AdapterTokenVerifier.verifyToken(token, KeycloakDeploymentBuilder.build(configuration));
            resp.setStatusCode(HttpStatus.SWITCHING_PROTOCOLS);
            System.out.println("token valid");
        } catch (IndexOutOfBoundsException e) {
            resp.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        catch (VerificationException e) {
            resp.setStatusCode(HttpStatus.FORBIDDEN);
            System.out.println(e.getMessage());
            System.out.println();
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest rq, ServerHttpResponse rp, WebSocketHandler h, @Nullable Exception e) {}


    public String getToken(ServerHttpRequest req){

        HttpHeaders headers = req.getHeaders();
        if( headers != null){

            // for(String header: headers.keySet()){
            //     System.out.println(header);
            //     for(String value: headers.get(header)){
            //         System.out.println("    "+ value);
            //     }
            // }

            
            if(headers.containsKey("Token")){

                //Parse Token from Token Header
                if(headers.get("Token").size() > 0){
                    return headers.get("Token").get(0);
                }
                
            }else if(headers.containsKey("sec-websocket-protocol")){

                //Parse Token From Cookie
                List<String> cookies = req.getHeaders().get("sec-websocket-protocol");
                String[] parts = cookies.get(0).split(", ");
                if(parts.length >2){
                    return parts[2];
                }
            }
            else{
                return null;
            }
        }

        return null;
    }
}