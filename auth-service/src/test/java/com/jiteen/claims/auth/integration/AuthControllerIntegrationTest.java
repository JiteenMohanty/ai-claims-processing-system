package com.jiteen.claims.auth.integration;

import com.jiteen.claims.auth.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void registerUserSuccessfully() throws Exception {

        final String requestBody = """
                {
                  "email": "integration6@test.com",
                  "password": "Password123!",
                  "firstName": "Integration",
                  "lastName": "Test"
                }
                """;

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void registerDuplicateUserFails() throws Exception {

        final String requestBody = """
            {
              "email": "duplicate3@test.com",
              "password": "Password123!",
              "firstName": "John",
              "lastName": "Doe"
            }
            """;

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    void loginSuccessfully() throws Exception {

        final String registerRequest = """
            {
              "email": "login-test2@test.com",
              "password": "Password123!",
              "firstName": "Login",
              "lastName": "User"
            }
            """;

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(registerRequest))
                .andExpect(status().isCreated());

        final String loginRequest = """
            {
              "email": "login-test2@test.com",
              "password": "Password123!"
            }
            """;

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginWithInvalidPasswordFails() throws Exception {

        final String registerRequest = """
            {
              "email": "invalid-password1@test.com",
              "password": "Password123!",
              "firstName": "Invalid",
              "lastName": "Password"
            }
            """;

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(registerRequest))
                .andExpect(status().isCreated());

        final String loginRequest = """
            {
              "email": "invalid-password1@test.com",
              "password": "WrongPassword"
            }
            """;

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenSuccessfully() throws Exception {

        final String registerRequest = """
            {
              "email": "refresh-success1@test.com",
              "password": "Password123!",
              "firstName": "Refresh",
              "lastName": "User"
            }
            """;

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(registerRequest))
                .andExpect(status().isCreated());

        final String loginRequest = """
            {
              "email": "refresh-success1@test.com",
              "password": "Password123!"
            }
            """;

        final String loginResponse
                = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType("application/json")
                                .content(loginRequest))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final JsonNode loginJson = objectMapper.readTree(loginResponse);

        final String refreshToken
                = loginJson.get("refreshToken").asText();

        final String refreshRequest = """
            {
              "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        mockMvc.perform(
                post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content(refreshRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void logoutSuccessfully() throws Exception {

        final String registerRequest = """
            {
              "email": "logout-success1@test.com",
              "password": "Password123!",
              "firstName": "Logout",
              "lastName": "User"
            }
            """;

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(registerRequest))
                .andExpect(status().isCreated());

        final String loginRequest = """
            {
              "email": "logout-success1@test.com",
              "password": "Password123!"
            }
            """;

        final String loginResponse
                = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType("application/json")
                                .content(loginRequest))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final JsonNode loginJson = objectMapper.readTree(loginResponse);

        final String refreshToken
                = loginJson.get("refreshToken").asText();

        final String logoutRequest = """
            {
              "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        mockMvc.perform(
                post("/api/v1/auth/logout")
                        .contentType("application/json")
                        .content(logoutRequest))
                .andExpect(status().isNoContent());
    }

    @Test
    void refreshTokenFailsAfterLogout() throws Exception {

        final String registerRequest = """
            {
              "email": "logout-refresh1@test.com",
              "password": "Password123!",
              "firstName": "Logout",
              "lastName": "Refresh"
            }
            """;

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(registerRequest))
                .andExpect(status().isCreated());

        final String loginRequest = """
            {
              "email": "logout-refresh1@test.com",
              "password": "Password123!"
            }
            """;

        final String loginResponse
                = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType("application/json")
                                .content(loginRequest))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final JsonNode loginJson = objectMapper.readTree(loginResponse);

        final String refreshToken
                = loginJson.get("refreshToken").asText();

        final String logoutRequest = """
            {
              "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        mockMvc.perform(
                post("/api/v1/auth/logout")
                        .contentType("application/json")
                        .content(logoutRequest))
                .andExpect(status().isNoContent());

        final String refreshRequest = """
            {
              "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        mockMvc.perform(
                post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content(refreshRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenRotationWorks() throws Exception {

        final String registerRequest = """
            {
              "email": "rotation1@test.com",
              "password": "Password123!",
              "firstName": "Rotation",
              "lastName": "User"
            }
            """;

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(registerRequest))
                .andExpect(status().isCreated());

        final String loginRequest = """
            {
              "email": "rotation1@test.com",
              "password": "Password123!"
            }
            """;

        final String loginResponse
                = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType("application/json")
                                .content(loginRequest))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final JsonNode loginJson = objectMapper.readTree(loginResponse);

        final String originalRefreshToken
                = loginJson.get("refreshToken").asText();

        final String refreshRequest = """
            {
              "refreshToken": "%s"
            }
            """.formatted(originalRefreshToken);

        final String refreshResponse
                = mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType("application/json")
                                .content(refreshRequest))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        final JsonNode refreshJson
                = objectMapper.readTree(refreshResponse);

        final String rotatedRefreshToken
                = refreshJson.get("refreshToken").asText();

        assertNotEquals(
                originalRefreshToken,
                rotatedRefreshToken);
    }

}
