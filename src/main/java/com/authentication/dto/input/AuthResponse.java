package com.authentication.dto.input;

public class AuthResponse {

    private String token;
    private UserOutput user;
    public AuthResponse(String token, UserOutput user) { this.token = token; this.user = user; }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserOutput getUser() {
        return user;
    }

    public void setUser(UserOutput user) {
        this.user = user;
    }
}
