package com.agrolink.backend.dto;

import com.agrolink.backend.model.User;

public class AdminAuthResponse {

    private final boolean success;
    private final String message;
    private final String token;
    private final User user;

    public AdminAuthResponse(boolean success, String message, String token, User user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }
}
