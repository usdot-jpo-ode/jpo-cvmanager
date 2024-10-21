package com.baeldung.auth.provider.mapper;

import org.apache.commons.collections4.map.HashedMap;
import java.util.Map;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.auth.provider.user.UserAdapter;
import com.baeldung.auth.provider.user.pojos.OrganizationObject;
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
        return "Adds a Baeldung text to the claim";
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

            // // Fetch custom properties from CustomUserStorageProvider
            // String customProperty = user.getFirstAttribute("customProperty");

            // Map custom property to the access token
            transformAccessToken.getOtherClaims().put("cvmanager_data", UserObject.toMap(user));

            log.info("Access token transformed: " + transformAccessToken.getOtherClaims().toString());
            // transformAccessToken.getOtherClaims().put("password", user.getPassword());

            // String password = ;
            // transformAccessToken.getOtherClaims();
        } catch (Exception e) {
            log.error("Error transforming access token: " + e.getMessage());
            e.printStackTrace();
        }
        return transformAccessToken;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel,
            UserSessionModel userSession, KeycloakSession keycloakSession,
            ClientSessionContext clientSessionCtx) {
        // CustomUserStorageProvider customUserProvider =
        // keycloakSession.getProvider(CustomUserStorageProvider.class);
        // UserAdapter user =
        // customUserProvider.getUserByUsername(userSession.getRealm(),
        // userSession.getUser().getUsername());
        // token.setOtherClaims("setClaim", "setClaim");
        // // token.setOtherClaims("email", user.getEmail());
        // // token.setOtherClaims("firstName", user.getFirstName());
        // // token.setOtherClaims("lastName", user.getLastName());
        // // token.setOtherClaims("password", user.getPassword());
        // ProtocolMapperModel md = new ProtocolMapperModel();
        // md.setName("mapper_name");
        // md.setId("mapper_id");
        // md.setProtocolMapper(PROVIDER_ID);
        // HashedMap<String, String> map = new HashedMap<String, String>();
        // map.put("claim_name", "claim_value");
        // md.setConfig(map);
        // OIDCAttributeMapperHelper.mapClaim(token, md, "attributeValue");
        // OIDCAttributeMapperHelper.mapClaim(token, mappingModel, "Baeldung");
    }
}