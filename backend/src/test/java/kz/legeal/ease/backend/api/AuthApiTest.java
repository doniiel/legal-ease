package kz.legeal.ease.backend.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for POST /open-api/auth/* endpoints.
 *
 * All endpoints are public (no JWT required).
 */
@DisplayName("Auth API")
class AuthApiTest extends BaseIntegrationTest {

    // ── Login ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /open-api/auth/login")
    class Login {

        @Test
        @DisplayName("returns 200 with tokens on valid credentials")
        void login_validCredentials_returns200() throws Exception {
            final var body = Map.of("email", testUser.getEmail(), "password", "Password1!");

            mvc.perform(post("/open-api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("returns 401 on wrong password")
        void login_wrongPassword_returns401() throws Exception {
            final var body = Map.of("email", testUser.getEmail(), "password", "WrongPass99!");

            mvc.perform(post("/open-api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("returns 401 on unknown email")
        void login_unknownEmail_returns401() throws Exception {
            final var body = Map.of("email", "nobody@test.com", "password", "Password1!");

            mvc.perform(post("/open-api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("returns 400 when email is blank")
        void login_missingEmail_returns400() throws Exception {
            final var body = Map.of("password", "Password1!");

            mvc.perform(post("/open-api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when password is too short")
        void login_shortPassword_returns400() throws Exception {
            final var body = Map.of("email", testUser.getEmail(), "password", "hi");

            mvc.perform(post("/open-api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /open-api/auth/register")
    class Register {

        @Test
        @DisplayName("returns 200 on successful registration")
        void register_validRequest_returns200() throws Exception {
            final var body = Map.of(
                    "firstName", "Aliya",
                    "lastName", "Bekova",
                    "iin", "960101123456",
                    "email", "newuser_" + System.nanoTime() + "@test.com",
                    "phone", "+77001234567",
                    "password", "Password1!"
            );

            mvc.perform(post("/open-api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 409/400 when email already exists")
        void register_duplicateEmail_returnsError() throws Exception {
            final var body = Map.of(
                    "firstName", "Test",
                    "lastName", "User",
                    "iin", "880101999888",
                    "email", testUser.getEmail(),  // already exists
                    "phone", "+77009998877",
                    "password", "Password1!"
            );

            mvc.perform(post("/open-api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("returns 400 when password is too weak")
        void register_weakPassword_returns400() throws Exception {
            final var body = Map.of(
                    "firstName", "Test",
                    "lastName", "User",
                    "iin", "770101777666",
                    "email", "weakpwd@test.com",
                    "phone", "+77007776655",
                    "password", "simple"  // no uppercase, too short
            );

            mvc.perform(post("/open-api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when IIN is not 12 digits")
        void register_invalidIin_returns400() throws Exception {
            final var body = Map.of(
                    "firstName", "Test",
                    "lastName", "User",
                    "iin", "123",  // too short
                    "email", "badiin@test.com",
                    "phone", "+77001112233",
                    "password", "Password1!"
            );

            mvc.perform(post("/open-api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when required fields are missing")
        void register_missingFirstName_returns400() throws Exception {
            final var body = Map.of(
                    "lastName", "User",
                    "iin", "960101555444",
                    "email", "noname@test.com",
                    "phone", "+77005554433",
                    "password", "Password1!"
            );

            mvc.perform(post("/open-api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Confirm account ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /open-api/auth/confirm")
    class Confirm {

        @Test
        @DisplayName("returns 400 on wrong verification code")
        void confirm_wrongCode_returnsError() throws Exception {
            final var body = Map.of(
                    "email", testUser.getEmail(),
                    "code", "000000"
            );

            mvc.perform(post("/open-api/auth/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("returns 400 when email is missing")
        void confirm_missingEmail_returns400() throws Exception {
            final var body = Map.of("code", "123456");

            mvc.perform(post("/open-api/auth/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Reset password ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /open-api/auth/reset-password")
    class ResetPassword {

        @Test
        @DisplayName("returns 200 for existing email")
        void resetPassword_existingEmail_returns200() throws Exception {
            final var body = Map.of("email", testUser.getEmail());

            mvc.perform(post("/open-api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 400 when email is missing")
        void resetPassword_missingEmail_returns400() throws Exception {
            mvc.perform(post("/open-api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Change password ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /open-api/auth/change-password")
    class ChangePassword {

        @Test
        @DisplayName("returns 4xx with invalid code")
        void changePassword_invalidCode_returnsError() throws Exception {
            final var body = Map.of(
                    "email", testUser.getEmail(),
                    "code", "badcode",
                    "newPassword", "NewPass1!"
            );

            mvc.perform(post("/open-api/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("returns 400 when new password is weak")
        void changePassword_weakNewPassword_returns400() throws Exception {
            final var body = Map.of(
                    "email", testUser.getEmail(),
                    "code", "123456",
                    "newPassword", "weak"
            );

            mvc.perform(post("/open-api/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Refresh token ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /open-api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("returns 4xx on invalid refresh token")
        void refresh_invalidToken_returnsError() throws Exception {
            final var body = Map.of("refreshToken", "not.a.valid.jwt.token");

            mvc.perform(post("/open-api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("returns 400 when refresh token is missing")
        void refresh_missingToken_returns400() throws Exception {
            mvc.perform(post("/open-api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /open-api/auth/logout")
    class Logout {

        @Test
        @DisplayName("returns 200 on valid refresh token")
        void logout_validToken_returns200() throws Exception {
            // Log in first to get a real refresh token
            final var loginBody = Map.of("email", testUser.getEmail(), "password", "Password1!");
            final var loginResult = mvc.perform(post("/open-api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginBody)))
                    .andExpect(status().isOk())
                    .andReturn();

            final var responseJson = objectMapper.readTree(
                    loginResult.getResponse().getContentAsString());
            final String refreshToken = responseJson.get("refreshToken").asText();

            mvc.perform(post("/open-api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", refreshToken))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 400 when refresh token is missing")
        void logout_missingToken_returns400() throws Exception {
            mvc.perform(post("/open-api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
