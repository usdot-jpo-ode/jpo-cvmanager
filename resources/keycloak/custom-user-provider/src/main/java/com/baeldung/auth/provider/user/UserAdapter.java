package com.baeldung.auth.provider.user;

import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import com.baeldung.auth.provider.user.pojos.OrganizationObject;
import com.baeldung.auth.provider.user.pojos.UserObject;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserAdapter extends AbstractUserAdapterFederatedStorage {
    private static final Logger log = Logger.getLogger(UserAdapter.class);
    protected UserObject entity;
    protected String keycloakId;
    final CustomUserStorageProvider provider;
    final RealmModel realm;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, UserObject entity) {
        super(session, realm, model);
        this.entity = entity;
        keycloakId = StorageId.keycloakId(model, entity.getId());
        provider = new CustomUserStorageProvider(session, model);
        this.realm = realm;
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
        log.info("Setting attribute: " + name + " to " + value);
        switch (name) {
            case UserModel.USERNAME:
                entity.setEmail(value);
                break;
            case UserModel.EMAIL:
                entity.setEmail(value);
                break;
            case UserModel.FIRST_NAME:
                entity.setFirstName(value);
                break;
            case UserModel.LAST_NAME:
                entity.setLastName(value);
                break;
            case "created_timestamp":
                entity.setCreatedTimestamp(value == null ? null : Long.valueOf(value));
                break;
            case "super_user":
                entity.setSuperUser(value == null ? null : Integer.valueOf(value));
                break;
            case "organizations":
                entity.setOrganizations(OrganizationObject.listFromString(value));
                break;
            default:
                super.setSingleAttribute(name, value);
        }
        save();
    }

    @Override
    public void removeAttribute(String name) {
        log.info("removeAttribute: " + name);
        switch (name) {
            case UserModel.USERNAME:
                entity.setEmail(null);
                break;
            case UserModel.EMAIL:
                entity.setEmail(null);
                break;
            case UserModel.FIRST_NAME:
                entity.setFirstName(null);
                break;
            case UserModel.LAST_NAME:
                entity.setLastName(null);
                break;
            case "created_timestamp":
                entity.setCreatedTimestamp(null);
                break;
            case "super_user":
                entity.setSuperUser(null);
                break;
            case "organizations":
                entity.setOrganizations(null);
                break;
            default:
                super.removeAttribute(name);
        }
        save();
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        log.info("setAttribute: " + name);
        if (values == null || values.isEmpty()) {
            removeAttribute(name);
        } else {
            setSingleAttribute(name, values.get(0));
        }
    }

    @Override
    public String getFirstAttribute(String name) {
        log.info("getFirstAttribute: " + name);
        switch (name) {
            case UserModel.USERNAME:
                return entity.getEmail();
            case UserModel.EMAIL:
                return entity.getEmail();
            case UserModel.FIRST_NAME:
                return entity.getFirstName();
            case UserModel.LAST_NAME:
                return entity.getLastName();
            case "created_timestamp":
                return String.valueOf(entity.getCreatedTimestamp());
            case "super_user":
                return String.valueOf(entity.getSuperUser());
            case "organizations":
                return entity.getOrganizations() != null ? OrganizationObject.toStringList(entity.getOrganizations())
                        : "[]";
            default:
                return super.getFirstAttribute(name);
        }
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        log.info("getAttributes");
        MultivaluedHashMap<String, String> attrs = new MultivaluedHashMap<>();
        attrs.add(UserModel.USERNAME, entity.getEmail());
        attrs.add(UserModel.EMAIL, entity.getEmail() != null ? entity.getEmail() : "");
        attrs.add(UserModel.FIRST_NAME, entity.getFirstName() != null ? entity.getFirstName() : "");
        attrs.add(UserModel.LAST_NAME, entity.getLastName() != null ? entity.getLastName() : "");
        attrs.add("created_timestamp",
                String.valueOf(entity.getCreatedTimestamp() != null ? entity.getCreatedTimestamp() : ""));
        attrs.add("super_user",
                String.valueOf(entity.getSuperUser() != null ? entity.getSuperUser() : ""));
        attrs.add("organizations",
                entity.getOrganizations() != null ? OrganizationObject.toStringList(entity.getOrganizations()) : "[]");
        // Include other attributes as needed
        return attrs;
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        log.info("getAttributeStream: " + name);
        switch (name) {
            case UserModel.USERNAME:
                return Stream.of(entity.getEmail());
            case UserModel.EMAIL:
                return entity.getEmail() != null ? Stream.of(entity.getEmail()) : Stream.empty();
            case UserModel.FIRST_NAME:
                return entity.getFirstName() != null ? Stream.of(entity.getFirstName()) : Stream.empty();
            case UserModel.LAST_NAME:
                return entity.getLastName() != null ? Stream.of(entity.getLastName()) : Stream.empty();
            case "created_timestamp":
                return entity.getCreatedTimestamp() != null ? Stream.of(String.valueOf(entity.getCreatedTimestamp()))
                        : Stream.empty();
            case "super_user":
                return entity.getSuperUser() != null ? Stream.of(String.valueOf(entity.getSuperUser()))
                        : Stream.empty();
            case "organizations":
                return entity.getOrganizations() != null
                        ? Stream.of(OrganizationObject.toStringList(entity.getOrganizations()))
                        : Stream.empty();
            default:
                return super.getAttributeStream(name);
        }
    }

    private void save() {
        provider.updateUser(realm, entity);
    }
}