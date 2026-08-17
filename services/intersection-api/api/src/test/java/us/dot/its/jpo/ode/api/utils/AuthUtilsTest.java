package us.dot.its.jpo.ode.api.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class AuthUtilsTest {

    @Nested
    @DisplayName("Tests for checkRoleAbove method")
    class CheckRoleAboveTests {
        @Test
        void testCheckRoleAbove_AdminAboveOperator() {
            assertTrue(AuthUtils.checkRoleAbove("ADMIN", "OPERATOR"));
        }

        @Test
        void testCheckRoleAbove_AdminAboveUser() {
            assertTrue(AuthUtils.checkRoleAbove("ADMIN", "USER"));
        }

        @Test
        void testCheckRoleAbove_OperatorAboveUser() {
            assertTrue(AuthUtils.checkRoleAbove("OPERATOR", "USER"));
        }

        @Test
        void testCheckRoleAbove_SameRole() {
            assertTrue(AuthUtils.checkRoleAbove("ADMIN", "ADMIN"));
            assertTrue(AuthUtils.checkRoleAbove("OPERATOR", "OPERATOR"));
            assertTrue(AuthUtils.checkRoleAbove("USER", "USER"));
        }

        @Test
        void testCheckRoleAbove_UserNotAboveOperator() {
            assertFalse(AuthUtils.checkRoleAbove("USER", "OPERATOR"));
        }

        @Test
        void testCheckRoleAbove_OperatorNotAboveAdmin() {
            assertFalse(AuthUtils.checkRoleAbove("OPERATOR", "ADMIN"));
        }

        @Test
        void testCheckRoleAbove_NullRole() {
            assertFalse(AuthUtils.checkRoleAbove(null, "ADMIN"));
        }

        @Test
        void testCheckRoleAbove_CaseInsensitive() {
            assertTrue(AuthUtils.checkRoleAbove("admin", "user"));
            assertTrue(AuthUtils.checkRoleAbove("Admin", "User"));
        }
    }
}
