package com.cvmanager.auth.provider.user;

import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import com.cvmanager.auth.provider.user.pojos.OrganizationObject;
import com.cvmanager.auth.provider.user.pojos.UserObject;
import com.cvmanager.auth.provider.Constants;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {
    private static final Logger log = Logger.getLogger(UserAdapter.class);
    protected UserObject entity;
    protected String keycloakId;
    final CustomUserStorageProvider provider;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, UserObject entity) {
        super(session, realm, model);
        this.entity = entity;
        keycloakId = StorageId.keycloakId(model, entity.getId());
        provider = new CustomUserStorageProvider(session, model);
        this.realm = realm;
    }

    public Integer getUserId() {
        return entity.getUserId();
    }

    public void setUserId(Integer userId) {
        entity.setUserId(userId);
    }

    public Integer getSuperUser() {
        return entity.getSuperUser();
    }

    public void setSuperUser(Integer superUser) {
        entity.setSuperUser(superUser);
    }

    public List<OrganizationObject> getOrganizations() {
        return entity.getOrganizations();
    }

    public void setOrganizations(List<OrganizationObject> organizations) {
        entity.setOrganizations(organizations);
    }

    @Override
    public String getUsername() {
        return entity.getEmail();
    }

    @Override
    public void setUsername(String username) {
        entity.setEmail(username);
    }

    @Override
    public String getEmail() {
        return entity.getEmail();
    }

    @Override
    public void setEmail(String email) {
        entity.setEmail(email);
    }

    @Override
    public String getFirstName() {
        return entity.getFirstName();
    }

    @Override
    public void setFirstName(String firstName) {
        entity.setFirstName(firstName);
    }

    @Override
    public String getLastName() {
        return entity.getLastName();
    }

    @Override
    public void setLastName(String lastName) {
        entity.setLastName(lastName);
    }

    @Override
    public Long getCreatedTimestamp() {
        return entity.getCreatedTimestamp();
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        entity.setCreatedTimestamp(timestamp);
    }

    @Override
    public String getId() {
        return keycloakId;
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        log.debug("Setting attribute: " + name + " to " + value);
        switch (name) {
            case UserModel.USERNAME, UserModel.EMAIL -> entity.setEmail(value);
            case UserModel.FIRST_NAME -> entity.setFirstName(value);
            case UserModel.LAST_NAME -> entity.setLastName(value);
            case Constants.CREATED_TIMESTAMP_KEY -> entity.setCreatedTimestamp(value == null ? null : Long.valueOf(value));
            case Constants.SUPER_USER_KEY -> entity.setSuperUser(value == null ? null : Integer.valueOf(value));
            case Constants.ORGANIZATIONS_KEY -> entity.setOrganizations(OrganizationObject.listFromString(value));
            default -> super.setSingleAttribute(name, value);
        }
        saveUpdatedUserAttributes(entity);
    }

    @Override
    public void removeAttribute(String name) {
        log.debug("removeAttribute: " + name);
        switch (name) {
            case UserModel.USERNAME, UserModel.EMAIL -> entity.setEmail(null);
            case UserModel.FIRST_NAME -> entity.setFirstName(null);
            case UserModel.LAST_NAME -> entity.setLastName(null);
            case Constants.CREATED_TIMESTAMP_KEY -> entity.setCreatedTimestamp(null);
            case Constants.SUPER_USER_KEY -> entity.setSuperUser(null);
            case Constants.ORGANIZATIONS_KEY -> entity.setOrganizations(null);
            default -> super.removeAttribute(name);
        }
        saveUpdatedUserAttributes(entity);
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        log.debug("setAttribute: " + name);
        if (values == null || values.isEmpty()) {
            removeAttribute(name);
        } else {
            setSingleAttribute(name, values.get(0));
        }
    }

    @Override
    public String getFirstAttribute(String name) {
        log.debug("getFirstAttribute: " + name);
        return switch (name) {
            case UserModel.USERNAME, UserModel.EMAIL -> entity.getEmail();
            case UserModel.FIRST_NAME -> entity.getFirstName();
            case UserModel.LAST_NAME -> entity.getLastName();
            case Constants.CREATED_TIMESTAMP_KEY -> String.valueOf(entity.getCreatedTimestamp());
            case Constants.SUPER_USER_KEY -> String.valueOf(entity.getSuperUser());
            case Constants.ORGANIZATIONS_KEY -> OrganizationObject.toStringList(entity.getOrganizations());
            default -> super.getFirstAttribute(name);
        };
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        log.debug("getAttributes");
        MultivaluedHashMap<String, String> attrs = new MultivaluedHashMap<>();
        attrs.add(UserModel.USERNAME, entity.getEmail() != null ? entity.getEmail() : "");
        attrs.add(UserModel.EMAIL, entity.getEmail() != null ? entity.getEmail() : "");
        attrs.add(UserModel.FIRST_NAME, entity.getFirstName() != null ? entity.getFirstName() : "");
        attrs.add(UserModel.LAST_NAME, entity.getLastName() != null ? entity.getLastName() : "");
        attrs.add(Constants.CREATED_TIMESTAMP_KEY,
                String.valueOf(entity.getCreatedTimestamp() != null ? entity.getCreatedTimestamp() : ""));
        attrs.add(Constants.SUPER_USER_KEY,
                String.valueOf(entity.getSuperUser() != null ? entity.getSuperUser() : ""));
        attrs.add(Constants.ORGANIZATIONS_KEY,
                entity.getOrganizations() != null ? OrganizationObject.toStringList(entity.getOrganizations()) : "[]");
        return attrs;
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        log.debug("getAttributeStream: " + name);
        return switch (name) {
            case UserModel.USERNAME, UserModel.EMAIL -> Stream.ofNullable(entity.getEmail());
            case UserModel.FIRST_NAME -> Stream.ofNullable(entity.getFirstName());
            case UserModel.LAST_NAME -> Stream.ofNullable(entity.getLastName());
            case Constants.CREATED_TIMESTAMP_KEY -> Stream.ofNullable(String.valueOf(entity.getCreatedTimestamp()));
            case Constants.SUPER_USER_KEY -> Stream.ofNullable(String.valueOf(entity.getSuperUser()));
            case Constants.ORGANIZATIONS_KEY ->  Stream.ofNullable(OrganizationObject.toStringList(entity.getOrganizations()));
            default -> super.getAttributeStream(name);
        };
    }

    /**
     * Save the updated user object to the database, relying on the saved provider (CustomUserStorageProvider) and super.realm
     */
    private void saveUpdatedUserAttributes(UserObject entity) {
        provider.updateUser(super.realm, entity);
    }
}