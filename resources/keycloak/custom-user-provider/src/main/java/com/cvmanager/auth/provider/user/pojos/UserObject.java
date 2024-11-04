package com.cvmanager.auth.provider.user.pojos;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cvmanager.auth.provider.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class UserObject {
    private static final Logger log = LoggerFactory.getLogger(UserObject.class);

    @JsonProperty("keycloak_id")
    private String id;
    @JsonProperty(Constants.USER_ID_KEY)
    private Integer userId;
    @JsonProperty(Constants.EMAIL_KEY)
    private String email;
    @JsonProperty(Constants.FIRST_NAME_KEY)
    private String firstName;
    @JsonProperty(Constants.LAST_NAME_KEY)
    private String lastName;
    @JsonProperty(Constants.CREATED_TIMESTAMP_KEY)
    private Long createdTimestamp;
    @JsonProperty(Constants.SUPER_USER_KEY)
    private Integer superUser;
    @JsonProperty(Constants.ORGANIZATIONS_KEY)
    private List<OrganizationObject> organizations;

    public static UserObject fromJoinedResultSet(ResultSet re) {
        UserObject user = new UserObject();
        try {
            user.setId(re.getString("keycloak_id"));
            user.setUserId(re.getInt(Constants.USER_ID_KEY));
            user.setEmail(re.getString(Constants.EMAIL_KEY));
            user.setFirstName(re.getString(Constants.FIRST_NAME_KEY));
            user.setLastName(re.getString(Constants.LAST_NAME_KEY));
            user.setCreatedTimestamp(re.getLong(Constants.CREATED_TIMESTAMP_KEY));
            user.setSuperUser(re.getInt(Constants.SUPER_USER_KEY));
            user.setOrganizations(OrganizationObject.listFromString(re.getString(Constants.ORGANIZATIONS_KEY)));
        } catch (Exception e) {
            log.error("Error parsing UserObject from SQL Result: ", e);
        }
        return user;
    }

    public static UserObject fromResultSet(ResultSet re) {
        UserObject user = new UserObject();
        try {
            user.setId(re.getString(Constants.KEYCLOAK_ID_KEY));
            user.setUserId(re.getInt(Constants.USER_ID_KEY));
            user.setEmail(re.getString(Constants.EMAIL_KEY));
            user.setFirstName(re.getString(Constants.FIRST_NAME_KEY));
            user.setLastName(re.getString(Constants.LAST_NAME_KEY));
            user.setCreatedTimestamp(re.getLong(Constants.CREATED_TIMESTAMP_KEY));
            user.setSuperUser(re.getInt(Constants.SUPER_USER_KEY));
            user.setOrganizations(null);
        } catch (Exception e) {
            log.error("Error parsing UserObject from SQL Result: ", e);
        }
        return user;
    }

    public static Map<String, Object> toTokenMap(UserModel user) {
        // Generate Map from user object, for use in custom token mapper. This only includes fields which are not already present in the token data.
        // - user_created_timestamp
        // - super_user
        // - organizations
        //     - org
        //     - role
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.CREATED_TIMESTAMP__TOKEN_KEY, user.getCreatedTimestamp());
        map.put(Constants.SUPER_USER_KEY, user.getFirstAttribute(Constants.SUPER_USER_KEY));
        map.put(Constants.ORGANIZATIONS_KEY, OrganizationObject.mapListFromString(user.getFirstAttribute(Constants.ORGANIZATIONS_KEY)));
        return map;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Integer getSuperUser() {
        return superUser;
    }

    public void setSuperUser(Integer superUser) {
        this.superUser = superUser;
    }

    public List<OrganizationObject> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<OrganizationObject> organizations) {
        this.organizations = organizations;
    }
}