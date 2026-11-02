package com.cvmanager.auth.provider.user;

import org.junit.jupiter.api.Test;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.cvmanager.auth.provider.Constants;
import com.cvmanager.auth.provider.user.pojos.UserObject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

class UserAdapterTest {

    private UserObject createEntity() {
        UserObject entity = new UserObject();
        entity.setId("keycloak_id");
        entity.setCreatedTimestamp(1730828047000L);
        entity.setSuperUser(false);
        return entity;
    }

    private PreparedStatement mockUpdateStatement(MockedStatic<CustomUserStorageProvider> mockedStatic)
            throws SQLException {
        Connection connection = mock(Connection.class);
        mockedStatic.when(() -> CustomUserStorageProvider.getConnection(any())).thenReturn(connection);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString(), any(Integer.class))).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(mock(ResultSet.class));
        return preparedStatement;
    }

    @Test
    void setSingleAttribute_superUserTrueIsPersistedAsBoolean() throws SQLException {
        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
            PreparedStatement preparedStatement = mockUpdateStatement(mockedStatic);
            UserAdapter adapter = new UserAdapter(mock(KeycloakSession.class), mock(RealmModel.class),
                    mock(ComponentModel.class), createEntity());

            adapter.setSingleAttribute(Constants.SUPER_USER_KEY, "true");

            assertThat(adapter.getSuperUser(), is(true));
            verify(preparedStatement).setObject(5, true, Types.BOOLEAN);
        }
    }

    @Test
    void setSingleAttribute_superUserNullClearsValue() throws SQLException {
        try (MockedStatic<CustomUserStorageProvider> mockedStatic = Mockito
                .mockStatic(CustomUserStorageProvider.class)) {
            PreparedStatement preparedStatement = mockUpdateStatement(mockedStatic);
            UserAdapter adapter = new UserAdapter(mock(KeycloakSession.class), mock(RealmModel.class),
                    mock(ComponentModel.class), createEntity());

            adapter.setSingleAttribute(Constants.SUPER_USER_KEY, null);

            assertThat(adapter.getSuperUser(), nullValue());
            verify(preparedStatement).setObject(5, null, Types.BOOLEAN);
        }
    }
}
