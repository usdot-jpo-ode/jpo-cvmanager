package com.baeldung.auth.provider.mapper;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.auth.provider.user.pojos.UserObject;

import java.util.ArrayList;
import java.util.List;

public class CustomProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper,
        OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final Logger log = LoggerFactory.getLogger(CustomProtocolMapper.class);
    public static final String PROVIDER_ID = "custom-protocol-mapper";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, CustomProtocolMapper.class);
    }

    @Override
    public String getDisplayCategory() {
        return "Token Mapper";
    }

    @Override
    public String getDisplayType() {
        return "Custom Token Mapper";
    }

    @Override
    public String getHelpText() {
        return "Adds help text to the claim";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public AccessToken transformAccessToken(AccessToken transformAccessToken, ProtocolMapperModel mappingModel,
            KeycloakSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        try {
            UserModel user = session.users().getUserById(session.getContext().getRealm(),
                    userSession.getUser().getId());

            // Map custom property to the access token
            transformAccessToken.getOtherClaims().put("cvmanager_data", UserObject.toMap(user));

            log.debug("Access token transformed: " + transformAccessToken.getOtherClaims().toString());
        } catch (Exception e) {
            log.error("Error transforming access token: " + e.getMessage());
            e.printStackTrace();
        }
        return transformAccessToken;
    }
}