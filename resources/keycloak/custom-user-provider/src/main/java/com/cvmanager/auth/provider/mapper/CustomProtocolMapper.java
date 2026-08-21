package com.cvmanager.auth.provider.mapper;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.storage.user.UserLookupProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cvmanager.auth.provider.user.pojos.UserObject;

import java.util.ArrayList;
import java.util.List;

public class CustomProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper,
        OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final Logger log = LoggerFactory.getLogger(CustomProtocolMapper.class);
    public static final String PROVIDER_ID = "custom-protocol-mapper";
    private static final String CUSTOM_USER_PROVIDER_ID = "custom-user-provider";

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
            RealmModel realm = session.getContext().getRealm();
            UserModel user = getFreshCustomProviderUser(session, realm, userSession.getUser());

            if (user == null) {
                user = session.users().getUserById(realm, userSession.getUser().getId());
            }


            // Add custom fields to the access token, under cvmanager_data. This only includes fields which are not already present in the access token:
            // - user_created_timestamp
            // - super_user
            // - organizations
            //     - org
            //     - role
            transformAccessToken.getOtherClaims().put("cvmanager_data", UserObject.toTokenMap(user));
        } catch (Exception e) {
            log.error("Error transforming access token: ", e);
        }
        return transformAccessToken;
    }

    private UserModel getFreshCustomProviderUser(KeycloakSession session, RealmModel realm, UserModel user) {
        if (session == null || realm == null || user == null || user.getUsername() == null) {
            return null;
        }

        ComponentModel providerComponent = realm.getComponentsStream(realm.getId(), UserStorageProvider.class.getName())
                .filter(component -> CUSTOM_USER_PROVIDER_ID.equals(component.getProviderId()))
                .findFirst()
                .orElse(null);

        if (providerComponent == null) {
            return null;
        }

        ProviderFactory<UserStorageProvider> providerFactory = session.getKeycloakSessionFactory()
                .getProviderFactory(UserStorageProvider.class, providerComponent.getProviderId());

        if (!(providerFactory instanceof UserStorageProviderFactory<?> factory)) {
            return null;
        }

        UserStorageProvider storageProvider = factory.create(session, providerComponent);
        if (storageProvider == null) {
            return null;
        }
        try {
            if (storageProvider instanceof UserLookupProvider userLookupProvider) {
                return userLookupProvider.getUserByUsername(realm, user.getUsername());
            }
            return null;
        } finally {
            storageProvider.close();
        }
    }
}