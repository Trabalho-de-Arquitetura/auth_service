package com.authentication.entity;

public enum UserRole {
    ADMIN("admin"),
    PROFESSOR("professor"),
    STUDENT("student");

    private final String role;
    UserRole(String role) { this.role = role; }
    public String getRole() { return role; }
}
