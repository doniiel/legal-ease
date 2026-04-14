package kz.legeal.ease.backend.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for /api/admin/** endpoints.
 *
 * Covers: authorization enforcement, happy paths for all admin controllers,
 * validation failures, and pagination.
 */
@DisplayName("Admin API")
class AdminApiTest extends BaseIntegrationTest {

    // ── Authorization ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Authorization")
    class Authorization {

        @Test
        @DisplayName("GET /api/metrics returns 403 for USER role")
        void metrics_userRole_returns403() throws Exception {
            perform(authGet("/api/metrics", userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/metrics returns 403 for LAWYER role")
        void metrics_lawyerRole_returns403() throws Exception {
            perform(authGet("/api/metrics", lawyerToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/metrics returns 401 for unauthenticated request")
        void metrics_noAuth_returns401() throws Exception {
            mvc.perform(get("/api/metrics"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/users returns 403 for USER role")
        void users_userRole_returns403() throws Exception {
            perform(authGet("/api/users", userToken))
                    .andExpect(status().isForbidden());
        }
    }

    // ── Metrics ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/metrics")
    class Metrics {

        @Test
        @DisplayName("returns 200 with platform metrics for ADMIN")
        void getMetrics_admin_returns200() throws Exception {
            perform(authGet("/api/metrics", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isMap());
        }
    }

    // ── Category Management ───────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/categories")
    class Categories {

        @Test
        @DisplayName("POST creates a new category")
        void createCategory_validRequest_returns201Or200() throws Exception {
            final var body = Map.of(
                    "name", "Test Category " + System.nanoTime(),
                    "description", "Category for testing"
            );

            perform(authPost("/api/categories", adminToken, body))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(jsonPath("$.name").value((String) body.get("name")));
        }

        @Test
        @DisplayName("POST returns 400 when name is too short")
        void createCategory_shortName_returns400() throws Exception {
            final var body = Map.of("name", "X"); // < 2 chars

            perform(authPost("/api/categories", adminToken, body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST returns 400 when name is missing")
        void createCategory_missingName_returns400() throws Exception {
            final var body = Map.of("description", "No name provided");

            perform(authPost("/api/categories", adminToken, body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET returns paginated list of categories")
        void getCategories_admin_returnsPaginatedList() throws Exception {
            perform(authGet("/api/categories", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber());
        }

        @Test
        @DisplayName("GET /{id} returns 404 for non-existent category")
        void getCategory_nonExistent_returns404() throws Exception {
            perform(authGet("/api/categories/999999", adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT updates category")
        void updateCategory_validRequest_returns200() throws Exception {
            // Create a category first
            final var createBody = Map.of("name", "UpdateMe " + System.nanoTime(), "description", "");
            final var createResult = perform(authPost("/api/categories", adminToken, createBody))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();

            final var categoryId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asLong();

            final var updateBody = Map.of(
                    "name", "Updated Category",
                    "description", "Updated description",
                    "active", true
            );

            perform(authPut("/api/categories/" + categoryId, adminToken, updateBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Category"));
        }

        @Test
        @DisplayName("DELETE soft-deletes category")
        void deleteCategory_exists_returns204Or200() throws Exception {
            final var createBody = Map.of("name", "DeleteMe " + System.nanoTime(), "description", "");
            final var createResult = perform(authPost("/api/categories", adminToken, createBody))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();

            final var categoryId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asLong();

            perform(authDelete("/api/categories/" + categoryId, adminToken))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("POST returns 403 for USER role")
        void createCategory_userRole_returns403() throws Exception {
            final var body = Map.of("name", "Unauthorized", "description", "");

            perform(authPost("/api/categories", userToken, body))
                    .andExpect(status().isForbidden());
        }
    }

    // ── User Management ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/users")
    class UserManagement {

        @Test
        @DisplayName("GET returns paginated user list")
        void getUsers_admin_returnsPaginatedList() throws Exception {
            perform(authGet("/api/users", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber());
        }

        @Test
        @DisplayName("GET /{id} returns user details")
        void getUserById_exists_returns200() throws Exception {
            perform(authGet("/api/users/" + testUser.getId(), adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testUser.getId()));
        }

        @Test
        @DisplayName("GET /{id} returns 404 for non-existent user")
        void getUserById_notFound_returns404() throws Exception {
            perform(authGet("/api/users/999999999", adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /{id}/block blocks user")
        void blockUser_validId_returns200() throws Exception {
            perform(authPost("/api/users/" + testUser2.getId() + "/block", adminToken, Map.of()))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("POST /{id}/unblock unblocks user")
        void unblockUser_validId_returns200() throws Exception {
            // Block first
            perform(authPost("/api/users/" + testUser2.getId() + "/block", adminToken, Map.of()));

            perform(authPost("/api/users/" + testUser2.getId() + "/unblock", adminToken, Map.of()))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("GET returns paginated list with pagination params")
        void getUsers_withPageAndSize_returns200() throws Exception {
            perform(authGet("/api/users?page=0&size=5", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.size").value(5));
        }
    }

    // ── Lawyer Applications ───────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/lawyer-applications")
    class LawyerApplications {

        @Test
        @DisplayName("GET returns paginated list")
        void listApplications_admin_returnsPaginatedList() throws Exception {
            perform(authGet("/api/lawyer-applications", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("GET /{id} returns 404 for non-existent application")
        void getApplication_notFound_returns404() throws Exception {
            perform(authGet("/api/lawyer-applications/999999", adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /{id}/approve returns 404 for non-existent application")
        void approveApplication_notFound_returns404() throws Exception {
            perform(authPost("/api/lawyer-applications/999999/approve", adminToken, Map.of()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /{id}/reject returns 400 when reason is missing")
        void rejectApplication_missingReason_returns400() throws Exception {
            perform(authPost("/api/lawyer-applications/1/reject", adminToken, Map.of()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Audit Logs ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/audit-logs")
    class AuditLogs {

        @Test
        @DisplayName("GET returns paginated audit log list")
        void getAuditLogs_admin_returnsPaginatedList() throws Exception {
            perform(authGet("/api/audit-logs", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("GET /{id} returns 404 for non-existent audit log")
        void getAuditLog_notFound_returns404() throws Exception {
            perform(authGet("/api/audit-logs/999999", adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET returns 403 for USER role")
        void getAuditLogs_userRole_returns403() throws Exception {
            perform(authGet("/api/audit-logs", userToken))
                    .andExpect(status().isForbidden());
        }
    }

    // ── Rule Management ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/rules")
    class Rules {

        @Test
        @DisplayName("GET /validation returns list of validation rules")
        void getValidationRules_admin_returns200() throws Exception {
            perform(authGet("/api/rules/validation", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /risk returns list of risk rules")
        void getRiskRules_admin_returns200() throws Exception {
            perform(authGet("/api/rules/risk", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /matching returns list of matching rules")
        void getMatchingRules_admin_returns200() throws Exception {
            perform(authGet("/api/rules/matching", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /conditional returns list of conditional rules")
        void getConditionalRules_admin_returns200() throws Exception {
            perform(authGet("/api/rules/conditional", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /required-docs returns list of required-docs rules")
        void getRequiredDocsRules_admin_returns200() throws Exception {
            perform(authGet("/api/rules/required-docs", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("PATCH /validation/{id}/toggle returns 404 for non-existent rule")
        void toggleValidationRule_notFound_returns404() throws Exception {
            perform(authPatch("/api/rules/validation/999999/toggle", adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /validation returns 403 for USER role")
        void getValidationRules_userRole_returns403() throws Exception {
            perform(authGet("/api/rules/validation", userToken))
                    .andExpect(status().isForbidden());
        }
    }
}
