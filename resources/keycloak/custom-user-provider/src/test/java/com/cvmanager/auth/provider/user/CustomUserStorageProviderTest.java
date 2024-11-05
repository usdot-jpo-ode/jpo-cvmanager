package com.cvmanager.auth.provider.user;

import org.junit.jupiter.api.Test;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.cvmanager.auth.provider.Constants;
import com.cvmanager.auth.provider.user.pojos.UserObject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomUserStorageProviderTest {

    private ResultSet createResultSet() throws SQLException {
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
        return resultSet;
    }

    private void validateUserModel(UserAdapter userAdapter) {
        assertThat(userAdapter.getId(), is("keycloak_id"));
        assertThat(userAdapter.getUserId(), is(1));
        assertThat(userAdapter.getUsername(), is("email"));
        assertThat(userAdapter.getFirstName(), is("first_name"));
        assertThat(userAdapter.getLastName(), is("last_name"));
        assertThat(userAdapter.getCreatedTimestamp(), is(1730828047000L));
        assertThat(userAdapter.getSuperUser(), is(1));
        assertThat(userAdapter.getOrganizations().size(), is(2));
        assertThat(userAdapter.getOrganizations().get(0).getOrg(), is("test org 1"));
        assertThat(userAdapter.getOrganizations().get(0).getRole(), is("test role 1"));
        assertThat(userAdapter.getOrganizations().get(1).getOrg(), is("test org 2"));
        assertThat(userAdapter.getOrganizations().get(1).getRole(), is("test role 2"));
    }

    @Test 
    public void getUserById() throws SQLException {
        String userId = "user_id";

        StorageId storageId = mock(StorageId.class);
        when(storageId.getExternalId()).thenReturn(userId);
        
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);


        String expectedQuery = """
SELECT
    keycloak_id,
    user_id,
    email,
    first_name,
    last_name,
    created_timestamp,
    super_user,
    COALESCE(
        jsonb_agg(
            jsonb_build_object('org', org_name, 'role', role)
        ) FILTER (WHERE org_name IS NOT NULL AND role IS NOT NULL),
        '[]'::jsonb
    ) AS organizations
FROM (
    SELECT
        users.keycloak_id,
        users.user_id,
        users.email,
        users.first_name,
        users.last_name,
        users.created_timestamp,
        users.super_user,
        org.name AS org_name,
        roles.name AS role
    FROM
        public.users
    LEFT JOIN
        public.user_organization AS uo ON uo.user_id = users.user_id
    LEFT JOIN
        public.organizations AS org ON org.organization_id = uo.organization_id
    LEFT JOIN
        public.roles ON roles.role_id = uo.role_id
) AS subquery
 WHERE keycloak_id = 'user_id'::UUID
GROUP BY
    user_id,
    keycloak_id,
    email,
    first_name,
    last_name,
    created_timestamp,
    super_user

;""";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito.mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

            ResultSet resultSet = createResultSet();
            when(preparedStatement.getResultSet()).thenReturn(resultSet);

            CustomUserStorageProvider customUserStorageProvider = new CustomUserStorageProvider(keycloakSession, model);
            UserAdapter response = customUserStorageProvider.getUserById(realmModel, userId);
            verify(preparedStatement).execute();
            verify(connection).prepareStatement(expectedQuery);
            validateUserModel(response);
        }
    }

    @Test
    public void getUserByUsername() throws SQLException {
        String username = "email";
        
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        String expectedQuery = """
SELECT
    keycloak_id,
    user_id,
    email,
    first_name,
    last_name,
    created_timestamp,
    super_user,
    COALESCE(
        jsonb_agg(
            jsonb_build_object('org', org_name, 'role', role)
        ) FILTER (WHERE org_name IS NOT NULL AND role IS NOT NULL),
        '[]'::jsonb
    ) AS organizations
FROM (
    SELECT
        users.keycloak_id,
        users.user_id,
        users.email,
        users.first_name,
        users.last_name,
        users.created_timestamp,
        users.super_user,
        org.name AS org_name,
        roles.name AS role
    FROM
        public.users
    LEFT JOIN
        public.user_organization AS uo ON uo.user_id = users.user_id
    LEFT JOIN
        public.organizations AS org ON org.organization_id = uo.organization_id
    LEFT JOIN
        public.roles ON roles.role_id = uo.role_id
) AS subquery
 WHERE email = 'email'
GROUP BY
    user_id,
    keycloak_id,
    email,
    first_name,
    last_name,
    created_timestamp,
    super_user

;""";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito.mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

            ResultSet resultSet = createResultSet();
            when(preparedStatement.getResultSet()).thenReturn(resultSet);

            CustomUserStorageProvider customUserStorageProvider = new CustomUserStorageProvider(keycloakSession, model);
            UserAdapter response = customUserStorageProvider.getUserByUsername(realmModel, username);
            verify(preparedStatement).execute();
            verify(connection).prepareStatement(expectedQuery);
            validateUserModel(response);
        }
    }

    @Test
    public void getUserByEmail() throws SQLException {
        String email = "email";
        
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        String expectedQuery = """
SELECT
    keycloak_id,
    user_id,
    email,
    first_name,
    last_name,
    created_timestamp,
    super_user,
    COALESCE(
        jsonb_agg(
            jsonb_build_object('org', org_name, 'role', role)
        ) FILTER (WHERE org_name IS NOT NULL AND role IS NOT NULL),
        '[]'::jsonb
    ) AS organizations
FROM (
    SELECT
        users.keycloak_id,
        users.user_id,
        users.email,
        users.first_name,
        users.last_name,
        users.created_timestamp,
        users.super_user,
        org.name AS org_name,
        roles.name AS role
    FROM
        public.users
    LEFT JOIN
        public.user_organization AS uo ON uo.user_id = users.user_id
    LEFT JOIN
        public.organizations AS org ON org.organization_id = uo.organization_id
    LEFT JOIN
        public.roles ON roles.role_id = uo.role_id
) AS subquery
 WHERE email = 'email'
GROUP BY
    user_id,
    keycloak_id,
    email,
    first_name,
    last_name,
    created_timestamp,
    super_user

;""";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito.mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

            ResultSet resultSet = createResultSet();
            when(preparedStatement.getResultSet()).thenReturn(resultSet);

            CustomUserStorageProvider customUserStorageProvider = new CustomUserStorageProvider(keycloakSession, model);
            UserAdapter response = customUserStorageProvider.getUserByUsername(realmModel, email);
            verify(preparedStatement).execute();
            verify(connection).prepareStatement(expectedQuery);
            validateUserModel(response);
        }
    }

    @Test
    public void getUsersCount() throws SQLException {
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        String expectedQuery = "select count(*) from public.users";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito.mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            Statement statement = mock(Statement.class);
            when(connection.createStatement()).thenReturn(statement);

            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(5);

            when(statement.getResultSet()).thenReturn(resultSet);

            CustomUserStorageProvider customUserStorageProvider = new CustomUserStorageProvider(keycloakSession, model);
            int response = customUserStorageProvider.getUsersCount(realmModel);
            verify(statement).execute(expectedQuery);
            assertThat(response, is(5));
        }
    }



    // // UserRegistrationProvider implementation
    // @Override
    // public UserModel addUser(RealmModel realm, String username) {
    //     log.debug("addUser: realm={}", realm.getName());
    //     String id = UUID.randomUUID().toString();
    //     Long now = System.currentTimeMillis();
    //     try (Connection c = getConnection(this.model)) {
    //         // insert new user with username into db
    //         PreparedStatement st = c.prepareStatement(
    //                 "insert into public.users (email, keycloak_id, created_timestamp) values (?, ?::UUID, ?)",
    //                 Statement.RETURN_GENERATED_KEYS);
    //         st.setString(1, username);
    //         st.setString(2, id);
    //         st.setLong(3, now);
    //         log.debug("addUser: st={}", st);
    //         st.executeUpdate();
    //         ResultSet rs = st.getGeneratedKeys();
    //         UserModel user = null;
    //         if (rs.next()) {
    //             user = new UserAdapter(ksession, realm, model, UserObject.fromResultSet(rs));
    //         }
    //         return user;
    //     } catch (SQLException ex) {
    //         throw new RuntimeStorageException(ex);
    //     }
    // }

    @Test
    public void addUser() throws SQLException {
        String username = "email";
        Long now = 1730828047000L;

        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        String expectedQuery = "insert into public.users (email, keycloak_id, created_timestamp) values (?, ?::UUID, ?)";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito.mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString(), any(Integer.class))).thenReturn(preparedStatement);

            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.getString("keycloak_id")).thenReturn("keycloak_id");
            when(resultSet.getInt(Constants.USER_ID_KEY)).thenReturn(1);
            when(resultSet.getString(Constants.EMAIL_KEY)).thenReturn("email");
            when(resultSet.getString(Constants.FIRST_NAME_KEY)).thenReturn(null);
            when(resultSet.getString(Constants.LAST_NAME_KEY)).thenReturn(null);
            when(resultSet.getLong(Constants.CREATED_TIMESTAMP_KEY)).thenReturn(now);
            when(resultSet.getInt(Constants.SUPER_USER_KEY)).thenReturn(0);
            when(resultSet.next()).thenReturn(true);
            when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);

            CustomUserStorageProvider customUserStorageProvider = new CustomUserStorageProvider(keycloakSession, model);
            UserAdapter response = customUserStorageProvider.addUser(realmModel, username);
            verify(connection).prepareStatement(expectedQuery, Statement.RETURN_GENERATED_KEYS);
            verify(preparedStatement).setString(1, "email");
            verify(preparedStatement).setString(eq(2), anyString());
            verify(preparedStatement).setLong(eq(3), anyLong());
            verify(preparedStatement).executeUpdate();
            assertThat(response.getId(), is("keycloak_id"));
            assertThat(response.getUserId(), is(1));
            assertThat(response.getUsername(), is("email"));
            assertThat(response.getFirstName(), nullValue());
            assertThat(response.getLastName(), nullValue());
            assertThat(response.getCreatedTimestamp(), is(now));
            assertThat(response.getSuperUser(), is(0));
            assertThat(response.getOrganizations(), nullValue());
        }
    }

    @Test
    public void updateUser() throws SQLException {
        Long now = 1730828047000L;

        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);

        String expectedQuery = "update public.users set email = ?, first_name = ?, last_name = ?, created_timestamp = ?, super_user = ?::bit where keycloak_id = ?::UUID";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito.mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);
            
            ComponentModel model = mock(ComponentModel.class);
            UserObject user = mock(UserObject.class);
            when(user.getId()).thenReturn("keycloak_id");
            when(user.getEmail()).thenReturn("email");
            when(user.getFirstName()).thenReturn("first_name");
            when(user.getLastName()).thenReturn("last_name");
            when(user.getSuperUser()).thenReturn(1);
            when(user.getCreatedTimestamp()).thenReturn(now);

            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString(), any(Integer.class))).thenReturn(preparedStatement);

            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.getString("keycloak_id")).thenReturn("keycloak_id");
            when(resultSet.getInt(Constants.USER_ID_KEY)).thenReturn(1);
            when(resultSet.getString(Constants.EMAIL_KEY)).thenReturn("email");
            when(resultSet.getString(Constants.FIRST_NAME_KEY)).thenReturn(null);
            when(resultSet.getString(Constants.LAST_NAME_KEY)).thenReturn(null);
            when(resultSet.getLong(Constants.CREATED_TIMESTAMP_KEY)).thenReturn(now);
            when(resultSet.getInt(Constants.SUPER_USER_KEY)).thenReturn(0);
            when(resultSet.next()).thenReturn(true);
            when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);

            CustomUserStorageProvider customUserStorageProvider = new CustomUserStorageProvider(keycloakSession, model);
            UserAdapter response = customUserStorageProvider.updateUser(realmModel, user);
            verify(connection).prepareStatement(expectedQuery, Statement.RETURN_GENERATED_KEYS);
            verify(preparedStatement).setString(1, "email");
            verify(preparedStatement).setString(2, "first_name");
            verify(preparedStatement).setString(3, "last_name");
            verify(preparedStatement).setLong(4, now);
            verify(preparedStatement).setInt(5, 1);
            verify(preparedStatement).setString(6, "keycloak_id");
            verify(preparedStatement).executeUpdate();
            assertThat(response.getId(), is("keycloak_id"));
            assertThat(response.getUserId(), is(1));
            assertThat(response.getUsername(), is("email"));
            assertThat(response.getFirstName(), nullValue());
            assertThat(response.getLastName(), nullValue());
            assertThat(response.getCreatedTimestamp(), is(now));
            assertThat(response.getSuperUser(), is(0));
            assertThat(response.getOrganizations(), nullValue());
        }
    }

    @Test
    public void removeUser() throws SQLException {
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        String expectedQuery = "delete from public.users where keycloak_id = ?::UUID";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito.mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            UserModel user = mock(UserModel.class);
            when(user.getId()).thenReturn("keycloak_id");

            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            CustomUserStorageProvider customUserStorageProvider = new CustomUserStorageProvider(keycloakSession, model);
            boolean response = customUserStorageProvider.removeUser(realmModel, user);
            verify(connection).prepareStatement(expectedQuery);
            verify(preparedStatement).setString(1, "keycloak_id");
            verify(preparedStatement).executeUpdate();

            assertThat(response, is(true));
        }
    }
        
}
