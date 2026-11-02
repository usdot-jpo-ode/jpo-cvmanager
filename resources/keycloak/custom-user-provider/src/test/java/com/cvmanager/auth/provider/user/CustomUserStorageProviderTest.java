package com.cvmanager.auth.provider.user;

import org.junit.jupiter.api.Test;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupModel;
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
import static org.mockito.Mockito.never;
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
import java.sql.Types;
import java.util.List;
import java.util.stream.Collectors;

class CustomUserStorageProviderTest {

    private ResultSet createResultSet() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("keycloak_id")).thenReturn("keycloak_id");
        when(resultSet.getInt(Constants.USER_ID_KEY)).thenReturn(1);
        when(resultSet.getString(Constants.EMAIL_KEY)).thenReturn("email");
        when(resultSet.getString(Constants.FIRST_NAME_KEY)).thenReturn("first_name");
        when(resultSet.getString(Constants.LAST_NAME_KEY)).thenReturn("last_name");
        when(resultSet.getLong(Constants.CREATED_TIMESTAMP_KEY)).thenReturn(1730828047000L);
        when(resultSet.getBoolean(Constants.SUPER_USER_KEY)).thenReturn(true);
        when(resultSet.getString(Constants.ORGANIZATIONS_KEY)).thenReturn(
                "[{\"org_id\": 1, \"org_name\": \"test org 1\", \"org_email\": \"email1\", \"role\": \"test role 1\"}, {\"org_id\": 2, \"org_name\": \"test org 2\", \"org_email\": \"email2\", \"role\": \"test role 2\"}]");
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
        assertThat(userAdapter.getSuperUser(), is(true));
        assertThat(userAdapter.getOrganizations().size(), is(2));
        assertThat(userAdapter.getOrganizations().get(0).getOrgId(), is(1));
        assertThat(userAdapter.getOrganizations().get(0).getOrgName(), is("test org 1"));
        assertThat(userAdapter.getOrganizations().get(0).getOrgEmail(), is("email1"));
        assertThat(userAdapter.getOrganizations().get(0).getRole(), is("test role 1"));
        assertThat(userAdapter.getOrganizations().get(1).getOrgId(), is(2));
        assertThat(userAdapter.getOrganizations().get(1).getOrgName(), is("test org 2"));
        assertThat(userAdapter.getOrganizations().get(1).getOrgEmail(), is("email2"));
        assertThat(userAdapter.getOrganizations().get(1).getRole(), is("test role 2"));
    }

    @Test
    void getUserById() throws SQLException {
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
                            jsonb_build_object('org_id', org_id, 'org_name', org_name, 'org_email', org_email, 'role', role)
                        ) FILTER (WHERE org_id IS NOT NULL AND role IS NOT NULL),
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
                        org.organization_id AS org_id,
                        org.name AS org_name,
                        org.email AS org_email,
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
                WHERE keycloak_id = ?::UUID
                GROUP BY
                    user_id,
                    keycloak_id,
                    email,
                    first_name,
                    last_name,
                    created_timestamp,
                    super_user
                ;""";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
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
            verify(preparedStatement).setObject(1, userId);
            validateUserModel(response);
        }
    }

    @Test
    void getUserByUsername() throws SQLException {
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
                            jsonb_build_object('org_id', org_id, 'org_name', org_name, 'org_email', org_email, 'role', role)
                        ) FILTER (WHERE org_id IS NOT NULL AND role IS NOT NULL),
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
                        org.organization_id AS org_id,
                        org.name AS org_name,
                        org.email AS org_email,
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
                WHERE email = ?
                GROUP BY
                    user_id,
                    keycloak_id,
                    email,
                    first_name,
                    last_name,
                    created_timestamp,
                    super_user
                ;""";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
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
            verify(preparedStatement).setObject(1, username);
            validateUserModel(response);
        }
    }

    @Test
    void getUserByEmail() throws SQLException {
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
                            jsonb_build_object('org_id', org_id, 'org_name', org_name, 'org_email', org_email, 'role', role)
                        ) FILTER (WHERE org_id IS NOT NULL AND role IS NOT NULL),
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
                        org.organization_id AS org_id,
                        org.name AS org_name,
                        org.email AS org_email,
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
                WHERE email = ?
                GROUP BY
                    user_id,
                    keycloak_id,
                    email,
                    first_name,
                    last_name,
                    created_timestamp,
                    super_user
                ;""";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

            ResultSet resultSet = createResultSet();
            when(preparedStatement.getResultSet()).thenReturn(resultSet);

            CustomUserStorageProvider customUserStorageProvider = new CustomUserStorageProvider(keycloakSession, model);
            UserAdapter response = customUserStorageProvider.getUserByEmail(realmModel, email);
            verify(preparedStatement).execute();
            verify(connection).prepareStatement(expectedQuery);
            verify(preparedStatement).setObject(1, email);
            validateUserModel(response);
        }
    }

    @Test
    void getUsersCount() throws SQLException {
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        String expectedQuery = "select count(*) from public.users";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
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

    @Test
    void addUser() throws SQLException {
        String username = "email";
        Long now = 1730828047000L;

        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        String expectedQuery = "insert into public.users (email, first_name, last_name, keycloak_id, created_timestamp) values (?, ?, ?, ?::UUID, ?) returning keycloak_id, user_id, email, first_name, last_name, created_timestamp, super_user";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.getString("keycloak_id")).thenReturn("keycloak_id");
            when(resultSet.getInt(Constants.USER_ID_KEY)).thenReturn(1);
            when(resultSet.getString(Constants.EMAIL_KEY)).thenReturn("email");
            when(resultSet.getString(Constants.FIRST_NAME_KEY)).thenReturn("");
            when(resultSet.getString(Constants.LAST_NAME_KEY)).thenReturn("");
            when(resultSet.getLong(Constants.CREATED_TIMESTAMP_KEY)).thenReturn(now);
            when(resultSet.getBoolean(Constants.SUPER_USER_KEY)).thenReturn(false);
            when(resultSet.next()).thenReturn(true);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);

            CustomUserStorageProvider customUserStorageProvider = new CustomUserStorageProvider(keycloakSession, model);
            UserAdapter response = customUserStorageProvider.addUser(realmModel, username);
            verify(connection).prepareStatement(expectedQuery);
            verify(preparedStatement).setString(1, "email");
            verify(preparedStatement).setString(2, "");
            verify(preparedStatement).setString(3, "");
            verify(preparedStatement).setString(eq(4), anyString());
            verify(preparedStatement).setLong(eq(5), anyLong());
            verify(preparedStatement).executeQuery();
            assertThat(response.getId(), is("keycloak_id"));
            assertThat(response.getUserId(), is(1));
            assertThat(response.getUsername(), is("email"));
            assertThat(response.getFirstName(), is(""));
            assertThat(response.getLastName(), is(""));
            assertThat(response.getCreatedTimestamp(), is(now));
            assertThat(response.getSuperUser(), is(false));
            assertThat(response.getOrganizations(), nullValue());
        }
    }

    @Test
    void updateUser() throws SQLException {
        Long now = 1730828047000L;

        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);

        String expectedQuery = "update public.users set email = ?, first_name = ?, last_name = ?, created_timestamp = ?, super_user = ? where keycloak_id = ?::UUID";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            ComponentModel model = mock(ComponentModel.class);
            UserObject user = mock(UserObject.class);
            when(user.getId()).thenReturn("keycloak_id");
            when(user.getEmail()).thenReturn("email");
            when(user.getFirstName()).thenReturn("first_name");
            when(user.getLastName()).thenReturn("last_name");
            when(user.getSuperUser()).thenReturn(true);
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
            when(resultSet.getBoolean(Constants.SUPER_USER_KEY)).thenReturn(false);
            when(resultSet.next()).thenReturn(true);
            when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);

            CustomUserStorageProvider customUserStorageProvider = new CustomUserStorageProvider(keycloakSession, model);
            UserAdapter response = customUserStorageProvider.updateUser(realmModel, user);
            verify(connection).prepareStatement(expectedQuery, Statement.RETURN_GENERATED_KEYS);
            verify(preparedStatement).setString(1, "email");
            verify(preparedStatement).setString(2, "first_name");
            verify(preparedStatement).setString(3, "last_name");
            verify(preparedStatement).setLong(4, now);
            verify(preparedStatement).setObject(5, true, Types.BOOLEAN);
            verify(preparedStatement).setString(6, "keycloak_id");
            verify(preparedStatement).executeUpdate();
            assertThat(response.getId(), is("keycloak_id"));
            assertThat(response.getUserId(), is(1));
            assertThat(response.getUsername(), is("email"));
            assertThat(response.getFirstName(), nullValue());
            assertThat(response.getLastName(), nullValue());
            assertThat(response.getCreatedTimestamp(), is(now));
            assertThat(response.getSuperUser(), is(false));
            assertThat(response.getOrganizations(), nullValue());
        }
    }

    @Test
    void removeUser() throws SQLException {
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        String expectedQuery = "delete from public.users where keycloak_id = ?::UUID";

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
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

    @Test
    void searchForUserStream_bindsSearchAndPagingAsParameters() throws SQLException {
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            ResultSet resultSet = createResultSet();
            when(resultSet.next()).thenReturn(true, false);
            when(preparedStatement.getResultSet()).thenReturn(resultSet);

            String search = "x' OR '1'='1";
            CustomUserStorageProvider provider = new CustomUserStorageProvider(keycloakSession, model);
            List<UserModel> users = provider.searchForUserStream(realmModel, search, 20, 10)
                    .collect(Collectors.toList());

            verify(connection).prepareStatement(Mockito.argThat(
                    (String q) -> q.contains("WHERE email LIKE ?") && q.endsWith("ORDER BY email LIMIT ? OFFSET ?;")
                            && !q.contains(search)));
            verify(preparedStatement).setObject(1, "%" + search + "%");
            verify(preparedStatement).setObject(2, 10);
            verify(preparedStatement).setObject(3, 20);
            assertThat(users.size(), is(1));
        }
    }

    @Test
    void searchForUserStream_wildcardOmitsWhereClause() throws SQLException {
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            ResultSet resultSet = createResultSet();
            when(resultSet.next()).thenReturn(false);
            when(preparedStatement.getResultSet()).thenReturn(resultSet);

            CustomUserStorageProvider provider = new CustomUserStorageProvider(keycloakSession, model);
            provider.searchForUserStream(realmModel, "*", 0, 10).collect(Collectors.toList());

            verify(connection).prepareStatement(Mockito.argThat((String q) -> !q.contains("WHERE email")));
            verify(preparedStatement).setObject(1, 10);
            verify(preparedStatement).setObject(2, 0);
        }
    }

    @Test
    void searchForUserByUserAttributeStream_bindsValueForKnownAttribute() throws SQLException {
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            ResultSet resultSet = createResultSet();
            when(resultSet.next()).thenReturn(true, false);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);

            CustomUserStorageProvider provider = new CustomUserStorageProvider(keycloakSession, model);
            List<UserModel> users = provider.searchForUserByUserAttributeStream(realmModel, "firstName", "Jane")
                    .collect(Collectors.toList());

            verify(connection).prepareStatement(Mockito.argThat((String q) -> q.contains("WHERE first_name = ?")));
            verify(preparedStatement).setObject(1, "Jane");
            assertThat(users.size(), is(1));
        }
    }

    @Test
    void searchForUserByUserAttributeStream_unknownAttributeReturnsEmptyWithoutQuerying() throws SQLException {
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            CustomUserStorageProvider provider = new CustomUserStorageProvider(keycloakSession, model);
            List<UserModel> users = provider
                    .searchForUserByUserAttributeStream(realmModel, "1=1; DROP TABLE users; --", "x")
                    .collect(Collectors.toList());

            assertThat(users.isEmpty(), is(true));
            verify(connection, never()).prepareStatement(anyString());
        }
    }

    @Test
    void getGroupMembersStream_bindsPagingAsParameters() throws SQLException {
        KeycloakSession keycloakSession = mock(KeycloakSession.class);
        RealmModel realmModel = mock(RealmModel.class);
        ComponentModel model = mock(ComponentModel.class);

        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
            Connection connection = mock(Connection.class);
            mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);

            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            ResultSet resultSet = createResultSet();
            when(resultSet.next()).thenReturn(true, false);
            when(preparedStatement.getResultSet()).thenReturn(resultSet);

            CustomUserStorageProvider provider = new CustomUserStorageProvider(keycloakSession, model);
            List<UserModel> users = provider
                    .getGroupMembersStream(realmModel, mock(GroupModel.class), 5, 25)
                    .collect(Collectors.toList());

            verify(connection).prepareStatement(Mockito.argThat(
                    (String q) -> !q.contains("\nWHERE ") && q.endsWith("ORDER BY email LIMIT ? OFFSET ?;")));
            verify(preparedStatement).setObject(1, 25);
            verify(preparedStatement).setObject(2, 5);
            assertThat(users.size(), is(1));
        }
    }

}
