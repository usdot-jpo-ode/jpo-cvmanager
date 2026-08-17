package us.dot.its.jpo.ode.api.utils;

import java.util.Map;

public class AuthUtils {

    private static final Map<String, Integer> ROLE_HIERARCHY = Map.of(
        "ADMIN", 3,
        "OPERATOR", 2,
        "USER", 1
    );

    public static boolean checkRoleAbove(String userRole, String requiredRole) {
        if (userRole == null || requiredRole == null) {
            return false;
        }
        Integer userRoleValue = ROLE_HIERARCHY.get(userRole.toUpperCase());
        Integer requiredRoleValue = ROLE_HIERARCHY.get(requiredRole.toUpperCase());
        if (userRoleValue == null || requiredRoleValue == null) {
            throw new IllegalArgumentException("Unknown role: " + userRole + ", " + requiredRole);
        }
        return userRoleValue >= requiredRoleValue;
    }
}
