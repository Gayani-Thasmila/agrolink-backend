package com.agrolink.backend.dto;

import com.agrolink.backend.model.User;

public class AuthResponse {

    private final boolean success;
    private final String message;
    private String token;

    private final User user;

    public AuthResponse(boolean success, String message, User user, Object o) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public String getToken() { return token; }
}
