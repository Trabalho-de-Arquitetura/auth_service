package com.authentication.dto;
import java.util.UUID;
import com.authentication.dto.input.UserRoleInput;

public class UserFromUserService {
    private UUID id;
    private String email;
    private String password; // Hashed password
    private String name;
    private String affiliatedSchool;
    private UserRoleInput role;  // Usando enum UserRoleInput

    // Getters e Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAffiliatedSchool() {
        return affiliatedSchool;
    }

    public void setAffiliatedSchool(String affiliatedSchool) {
        this.affiliatedSchool = affiliatedSchool;
    }

    public UserRoleInput getRole() {
        return role;
    }

    public void setRole(UserRoleInput role) {
        this.role = role;
    }

}
