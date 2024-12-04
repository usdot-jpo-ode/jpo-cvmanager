/**
 * 
 */
package com.cvmanager.auth.provider.user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.cvmanager.auth.provider.user.CustomUserStorageProviderConstants.*;

import com.cvmanager.auth.provider.RuntimeStorageException;
import com.cvmanager.auth.provider.user.pojos.UserObject;

public class CustomUserStorageProvider implements UserStorageProvider,
        UserLookupProvider,
        UserQueryProvider,
        UserRegistrationProvider {

    private static final Logger log = LoggerFactory.getLogger(CustomUserStorageProvider.class);
    private final KeycloakSession ksession;
    private final ComponentModel model;

    private static final String baseUserQuery = """
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
             %s
            GROUP BY
                user_id,
                keycloak_id,
                email,
                first_name,
                last_name,
                created_timestamp,
                super_user
            %s
            ;""";

    public CustomUserStorageProvider(KeycloakSession ksession, ComponentModel model) {
        this.ksession = ksession;
        this.model = model;
    }

    public static Connection getConnection(ComponentModel config) throws SQLException{
        String driverClass = config.get(CONFIG_KEY_JDBC_DRIVER);
        try {
            Class.forName(driverClass);
        }
        catch(ClassNotFoundException nfe) {
            throw new RuntimeStorageException("Invalid JDBC driver: " + driverClass + ". Please check if your driver if properly installed");
        }
        
        return DriverManager.getConnection(config.get(CONFIG_KEY_JDBC_URL),
          config.get(CONFIG_KEY_DB_USERNAME),
          config.get(CONFIG_KEY_DB_PASSWORD));
    }

    private PreparedStatement prepareUserQuery(Connection c, String where, String end) {
        try {
            return c.prepareStatement(String.format(baseUserQuery, where, end));
        } catch (SQLException ex) {
            throw new RuntimeStorageException(ex);
        }
    }

    @Override
    public void close() {
        log.debug("close()");
    }

    @Override
    public UserAdapter getUserById(RealmModel realm, String id) {
        // Get user by keycloak_id
        StorageId sid = new StorageId(id);
        String userId = sid.getExternalId();
        log.debug("getUserById({})", userId);
        try (Connection c = getConnection(this.model)) {
            PreparedStatement st = this.prepareUserQuery(c, String.format("WHERE keycloak_id = '%s'::UUID", userId),
                    "");
            log.debug("getUserById: st={}", st);
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                return new UserAdapter(ksession, realm, model, UserObject.fromJoinedResultSet(rs));
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeStorageException(ex);
        }
    }

    @Override
    public UserAdapter getUserByUsername(RealmModel realm, String username) {
        log.debug("getUserByUsername({})", username);
        try (Connection c = getConnection(this.model)) {
            PreparedStatement st = this.prepareUserQuery(c, String.format("WHERE email = '%s'", username), "");
            log.debug("getUserByUsername: st={}", st);
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                return new UserAdapter(ksession, realm, model, UserObject.fromJoinedResultSet(rs));
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeStorageException(ex);
        }
    }

    @Override
    public UserAdapter getUserByEmail(RealmModel realm, String email) {
        log.debug("getUserByEmail({})", email);
        try (Connection c = getConnection(this.model)) {
            PreparedStatement st = this.prepareUserQuery(c, String.format("WHERE email = '%s'", email), "");
            log.debug("getUserByEmail: st={}", st);
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                return new UserAdapter(ksession, realm, model, UserObject.fromJoinedResultSet(rs));
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeStorageException(ex);
        }
    }

    
    // UserQueryProvider implementation
    @Override
    public int getUsersCount(RealmModel realm) {
        log.debug("getUsersCount: realm={}", realm.getName());
        try (Connection c = getConnection(this.model)) {
            Statement st = c.createStatement();
            st.execute("select count(*) from public.users");
            log.debug("getUsersCount: st={}", st);
            ResultSet rs = st.getResultSet();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            throw new RuntimeStorageException(ex);
        }
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult,
            Integer maxResults) {
        log.debug("getGroupMembersStream: realm={}", realm.getName());

        try (Connection c = getConnection(this.model)) {
            PreparedStatement st = this.prepareUserQuery(c, "",
                    String.format("order by email limit %o offset %o", maxResults, firstResult));
            log.debug("getGroupMembersStream: st={}", st);
            st.execute();
            ResultSet rs = st.getResultSet();
            List<UserModel> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new UserAdapter(ksession, realm, model, UserObject.fromJoinedResultSet(rs)));
            }
            return users.stream();
        } catch (SQLException ex) {
            throw new RuntimeStorageException(ex);
        }
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult,
            Integer maxResults) {
        log.debug("searchForUserStream: realm={}", realm.getName());

        try (Connection c = getConnection(this.model)) {
            PreparedStatement st = this.prepareUserQuery(c,
                    search.equals("*") ? "" : String.format("WHERE email like %s", "%" + search + "%"),
                    String.format("order by email limit %s offset %s", maxResults, firstResult));
            log.debug("searchForUserStream: st={}", st);
            st.execute();
            ResultSet rs = st.getResultSet();
            List<UserModel> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new UserAdapter(ksession, realm, model, UserObject.fromJoinedResultSet(rs)));
            }
            return users.stream();
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult,
            Integer maxResults) {
        return getGroupMembersStream(realm, null, firstResult, maxResults);
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        log.debug("searchForUserByUserAttributeStream: realm={}, attrName={}, attrValue={}", realm.getName(),
                attrName, attrValue);
        try (Connection c = getConnection(this.model)) {
            PreparedStatement st = this.prepareUserQuery(c,
                    String.format("WHERE %s = %s", attrName, attrValue),
                    "order by email");
            log.debug("searchForUserByUserAttributeStream: st={}", st);
            ResultSet rs = st.executeQuery();
            List<UserModel> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new UserAdapter(ksession, realm, model, UserObject.fromJoinedResultSet(rs)));
            }
            return users.stream();
        } catch (SQLException ex) {
            throw new RuntimeStorageException(ex);
        }
    }


    // UserRegistrationProvider implementation
    @Override
    public UserAdapter addUser(RealmModel realm, String username) {
        log.debug("addUser: realm={}", realm.getName());
        String id = UUID.randomUUID().toString();
        Long now = System.currentTimeMillis();
        try (Connection c = getConnection(this.model)) {
            // insert new user with username into db
            PreparedStatement st = c.prepareStatement(
                    "insert into public.users (email, keycloak_id, created_timestamp) values (?, ?::UUID, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, username);
            st.setString(2, id);
            st.setLong(3, now);
            log.debug("addUser: st={}", st);
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            UserAdapter user = null;
            if (rs.next()) {
                user = new UserAdapter(ksession, realm, model, UserObject.fromResultSet(rs));
            }
            return user;
        } catch (SQLException ex) {
            throw new RuntimeStorageException(ex);
        }
    }

    public UserAdapter updateUser(RealmModel realm, UserObject user) {
        log.debug("updateUser: realm={}, id={}", realm.getName(), user.getId());
        try (Connection c = getConnection(this.model)) {
            // insert new user with ID into db
            PreparedStatement st = c.prepareStatement(
                    "update public.users set email = ?, first_name = ?, last_name = ?, created_timestamp = ?, super_user = ?::bit where keycloak_id = ?::UUID",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, user.getEmail());
            st.setString(2, user.getFirstName());
            st.setString(3, user.getLastName());
            st.setLong(4, user.getCreatedTimestamp());
            st.setInt(5, user.getSuperUser());
            st.setString(6, user.getId());
            log.debug("updateUser: st={}", st);
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            UserAdapter returnedUser = null;
            if (rs.next()) {
                returnedUser = new UserAdapter(ksession, realm, model, UserObject.fromResultSet(rs));
            }
            return returnedUser;
        } catch (SQLException ex) {
            throw new RuntimeStorageException(ex);
        }
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        // Delete user as well as user organization associations
        log.debug("removeUser: realm={}", realm.getName());
        try (Connection c = getConnection(this.model)) {
            // remove user with ID from db
            PreparedStatement st = c.prepareStatement(
                    "delete from public.users where keycloak_id = ?::UUID");
            st.setString(1, user.getId());
            log.debug("removeUser: st={}", st);
            int rowsAffected = st.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            throw new RuntimeStorageException(ex);
        }
    }
}
