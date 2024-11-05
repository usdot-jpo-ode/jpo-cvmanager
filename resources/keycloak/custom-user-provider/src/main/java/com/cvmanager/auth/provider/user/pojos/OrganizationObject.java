package com.cvmanager.auth.provider.user.pojos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
* OrganizationObject represents a user's organization and role association object from the user_organization postgresql table
*/
@JsonSerialize
public class OrganizationObject {

    private static final Logger log = LoggerFactory.getLogger(OrganizationObject.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    @JsonProperty("org")
    private String org;
    @JsonProperty("role")
    private String role;

    public OrganizationObject() {
    }

    public OrganizationObject(String org, String role) {
        this.org = org;
        this.role = role;
    }

    public static List<OrganizationObject> listFromString(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, OrganizationObject.class));
        } catch (JsonProcessingException e) {
            log.error("Error parsing OrganizationObject from JSON: {}", json, e);
            return Collections.emptyList();
        }
    }

    public static OrganizationObject fromString(String json) {
        try {
            return objectMapper.readValue(json, OrganizationObject.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing OrganizationObject from JSON: {}", json, e);
            return null;
        }
    }

    public static String toStringList(List<OrganizationObject> orgs) {
        try {
            if (orgs == null || orgs.isEmpty()) {
                return "[]";
            }
            return objectMapper.writeValueAsString(orgs);
        } catch (JsonProcessingException e) {
            log.error("Error serializing OrganizationObject to JSON", e);
            return "[]";
        }
    }

    public static Map<String, String> toMap(OrganizationObject org) {
        try {
            Map map = objectMapper.convertValue(org, Map.class);
            return map == null ? Collections.emptyMap() : map;
        } catch (Exception e) {
            log.error("Error converting OrganizationObject to Map", e);
            return Collections.emptyMap();
        }
    }

    public static List<Map<String, String>> mapListFromString(String json) {
        List<OrganizationObject> objs = listFromString(json);
        return objs == null ? null : objs.stream().map(OrganizationObject::toMap).toList();
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String toString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("Error serializing OrganizationObject to JSON", e);
            return "";
        }
    }
}