package com.iamportfolio.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Nome de usuÃ¡rio Ã© obrigatÃ³rio")
    @Size(min = 3, max = 50, message = "Nome de usuÃ¡rio deve ter entre 3 e 50 caracteres")
    private String username;

    @NotBlank(message = "Email Ã© obrigatÃ³rio")
    @Email(message = "Email deve ter formato vÃ¡lido")
    private String email;

    @NotBlank(message = "Senha Ã© obrigatÃ³ria")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String password;

    @NotBlank(message = "ConfirmaÃ§Ã£o de senha Ã© obrigatÃ³ria")
    private String confirmPassword;

    private String firstName;
    
    private String lastName;

    // Construtores
    public RegisterRequest() {}

    public RegisterRequest(String username, String email, String password, String confirmPassword) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    // Getters e Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTEGIDA]'" +
                ", confirmPassword='[PROTEGIDA]'" +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
} 