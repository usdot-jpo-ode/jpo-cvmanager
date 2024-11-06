package com.cvmanager.auth.provider.user.pojos;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.Map;

public class OrganizationObjectTest {

    @Test
    public void listFromString() {
        List<OrganizationObject> orgs = OrganizationObject.listFromString("[{\"org\": \"test org 1\", \"role\": \"test role 1\"}, {\"org\": \"test org 2\", \"role\": \"test role 2\"}]");

        assertThat(orgs.size(), is(2));
        assertThat(orgs.get(0).getOrg(), is("test org 1"));
        assertThat(orgs.get(0).getRole(), is("test role 1"));
        assertThat(orgs.get(1).getOrg(), is("test org 2"));
        assertThat(orgs.get(1).getRole(), is("test role 2"));
    }

    @Test
    public void listFromStringEmptyList() {
        List<OrganizationObject> orgs = OrganizationObject.listFromString("[]");

        assertThat(orgs.size(), is(0));
    }

    @Test
    public void listFromStringEmpty() {
        List<OrganizationObject> orgs = OrganizationObject.listFromString(null);

        assertThat(orgs.size(), is(0));
    }

    @Test
    public void listFromStringInvalid() {
        List<OrganizationObject> orgs = OrganizationObject.listFromString("invalid");

        assertThat(orgs.size(), is(0));
    }

    @Test
    public void fromString() {
        OrganizationObject org = OrganizationObject.fromString("{\"org\": \"test org 1\", \"role\": \"test role 1\"}");

        assertThat(org.getOrg(), is("test org 1"));
        assertThat(org.getRole(), is("test role 1"));
    }

    @Test
    public void fromStringInvalid() {
        OrganizationObject org = OrganizationObject.fromString("invalid");

        assertThat(org, is(nullValue()));
    }

    @Test
    public void toStringList() {
        List<OrganizationObject> orgs = List.of(new OrganizationObject("test org 1", "test role 1"), new OrganizationObject("test org 2", "test role 2"));

        String json = OrganizationObject.toStringList(orgs);

        assertThat(json, is("[{\"org\":\"test org 1\",\"role\":\"test role 1\"},{\"org\":\"test org 2\",\"role\":\"test role 2\"}]"));
    }

    @Test
    public void toStringListEmpty() {
        List<OrganizationObject> orgs = List.of();

        String json = OrganizationObject.toStringList(orgs);

        assertThat(json, is("[]"));
    }

    @Test
    public void toStringListNull() {
        String json = OrganizationObject.toStringList(null);

        assertThat(json, is("[]"));
    }

    @Test
    public void toMap() {
        OrganizationObject org = new OrganizationObject("test org 1", "test role 1");

        Map<String, String> map = OrganizationObject.toMap(org);

        assertThat(map.get("org"), is("test org 1"));
        assertThat(map.get("role"), is("test role 1"));
    }

    @Test
    public void toMapNull() {
        Map<String, String> map = OrganizationObject.toMap(null);

        assertThat(map.size(), is(0));
    }

    @Test
    public void mapListFromString() {
        List<Map<String, String>> maps = OrganizationObject.mapListFromString("[{\"org\": \"test org 1\", \"role\": \"test role 1\"}, {\"org\": \"test org 2\", \"role\": \"test role 2\"}]");

        assertThat(maps.size(), is(2));
        assertThat(maps.get(0).get("org"), is("test org 1"));
        assertThat(maps.get(0).get("role"), is("test role 1"));
        assertThat(maps.get(1).get("org"), is("test org 2"));
        assertThat(maps.get(1).get("role"), is("test role 2"));
    }

    @Test
    public void mapListFromStringEmptyList() {
        List<Map<String, String>> maps = OrganizationObject.mapListFromString("[]");

        assertThat(maps.size(), is(0));
    }

    @Test
    public void mapListFromStringEmpty() {
        List<Map<String, String>> maps = OrganizationObject.mapListFromString(null);

        assertThat(maps.size(), is(0));
    }

    @Test
    public void mapListFromStringInvalid() {
        List<Map<String, String>> maps = OrganizationObject.mapListFromString("invalid");

        assertThat(maps.size(), is(0));
    }
}