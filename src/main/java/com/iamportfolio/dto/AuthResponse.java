package com.iamportfolio.dto;

import java.time.LocalDateTime;

public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private String username;
    private String email;
    private boolean twoFactorEnabled;
    private boolean requiresTwoFactor;
    private String message;
    private LocalDateTime timestamp;

    // Construtores
    public AuthResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public AuthResponse(String accessToken, Long expiresIn, String username, String email, boolean twoFactorEnabled) {
        this();
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.username = username;
        this.email = email;
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public AuthResponse(String message, boolean requiresTwoFactor) {
        this();
        this.message = message;
        this.requiresTwoFactor = requiresTwoFactor;
    }

    // Factory methods
    public static AuthResponse success(String accessToken, Long expiresIn, String username, String email, boolean twoFactorEnabled) {
        return new AuthResponse(accessToken, expiresIn, username, email, twoFactorEnabled);
    }

    public static AuthResponse requiresTwoFactor(String message) {
        return new AuthResponse(message, true);
    }

    public static AuthResponse error(String message) {
        AuthResponse response = new AuthResponse();
        response.setMessage(message);
        return response;
    }

    // Getters e Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public boolean isRequiresTwoFactor() {
        return requiresTwoFactor;
    }

    public void setRequiresTwoFactor(boolean requiresTwoFactor) {
        this.requiresTwoFactor = requiresTwoFactor;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", twoFactorEnabled=" + twoFactorEnabled +
                ", requiresTwoFactor=" + requiresTwoFactor +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 