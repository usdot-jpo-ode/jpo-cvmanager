/**
 * 
 */
package com.baeldung.auth.provider.user;

import java.sql.Connection;
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

import com.baeldung.auth.provider.user.pojos.UserObject;

public class CustomUserStorageProvider implements UserStorageProvider,
        UserLookupProvider,
        UserQueryProvider,
        UserRegistrationProvider {

    private static final Logger log = LoggerFactory.getLogger(CustomUserStorageProvider.class);
    private KeycloakSession ksession;
    private ComponentModel model;

    private static String baseUserQuery = """
                        SELECT
                user_id,
                email,
                first_name,
                last_name,
                created_timestamp,
                super_user,
                receive_error_emails,
                jsonb_agg(
                    jsonb_build_object('org', org_name, 'role', role)
                ) AS organizations
            FROM (
                SELECT
                    users.user_id,
                    users.email,
                    users.first_name,
                    users.last_name,
                    users.created_timestamp,
                    users.super_user,
                    users.receive_error_emails,
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
                email,
                first_name,
                last_name,
                created_timestamp,
                super_user,
                receive_error_emails
            %s
            ;
                        """;

    public CustomUserStorageProvider(KeycloakSession ksession, ComponentModel model) {
        this.ksession = ksession;
        this.model = model;
    }

    private PreparedStatement getBaseUserQuery(Connection c, String where, String end) {
        try {
            return c.prepareStatement(String.format(baseUserQuery, where, end));
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public void close() {
        log.info("[I30] close()");
    }

    @Override
    public UserAdapter getUserById(RealmModel realm, String id) {
        StorageId sid = new StorageId(id);
        String userId = sid.getExternalId();
        log.info("[I41] getUserById({})", userId);
        try (Connection c = DbUtil.getConnection(this.model)) {
            PreparedStatement st = this.getBaseUserQuery(c, String.format("WHERE user_id = '%s'::UUID", userId),
                    "");
            log.info("[I41] getUserById: st={}", st);
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                return new UserAdapter(ksession, realm, model, UserObject.fromResultSet(rs));
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public UserAdapter getUserByUsername(RealmModel realm, String username) {
        log.info("[I41] getUserByUsername({})", username);
        try (Connection c = DbUtil.getConnection(this.model)) {
            PreparedStatement st = this.getBaseUserQuery(c, String.format("WHERE email = '%s'", username), "");
            log.info("[I41] getUserByUsername: st={}", st);
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                return new UserAdapter(ksession, realm, model, UserObject.fromResultSet(rs));
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        log.info("[I48] getUserByEmail({})", email);
        try (Connection c = DbUtil.getConnection(this.model)) {
            PreparedStatement st = this.getBaseUserQuery(c, String.format("WHERE email = '%s'", email), "");
            log.info("[I48] getUserByEmail: st={}", st);
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                return new UserAdapter(ksession, realm, model, UserObject.fromResultSet(rs));
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    // UserQueryProvider implementation

    @Override
    public int getUsersCount(RealmModel realm) {
        log.info("[I93] getUsersCount: realm={}", realm.getName());
        try (Connection c = DbUtil.getConnection(this.model)) {
            Statement st = c.createStatement();
            st.execute("select count(*) from public.users");
            log.info("[I93] getUsersCount: st={}", st);
            ResultSet rs = st.getResultSet();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult,
            Integer maxResults) {
        log.info("[I113] getUsers: realm={}", realm.getName());

        try (Connection c = DbUtil.getConnection(this.model)) {
            PreparedStatement st = this.getBaseUserQuery(c, "",
                    String.format("order by email limit %o offset %o", maxResults, firstResult));
            log.info("[I113] getUsers: st={}", st);
            st.execute();
            ResultSet rs = st.getResultSet();
            List<UserModel> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new UserAdapter(ksession, realm, model, UserObject.fromResultSet(rs)));
            }
            return users.stream();
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult,
            Integer maxResults) {
        log.info("[I139] searchForUser: realm={}", realm.getName());

        try (Connection c = DbUtil.getConnection(this.model)) {
            PreparedStatement st = this.getBaseUserQuery(c,
                    search.equals("*") ? "" : String.format("WHERE email like %s", "%" + search + "%"),
                    String.format("order by email limit %s offset %s", maxResults, firstResult));
            log.info("[I139] searchForUser: st={}", st);
            st.execute();
            ResultSet rs = st.getResultSet();
            List<UserModel> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new UserAdapter(ksession, realm, model, UserObject.fromResultSet(rs)));
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
        // TODO: Cast variables of certain types
        log.info("[I150] searchForUserByUserAttributeStream: realm={}, attrName={}, attrValue={}", realm.getName(),
                attrName, attrValue);
        try (Connection c = DbUtil.getConnection(this.model)) {
            PreparedStatement st = this.getBaseUserQuery(c,
                    String.format("WHERE %s = %s", attrName, attrValue),
                    String.format("order by email"));
            log.info("[I150] searchForUserByUserAttributeStream: st={}", st);
            ResultSet rs = st.executeQuery();
            List<UserModel> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new UserAdapter(ksession, realm, model, UserObject.fromResultSet(rs)));
            }
            return users.stream();
        } catch (SQLException ex) {
            log.error("Database error in searchForUserByUserAttributeStream:", ex);
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    // UserRegistrationProvider implementation

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        log.info("[Ijacob1] addUser: realm={}", realm.getName());
        String id = UUID.randomUUID().toString();
        Long now = System.currentTimeMillis();
        try (Connection c = DbUtil.getConnection(this.model)) {
            // insert new user with username into db
            PreparedStatement st = c.prepareStatement(
                    "insert into public.users (email, user_id, created_timestamp) values (?, ?::UUID, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, username);
            st.setString(2, id);
            st.setLong(3, now);
            log.info("[Ijacob2] addUser: st={}", st);
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            UserModel user = null;
            if (rs.next()) {
                user = new UserAdapter(ksession, realm, model, UserObject.fromResultSet(rs));
            }
            return user;
        } catch (SQLException ex) {
            throw new RuntimeException("Database error Creating User:" + ex.getMessage(), ex);
        }
    }

    public UserAdapter updateUser(RealmModel realm, UserObject user) {
        // TOOD: save organization data from list??
        log.info("[Ijacob3] updateUser: realm={}, id={}", realm.getName(), user.getId());
        try (Connection c = DbUtil.getConnection(this.model)) {
            // insert new user with username into db
            PreparedStatement st = c.prepareStatement(
                    "update public.users set email = ?, first_name = ?, last_name = ?, created_timestamp = ?, super_user = ?::bit, receive_error_emails = ?::bit where user_id = ?::UUID",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, user.getEmail());
            st.setString(2, user.getFirstName());
            st.setString(3, user.getLastName());
            st.setLong(4, user.getCreatedTimestamp());
            st.setInt(5, user.getSuperUser());
            st.setInt(6, user.getReceiveErrorEmails());
            st.setString(7, user.getId());
            log.info("[Ijacob2] updateUser: st={}", st);
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            UserAdapter returnedUser = null;
            if (rs.next()) {
                returnedUser = new UserAdapter(ksession, realm, model, UserObject.fromResultSet(rs));
            }
            return returnedUser;
        } catch (SQLException ex) {
            throw new RuntimeException("Database error Updating User:" + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        // Delete user organization associations as well
        log.info("[Ijacob2] removeUser: realm={}", realm.getName());
        try (Connection c = DbUtil.getConnection(this.model)) {
            // remove user with username from db
            PreparedStatement st = c.prepareStatement(
                    "delete from public.users where email = ?");
            st.setString(1, user.getUsername());
            log.info("[Ijacob2] removeUser: st={}", st);
            int rowsAffected = st.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Database error Removing User:" + ex.getMessage(), ex);
        }
    }

    // @Override
    // public boolean supportsCredentialType(String credentialType) {
    // log.info("[I57] supportsCredentialType({})", credentialType);
    // return PasswordCredentialModel.TYPE.endsWith(credentialType);
    // }

    // @Override
    // public boolean isConfiguredFor(RealmModel realm, UserModel user, String
    // credentialType) {
    // log.info("[I57] isConfiguredFor(realm={},user={},credentialType={})",
    // realm.getName(), user.getUsername(),
    // credentialType);
    // // In our case, password is the only type of credential, so we allways return
    // // 'true' if
    // // this is the credentialType
    // return supportsCredentialType(credentialType);
    // }

    // @Override
    // public boolean isValid(RealmModel realm, UserModel user, CredentialInput
    // credentialInput) {
    // log.info("[I57] isValid(realm={},user={},credentialInput.type={})",
    // realm.getName(), user.getUsername(),
    // credentialInput.getType());
    // if (!this.supportsCredentialType(credentialInput.getType())) {
    // return false;
    // }

    // try (Connection c = DbUtil.getConnection(this.model)) {
    // PreparedStatement st = c.prepareStatement("select password from kc_users
    // where username = ?");
    // st.setString(1, user.getUsername());
    // log.info("[I57] isValid: st={}", st);
    // st.execute();
    // ResultSet rs = st.getResultSet();
    // if (rs.next()) {
    // String pwd = rs.getString(1);
    // log.info("[I57] disableCredentialType: supplied={}, pwd={}, equal={}",
    // credentialInput.getChallengeResponse(), pwd,
    // pwd.equals(credentialInput.getChallengeResponse()));
    // return pwd.equals(credentialInput.getChallengeResponse());
    // } else {
    // return false;
    // }
    // } catch (SQLException ex) {
    // throw new RuntimeException("Database error:" + ex.getMessage(), ex);
    // }
    // }

    // @Override
    // public boolean updateCredential(RealmModel realm, UserModel user,
    // CredentialInput input) {
    // log.info("[I57] updateCredential(realm={},user={},credentialInput.type={})",
    // realm.getName(),
    // user.getUsername(), input.getType());
    // if (!this.supportsCredentialType(input.getType())) {
    // return false;
    // }

    // try (Connection c = DbUtil.getConnection(this.model)) {
    // PreparedStatement st = c.prepareStatement("update kc_users set password = ?
    // where username = ?");
    // st.setString(1, input.getChallengeResponse());
    // st.setString(2, user.getUsername());
    // log.info("[I57] updateCredential: st={}", st);
    // st.execute();
    // return true;
    // } catch (SQLException ex) {
    // throw new RuntimeException("Database error:" + ex.getMessage(), ex);
    // }
    // }

    // @Override
    // public void disableCredentialType(RealmModel realm, UserModel user, String
    // credentialType) {
    // log.info("[I57] disableCredentialType(realm={},user={},credentialType={})",
    // realm.getName(),
    // user.getUsername(), credentialType);
    // if (!credentialType.equals(PasswordCredentialModel.TYPE))
    // return;

    // try (Connection c = DbUtil.getConnection(this.model)) {
    // PreparedStatement st = c.prepareStatement("update kc_users set password = ''
    // where username = ?");
    // st.setString(1, user.getUsername());
    // log.info("[I57] disableCredentialType: st={}", st);
    // st.execute();
    // return;
    // } catch (SQLException ex) {
    // throw new RuntimeException("Database error:" + ex.getMessage(), ex);
    // }
    // }

    // @Override
    // public Stream<String> getDisableableCredentialTypesStream(RealmModel realm,
    // UserModel user) {
    // return Stream.of(PasswordCredentialModel.TYPE);
    // }

}
