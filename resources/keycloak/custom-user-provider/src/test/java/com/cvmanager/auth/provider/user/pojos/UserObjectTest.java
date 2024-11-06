package com.cvmanager.auth.provider.user.pojos;

import org.junit.jupiter.api.Test;
import org.keycloak.models.UserModel;

import com.cvmanager.auth.provider.Constants;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

class UserObjectTest {

    @Test
    void fromJoinedResultSet() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("keycloak_id")).thenReturn("keycloak_id");
        when(resultSet.getInt(Constants.USER_ID_KEY)).thenReturn(1);
        when(resultSet.getString(Constants.EMAIL_KEY)).thenReturn("email");
        when(resultSet.getString(Constants.FIRST_NAME_KEY)).thenReturn("first_name");
        when(resultSet.getString(Constants.LAST_NAME_KEY)).thenReturn("last_name");
        when(resultSet.getLong(Constants.CREATED_TIMESTAMP_KEY)).thenReturn(1730828047000L);
        when(resultSet.getInt(Constants.SUPER_USER_KEY)).thenReturn(1);
        when(resultSet.getString(Constants.ORGANIZATIONS_KEY)).thenReturn("[{\"org\": \"test org 1\", \"role\": \"test role 1\"}, {\"org\": \"test org 2\", \"role\": \"test role 2\"}]");
        when(resultSet.next()).thenReturn(true);

        UserObject userObject = UserObject.fromJoinedResultSet(resultSet);

        assertThat(userObject.getId(), is("keycloak_id"));
        assertThat(userObject.getUserId(), is(1));
        assertThat(userObject.getFirstName(), is("first_name"));
        assertThat(userObject.getLastName(), is("last_name"));
        assertThat(userObject.getCreatedTimestamp(), is(1730828047000L));
        assertThat(userObject.getSuperUser(), is(1));
        assertThat(userObject.getOrganizations().size(), is(2));
        assertThat(userObject.getOrganizations().get(0).getOrg(), is("test org 1"));
        assertThat(userObject.getOrganizations().get(0).getRole(), is("test role 1"));
        assertThat(userObject.getOrganizations().get(1).getOrg(), is("test org 2"));
        assertThat(userObject.getOrganizations().get(1).getRole(), is("test role 2"));
    }

    @Test 
    void fromResultSet() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(Constants.KEYCLOAK_ID_KEY)).thenReturn("keycloak_id");
        when(resultSet.getInt(Constants.USER_ID_KEY)).thenReturn(1);
        when(resultSet.getString(Constants.EMAIL_KEY)).thenReturn("email");
        when(resultSet.getString(Constants.FIRST_NAME_KEY)).thenReturn("first_name");
        when(resultSet.getString(Constants.LAST_NAME_KEY)).thenReturn("last_name");
        when(resultSet.next()).thenReturn(true);

        UserObject userObject = UserObject.fromResultSet(resultSet);

        assertThat(userObject.getId(), is("keycloak_id"));
        assertThat(userObject.getUserId(), is(1));
        assertThat(userObject.getFirstName(), is("first_name"));
        assertThat(userObject.getLastName(), is("last_name"));
        assertThat(userObject.getCreatedTimestamp(), is(0L));
        assertThat(userObject.getSuperUser(), is(0));
        assertThat(userObject.getOrganizations(), is(nullValue()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void toTokenMap() {
        UserModel userModel = mock(UserModel.class);
        when(userModel.getCreatedTimestamp()).thenReturn(1730828047000L);
        when(userModel.getFirstAttribute(Constants.SUPER_USER_KEY)).thenReturn("1");
        when(userModel.getFirstAttribute(Constants.ORGANIZATIONS_KEY)).thenReturn("[{\"org\": \"test org 1\", \"role\": \"test role 1\"}, {\"org\": \"test org 2\", \"role\": \"test role 2\"}]");

        Map<String, Object> tokenMap = UserObject.toTokenMap(userModel);

        assertThat(tokenMap.get(Constants.CREATED_TIMESTAMP_TOKEN_KEY), is(1730828047000L));
        assertThat(tokenMap.get(Constants.SUPER_USER_KEY), is("1"));
        assertThat(tokenMap.get(Constants.ORGANIZATIONS_KEY), is(notNullValue()));
        assertThat(tokenMap.get(Constants.ORGANIZATIONS_KEY), isA(List.class));
        assertThat(((List<Map<String, String>>) tokenMap.get(Constants.ORGANIZATIONS_KEY)).size(), is(2));
        assertThat(((List<Map<String, String>>) tokenMap.get(Constants.ORGANIZATIONS_KEY)).get(0), isA(Map.class));
        assertThat(((List<Map<String, String>>) tokenMap.get(Constants.ORGANIZATIONS_KEY)).get(1), isA(Map.class));
        assertThat((((List<Map<String, String>>) tokenMap.get(Constants.ORGANIZATIONS_KEY)).get(0)).get("org"), is("test org 1"));
        assertThat((((List<Map<String, String>>) tokenMap.get(Constants.ORGANIZATIONS_KEY)).get(0)).get("role"), is("test role 1"));
        assertThat((((List<Map<String, String>>) tokenMap.get(Constants.ORGANIZATIONS_KEY)).get(1)).get("org"), is("test org 2"));
        assertThat((((List<Map<String, String>>) tokenMap.get(Constants.ORGANIZATIONS_KEY)).get(1)).get("role"), is("test role 2"));
    }
}