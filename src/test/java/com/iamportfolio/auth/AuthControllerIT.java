package com.iamportfolio.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iamportfolio.AbstractPostgresIT;
import com.iamportfolio.auth.dto.LoginRequest;
import com.iamportfolio.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke integration test for the auth flow against a real Postgres.
 * Exercises register + login round-trip; lifecycle blocking and 2FA
 * have their own slimmer tests.
 */
@AutoConfigureMockMvc
class AuthControllerIT extends AbstractPostgresIT {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test
    void registerThenLoginReturnsJwt() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice_" + suffix);
        req.setEmail("alice_" + suffix + "@example.com");
        req.setPassword("S3cr3tPass!");
        req.setConfirmPassword("S3cr3tPass!");
        req.setFirstName("Alice");
        req.setLastName("Tester");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        LoginRequest login = new LoginRequest();
        login.setUsernameOrEmail("alice_" + suffix);
        login.setPassword("S3cr3tPass!");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.username").value("alice_" + suffix));
    }
}
