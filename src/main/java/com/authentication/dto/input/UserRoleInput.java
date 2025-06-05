package com.authentication.dto.input;

public enum UserRoleInput {
    ADMIN("admin"),
    PROFESSOR("professor"),
    STUDENT("student");

    private final String role;

    UserRoleInput(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public static UserRoleInput fromString(String roleStr) {
        for (UserRoleInput role : UserRoleInput.values()) {
            if (role.getRole().equalsIgnoreCase(roleStr)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + roleStr);
    }
}
