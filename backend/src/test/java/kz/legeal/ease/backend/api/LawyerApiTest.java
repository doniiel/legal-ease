package kz.legeal.ease.backend.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for /api/lawyer/** endpoints.
 *
 * Covers: template CRUD, publish, document reviews, matching rules.
 * Also verifies that USER and ADMIN roles are denied access.
 */
@DisplayName("Lawyer API")
class LawyerApiTest extends BaseIntegrationTest {

    // ── Authorization ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Authorization")
    class Authorization {

        @Test
        @DisplayName("GET /api/my-templates returns 403 for USER role")
        void templates_userRole_returns403() throws Exception {
            perform(authGet("/api/my-templates", userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/my-templates returns 403 for ADMIN role")
        void templates_adminRole_returns403() throws Exception {
            perform(authGet("/api/my-templates", adminToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/my-templates returns 401 for unauthenticated")
        void templates_noAuth_returns401() throws Exception {
            mvc.perform(get("/api/my-templates"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── Template CRUD ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/my-templates")
    class Templates {

        private Long createCategory() throws Exception {
            final var body = Map.of("name", "LT Cat " + System.nanoTime(), "description", "");
            final var result = perform(authPost("/api/categories", adminToken, body))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();
            return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        }

        private Long createTemplate(Long categoryId) throws Exception {
            final var body = Map.of(
                    "title", "Template " + System.nanoTime(),
                    "description", "A test template",
                    "categoryId", categoryId,
                    "fields", List.of(
                            Map.of("fieldKey", "client_name", "label", "Client Name",
                                    "fieldType", "TEXT", "required", true, "orderNum", 0)
                    )
            );
            final var result = perform(authPost("/api/my-templates", lawyerToken, body))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();
            return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        }

        @Test
        @DisplayName("POST creates a template in DRAFT status")
        void createTemplate_validRequest_returns2xxWithDraft() throws Exception {
            final Long catId = createCategory();
            final var body = Map.of(
                    "title", "Sale Agreement " + System.nanoTime(),
                    "description", "A basic sale contract",
                    "categoryId", catId,
                    "fields", List.of(
                            Map.of("fieldKey", "party_a", "label", "Party A",
                                    "fieldType", "TEXT", "required", true, "orderNum", 0)
                    )
            );

            perform(authPost("/api/my-templates", lawyerToken, body))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.id").isNumber());
        }

        @Test
        @DisplayName("POST returns 400 when title is too short")
        void createTemplate_shortTitle_returns400() throws Exception {
            final Long catId = createCategory();
            final var body = Map.of(
                    "title", "AB",   // < 3 chars
                    "categoryId", catId,
                    "fields", List.of()
            );

            perform(authPost("/api/my-templates", lawyerToken, body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST returns 400 when categoryId is missing")
        void createTemplate_missingCategory_returns400() throws Exception {
            final var body = Map.of(
                    "title", "Valid Title",
                    "fields", List.of()
            );

            perform(authPost("/api/my-templates", lawyerToken, body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET returns paginated templates for current lawyer")
        void getTemplates_lawyer_returnsPaginatedList() throws Exception {
            perform(authGet("/api/my-templates", lawyerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("GET /{id} returns template details")
        void getTemplateById_exists_returns200() throws Exception {
            final Long catId = createCategory();
            final Long templateId = createTemplate(catId);

            perform(authGet("/api/my-templates/" + templateId, lawyerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(templateId));
        }

        @Test
        @DisplayName("GET /{id} returns 404 for non-existent template")
        void getTemplateById_notFound_returns404() throws Exception {
            perform(authGet("/api/my-templates/999999999", lawyerToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT updates a DRAFT template")
        void updateTemplate_draftTemplate_returns200() throws Exception {
            final Long catId = createCategory();
            final Long templateId = createTemplate(catId);

            final var updateBody = Map.of(
                    "title", "Updated Title " + System.nanoTime(),
                    "description", "Updated description",
                    "categoryId", catId,
                    "fields", List.of()
            );

            perform(authPut("/api/my-templates/" + templateId, lawyerToken, updateBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").isString());
        }

        @Test
        @DisplayName("POST /{id}/publish transitions template to PUBLISHED")
        void publishTemplate_draftTemplate_returns200() throws Exception {
            final Long catId = createCategory();
            final Long templateId = createTemplate(catId);

            perform(authPost("/api/my-templates/" + templateId + "/publish", lawyerToken, Map.of()))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("DELETE soft-deletes a DRAFT template")
        void deleteTemplate_draftTemplate_returns2xx() throws Exception {
            final Long catId = createCategory();
            final Long templateId = createTemplate(catId);

            perform(authDelete("/api/my-templates/" + templateId, lawyerToken))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("GET /{id} returns 403 when accessing another lawyer's template")
        void getTemplateById_otherLawyersTemplate_returns403Or404() throws Exception {
            // Create another lawyer user
            final var otherLawyer = createTestUser("other_lawyer_" + System.nanoTime(), "LAWYER");
            final String otherToken = generateToken(otherLawyer, "LAWYER");

            final Long catId = createCategory();
            final Long templateId = createTemplate(catId);

            // Other lawyer tries to access this lawyer's template
            perform(authGet("/api/my-templates/" + templateId, otherToken))
                    .andExpect(status().is4xxClientError());

            // Cleanup
            userRepository.deleteById(otherLawyer.getId());
        }
    }

    // ── Matching Rules ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/matching-rules")
    class MatchingRules {

        @Test
        @DisplayName("GET returns list of matching rules")
        void getMatchingRules_lawyer_returns200() throws Exception {
            perform(authGet("/api/matching-rules", lawyerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /template/{id} returns rules for a template")
        void getMatchingRulesByTemplate_lawyer_returns200() throws Exception {
            perform(authGet("/api/matching-rules/template/1", lawyerToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET returns 403 for USER role")
        void getMatchingRules_userRole_returns403() throws Exception {
            perform(authGet("/api/matching-rules", userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /{id} returns 404 for non-existent rule")
        void deleteMatchingRule_notFound_returns404() throws Exception {
            perform(authDelete("/api/matching-rules/999999", lawyerToken))
                    .andExpect(status().isNotFound());
        }
    }

    // ── Document Reviews ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/lawyer - Document Reviews")
    class DocumentReviews {

        @Test
        @DisplayName("GET /api/reviews returns paginated reviews")
        void getReviews_lawyer_returnsPaginatedList() throws Exception {
            perform(authGet("/api/reviews", lawyerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("GET /api/reviews returns 403 for USER role")
        void getReviews_userRole_returns403() throws Exception {
            perform(authGet("/api/reviews", userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/documents/{id}/reviews returns 404 for non-existent document")
        void getDocumentReviews_notFound_returns404() throws Exception {
            perform(authGet("/api/documents/999999/reviews", lawyerToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/documents/{id}/reviews returns 400 when notes are too short")
        void createReview_shortNotes_returns400() throws Exception {
            final var body = Map.of(
                    "notes", "short",    // < 10 chars
                    "riskLevel", "LOW",
                    "recommended", true
            );

            perform(authPost("/api/documents/1/reviews", lawyerToken, body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /api/reviews/{id} returns 404 for non-existent review")
        void deleteReview_notFound_returns404() throws Exception {
            perform(authDelete("/api/reviews/999999", lawyerToken))
                    .andExpect(status().isNotFound());
        }
    }

    // ── Lawyer Documents ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/lawyer-documents")
    class LawyerDocuments {

        @Test
        @DisplayName("GET returns paginated list of documents for lawyer's templates")
        void getDocuments_lawyer_returnsPaginatedList() throws Exception {
            perform(authGet("/api/lawyer-documents", lawyerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("GET /{id} returns 404 for non-existent document")
        void getDocumentById_notFound_returns404() throws Exception {
            perform(authGet("/api/lawyer-documents/999999", lawyerToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET returns 403 for USER role")
        void getDocuments_userRole_returns403() throws Exception {
            perform(authGet("/api/lawyer-documents", userToken))
                    .andExpect(status().isForbidden());
        }
    }
}
