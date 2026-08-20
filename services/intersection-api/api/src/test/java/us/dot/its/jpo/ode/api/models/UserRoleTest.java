package us.dot.its.jpo.ode.api.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class UserRoleTest {

    @Nested
    class FromStringTests {
        @Test
        void testFromString_User() {
            UserRole role = UserRole.fromString("user");
            assertEquals(UserRole.USER, role);

            role = UserRole.fromString("User");
            assertEquals(UserRole.USER, role);

            role = UserRole.fromString("USER");
            assertEquals(UserRole.USER, role);
        }

        @Test
        void testFromString_Operator() {
            UserRole role = UserRole.fromString("operator");
            assertEquals(UserRole.OPERATOR, role);

            role = UserRole.fromString("Operator");
            assertEquals(UserRole.OPERATOR, role);

            role = UserRole.fromString("OPERATOR");
            assertEquals(UserRole.OPERATOR, role);
        }

        @Test
        void testFromString_Admin() {
            UserRole role = UserRole.fromString("admin");
            assertEquals(UserRole.ADMIN, role);

            role = UserRole.fromString("Admin");
            assertEquals(UserRole.ADMIN, role);

            role = UserRole.fromString("ADMIN");
            assertEquals(UserRole.ADMIN, role);
        }

        @Test
        void testFromString_Invalid() {
            assertThrows(IllegalArgumentException.class, () -> {
                UserRole.fromString("a");
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                UserRole.fromString("");
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                UserRole.fromString(null);
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                UserRole.fromString("useradmin");
            });
        }
    }

    @Nested
    @DisplayName("Tests for checkRoleAbove method")
    class CheckRoleAboveTests {
        @Test
        void testCheckRoleAbove_AdminAboveOperator() {
            assertTrue(UserRole.ADMIN.hasMinimumRole(UserRole.OPERATOR));
        }

        @Test
        void testCheckRoleAbove_AdminAboveUser() {
            assertTrue(UserRole.ADMIN.hasMinimumRole(UserRole.USER));
        }

        @Test
        void testCheckRoleAbove_OperatorAboveUser() {
            assertTrue(UserRole.OPERATOR.hasMinimumRole(UserRole.USER));
        }

        @Test
        void testCheckRoleAbove_SameRole() {
            assertTrue(UserRole.ADMIN.hasMinimumRole(UserRole.ADMIN));
            assertTrue(UserRole.OPERATOR.hasMinimumRole(UserRole.OPERATOR));
            assertTrue(UserRole.USER.hasMinimumRole(UserRole.USER));
        }

        @Test
        void testCheckRoleAbove_UserNotAboveOperator() {
            assertFalse(UserRole.USER.hasMinimumRole(UserRole.OPERATOR));
        }

        @Test
        void testCheckRoleAbove_OperatorNotAboveAdmin() {
            assertFalse(UserRole.OPERATOR.hasMinimumRole(UserRole.ADMIN));
        }

        @Test
        void testCheckRoleAbove_NullRole() {
            assertFalse(UserRole.ADMIN.hasMinimumRole(null));
        }
    }
}
