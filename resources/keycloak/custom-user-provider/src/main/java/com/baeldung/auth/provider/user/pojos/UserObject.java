package com.baeldung.auth.provider.user.pojos;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class UserObject {
    private static final Logger log = LoggerFactory.getLogger(OrganizationObject.class);

    @JsonProperty("user_id")
    private String id;
    @JsonProperty("email")
    private String email;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("created_timestamp")
    private Long createdTimestamp;
    @JsonProperty("super_user")
    private Integer superUser;
    @JsonProperty("organizations")
    private List<OrganizationObject> organizations;

    public static UserObject fromResultSet(ResultSet re) {
        UserObject user = new UserObject();
        try {
            user.setId(re.getString("user_id"));
            user.setEmail(re.getString("email"));
            user.setFirstName(re.getString("first_name"));
            user.setLastName(re.getString("last_name"));
            user.setCreatedTimestamp(re.getLong("created_timestamp"));
            user.setSuperUser(re.getInt("super_user"));
            user.setOrganizations(OrganizationObject.listFromString(re.getString("organizations")));
        } catch (Exception e) {
            log.error("Error parsing UserObject from SQL Result: ", e);
        }
        return user;
    }

    public static Map<String, Object> toMap(UserModel user) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("user_created_timestamp", user.getCreatedTimestamp());
        map.put("super_user", user.getFirstAttribute("super_user"));
        map.put("organizations", OrganizationObject.mapListFromString(user.getFirstAttribute("organizations")));
        return map;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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