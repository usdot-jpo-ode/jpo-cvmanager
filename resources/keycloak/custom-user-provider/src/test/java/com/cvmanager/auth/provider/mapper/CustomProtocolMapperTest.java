package com.cvmanager.auth.provider.mapper;

import org.junit.jupiter.api.Test;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.AccessToken;

import com.cvmanager.auth.provider.Constants;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomProtocolMapperTest {
    @Test 
    public void transformAccessToken() {
        AccessToken accessToken = mock(AccessToken.class);
        ProtocolMapperModel mappingModel = mock(ProtocolMapperModel.class);
        KeycloakSession session = mock(KeycloakSession.class);
        UserSessionModel userSession = mock(UserSessionModel.class);
        ClientSessionContext clientSessionCtx = mock(ClientSessionContext.class);

        Long createdTimestamp = 1234567890L;
        String superUser = "1";
        String organizationsString = "[{\"org\":\"org1\",\"role\":\"role1\"}]";
        List<Map<String, String>> organizations = List.of(Map.of("org", "org1", "role", "role1"));

        // mock userSession.getUser().getId() to return a string
        UserModel userModel = mock(UserModel.class);
        when(userSession.getUser()).thenReturn(userModel);
        when(userModel.getId()).thenReturn("user_session_id");

        // mock session.getContext().getRealm() to return a realm
        RealmModel realm = mock(RealmModel.class);
        KeycloakContext keycloakContext = mock(KeycloakContext.class);
        when(session.getContext()).thenReturn(keycloakContext);
        when(keycloakContext.getRealm()).thenReturn(realm);

        // mock session.users().getUserById(realm, "user_session_id") to return a UserModel object
        UserModel user = mock(UserModel.class);
        when(user.getCreatedTimestamp()).thenReturn(createdTimestamp);
        when(user.getFirstAttribute(Constants.SUPER_USER_KEY)).thenReturn(superUser);
        when(user.getFirstAttribute(Constants.ORGANIZATIONS_KEY)).thenReturn(organizationsString);

        // Mock 
        UserProvider userProvider = mock(UserProvider.class);
        when(session.users()).thenReturn(userProvider);
        when(userProvider.getUserById(realm, "user_session_id")).thenReturn(user);

        CustomProtocolMapper customProtocolMapper = mock(CustomProtocolMapper.class);
        doCallRealMethod().when(customProtocolMapper).transformAccessToken(accessToken, mappingModel, session, userSession, clientSessionCtx);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> otherClaims = mock(Map.class);
        when(accessToken.getOtherClaims()).thenReturn(otherClaims);

        customProtocolMapper.transformAccessToken(accessToken, mappingModel, session, userSession, clientSessionCtx);

        Map<String, Object> expectedTokenMap = new HashMap<String, Object>();
        expectedTokenMap.put(Constants.CREATED_TIMESTAMP_TOKEN_KEY, createdTimestamp);
        expectedTokenMap.put(Constants.SUPER_USER_KEY, superUser);
        expectedTokenMap.put(Constants.ORGANIZATIONS_KEY, organizations);

        // verify that the put method was called correctly
        verify(otherClaims).put("cvmanager_data", expectedTokenMap);
    }
}
