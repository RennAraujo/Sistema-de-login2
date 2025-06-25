package com.login.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "Nome de usuário ou email é obrigatório")
    private String usernameOrEmail;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String password;

    private String twoFactorCode;

    // Construtores
    public LoginRequest() {}

    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    public LoginRequest(String usernameOrEmail, String password, String twoFactorCode) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
        this.twoFactorCode = twoFactorCode;
    }

    // Getters e Setters
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTwoFactorCode() {
        return twoFactorCode;
    }

    public void setTwoFactorCode(String twoFactorCode) {
        this.twoFactorCode = twoFactorCode;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "usernameOrEmail='" + usernameOrEmail + '\'' +
                ", password='[PROTEGIDA]'" +
                ", twoFactorCode='" + (twoFactorCode != null ? "[INFORMADO]" : "[NÃO INFORMADO]") + '\'' +
                '}';
    }
} 