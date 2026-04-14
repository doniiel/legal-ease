package kz.legeal.ease.backend.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cross-cutting security tests:
 * - Authentication enforcement on all authenticated routes
 * - IDOR: users cannot access each other's resources
 * - Invalid/expired token handling
 * - Role isolation across all three roles
 */
@DisplayName("Security — Authorization & IDOR")
class SecurityApiTest extends BaseIntegrationTest {

    private Long categoryId;
    private Long templateId;

    @BeforeEach
    void setUpData() throws Exception {
        // Create a category and published template for document tests
        final var catBody = Map.of("name", "Security Cat " + System.nanoTime(), "description", "");
        final var catResult = perform(authPost("/api/categories", adminToken, catBody)).andReturn();
        categoryId = objectMapper.readTree(catResult.getResponse().getContentAsString()).get("id").asLong();

        final var tplBody = Map.of(
                "title", "Security Template " + System.nanoTime(),
                "description", "desc",
                "categoryId", categoryId,
                "fields", List.of(
                        Map.of("fieldKey", "name", "label", "Name",
                                "fieldType", "TEXT", "required", true, "orderNum", 0)
                )
        );
        final var tplResult = perform(authPost("/api/my-templates", lawyerToken, tplBody)).andReturn();
        templateId = objectMapper.readTree(tplResult.getResponse().getContentAsString()).get("id").asLong();
        perform(authPost("/api/my-templates/" + templateId + "/publish", lawyerToken, Map.of()));
    }

    // ── No Token / Invalid Token ──────────────────────────────────────────────

    @Nested
    @DisplayName("Missing or invalid JWT")
    class InvalidToken {

        @Test
        @DisplayName("request without token to user endpoint returns 401")
        void userEndpoint_noToken_returns401() throws Exception {
            mvc.perform(get("/api/documents"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("request without token to lawyer endpoint returns 401")
        void lawyerEndpoint_noToken_returns401() throws Exception {
            mvc.perform(get("/api/my-templates"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("request without token to admin endpoint returns 401")
        void adminEndpoint_noToken_returns401() throws Exception {
            mvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("request with malformed JWT returns 401")
        void malformedJwt_returns401() throws Exception {
            mvc.perform(get("/api/documents")
                            .header("Authorization", "Bearer not.a.real.jwt"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("request with garbage token returns 401")
        void garbageToken_returns401() throws Exception {
            mvc.perform(get("/api/profile")
                            .header("Authorization", "Bearer AAABBBCCC"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("request with empty Bearer returns 401")
        void emptyBearer_returns401() throws Exception {
            mvc.perform(get("/api/profile")
                            .header("Authorization", "Bearer "))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── IDOR — User isolation ─────────────────────────────────────────────────

    @Nested
    @DisplayName("IDOR — Users cannot access other users' documents")
    class Idor {

        @Test
        @DisplayName("user2 cannot GET user1's document")
        void user2CannotGetUser1Document() throws Exception {
            // user creates a document
            final var body = Map.of(
                    "templateId", templateId,
                    "title", "Private Doc " + System.nanoTime(),
                    "fieldValues", Map.of("name", "Secret")
            );
            final var result = perform(authPost("/api/documents", userToken, body))
                    .andReturn();
            final Long docId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            // user2 tries to access it → should be 403 or 404
            perform(authGet("/api/documents/" + docId, user2Token))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("user2 cannot DELETE user1's document")
        void user2CannotDeleteUser1Document() throws Exception {
            final var body = Map.of(
                    "templateId", templateId,
                    "title", "Delete IDOR " + System.nanoTime(),
                    "fieldValues", Map.of("name", "Secret")
            );
            final var result = perform(authPost("/api/documents", userToken, body)).andReturn();
            final Long docId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            perform(authDelete("/api/documents/" + docId, user2Token))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("user2 cannot UPDATE user1's document")
        void user2CannotUpdateUser1Document() throws Exception {
            final var body = Map.of(
                    "templateId", templateId,
                    "title", "Update IDOR " + System.nanoTime(),
                    "fieldValues", Map.of("name", "Original")
            );
            final var result = perform(authPost("/api/documents", userToken, body)).andReturn();
            final Long docId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            final var updateBody = Map.of("title", "Hijacked", "fieldValues", Map.of("name", "Hijacked"));
            perform(authPut("/api/documents/" + docId, user2Token, updateBody))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("user2 cannot VALIDATE user1's document")
        void user2CannotValidateUser1Document() throws Exception {
            final var body = Map.of(
                    "templateId", templateId,
                    "title", "Validate IDOR " + System.nanoTime(),
                    "fieldValues", Map.of("name", "Data")
            );
            final var result = perform(authPost("/api/documents", userToken, body)).andReturn();
            final Long docId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            perform(authPost("/api/documents/" + docId + "/validate", user2Token, Map.of()))
                    .andExpect(status().is4xxClientError());
        }
    }

    // ── Role Isolation ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Role isolation — all three roles")
    class RoleIsolation {

        // Admin cannot access user-only endpoints
        @Test
        @DisplayName("ADMIN cannot access user-only endpoints")
        void admin_cannotAccess_userEndpoints() throws Exception {
            perform(authGet("/api/documents", adminToken))
                    .andExpect(status().isForbidden());

            // Profile is accessible to all authenticated users
            perform(authGet("/api/profile", adminToken))
                    .andExpect(status().isOk());
        }

        // Admin cannot access lawyer endpoints
        @Test
        @DisplayName("ADMIN cannot access /api/lawyer/** endpoints")
        void admin_cannotAccess_lawyerEndpoints() throws Exception {
            perform(authGet("/api/my-templates", adminToken))
                    .andExpect(status().isForbidden());
        }

        // User cannot access admin endpoints
        @Test
        @DisplayName("USER cannot access /api/admin/** endpoints")
        void user_cannotAccess_adminEndpoints() throws Exception {
            perform(authGet("/api/users", userToken))
                    .andExpect(status().isForbidden());

            perform(authGet("/api/audit-logs", userToken))
                    .andExpect(status().isForbidden());

            perform(authGet("/api/metrics", userToken))
                    .andExpect(status().isForbidden());
        }

        // User cannot access lawyer endpoints
        @Test
        @DisplayName("USER cannot access /api/lawyer/** endpoints")
        void user_cannotAccess_lawyerEndpoints() throws Exception {
            perform(authGet("/api/my-templates", userToken))
                    .andExpect(status().isForbidden());

            perform(authGet("/api/lawyer-documents", userToken))
                    .andExpect(status().isForbidden());

            perform(authGet("/api/reviews", userToken))
                    .andExpect(status().isForbidden());
        }

        // Lawyer cannot access admin endpoints
        @Test
        @DisplayName("LAWYER cannot access /api/admin/** endpoints")
        void lawyer_cannotAccess_adminEndpoints() throws Exception {
            perform(authGet("/api/users", lawyerToken))
                    .andExpect(status().isForbidden());

            perform(authGet("/api/categories", lawyerToken))
                    .andExpect(status().isForbidden());
        }

        // Lawyer cannot access user-only endpoints
        @Test
        @DisplayName("LAWYER cannot access user-only endpoints")
        void lawyer_cannotAccess_userEndpoints() throws Exception {
            perform(authGet("/api/documents", lawyerToken))
                    .andExpect(status().isForbidden());

            // Profile is accessible to all authenticated users
            perform(authGet("/api/profile", lawyerToken))
                    .andExpect(status().isOk());
        }
    }

    // ── AI endpoint (isAuthenticated) ─────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/ai/explain-clause")
    class AiClauseExplain {

        @Test
        @DisplayName("returns 200 for USER role (any authenticated role works)")
        void explainClause_userRole_returns200() throws Exception {
            final var body = Map.of("text", "The seller agrees to transfer ownership upon full payment.");

            perform(authPost("/api/ai/explain-clause", userToken, body))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 200 for LAWYER role")
        void explainClause_lawyerRole_returns200() throws Exception {
            final var body = Map.of("text", "The seller agrees to transfer ownership upon full payment.");

            perform(authPost("/api/ai/explain-clause", lawyerToken, body))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 200 for ADMIN role")
        void explainClause_adminRole_returns200() throws Exception {
            final var body = Map.of("text", "The seller agrees to transfer ownership upon full payment.");

            perform(authPost("/api/ai/explain-clause", adminToken, body))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 401 for unauthenticated request")
        void explainClause_noAuth_returns401() throws Exception {
            mvc.perform(post("/api/ai/explain-clause")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content("{\"text\":\"some clause\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("returns 400 when text is blank")
        void explainClause_blankText_returns400() throws Exception {
            final var body = Map.of("text", "");

            perform(authPost("/api/ai/explain-clause", userToken, body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when text exceeds 5000 chars")
        void explainClause_tooLongText_returns400() throws Exception {
            final var body = Map.of("text", "a".repeat(5001));

            perform(authPost("/api/ai/explain-clause", userToken, body))
                    .andExpect(status().isBadRequest());
        }
    }
}
