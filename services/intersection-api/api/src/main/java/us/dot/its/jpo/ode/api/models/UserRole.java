package us.dot.its.jpo.ode.api.models;

public enum UserRole {
    // Order matters, ordinal starts at 0 and increments for each role
    USER,
    OPERATOR,
    ADMIN;

    public boolean hasMinimumRole(UserRole requiredRole) {
        if (requiredRole == null) {
            return false;
        }
        return this.ordinal() >= requiredRole.ordinal();
    }

    public static UserRole fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Role value cannot be null or empty");
        }
        return UserRole.valueOf(value.trim().toUpperCase());
    }
}