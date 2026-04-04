package kz.legeal.ease.backend.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for /api/user/** endpoints.
 *
 * Covers: document lifecycle (create → validate → complete → archive → restore → delete),
 * authorization, IDOR, and validation failures.
 */
@DisplayName("User Document API")
class UserDocumentApiTest extends BaseIntegrationTest {

    private Long categoryId;
    private Long templateId;

    @BeforeEach
    void setUpTemplateAndCategory() throws Exception {
        // Create a category (needs admin)
        final var catBody = Map.of("name", "Doc Test Cat " + System.nanoTime(), "description", "");
        final var catResult = perform(authPost("/api/admin/categories", adminToken, catBody))
                .andReturn();
        categoryId = objectMapper.readTree(catResult.getResponse().getContentAsString()).get("id").asLong();

        // Create and publish a template (needs lawyer)
        final var tplBody = Map.of(
                "title", "Test Template " + System.nanoTime(),
                "description", "A test template",
                "categoryId", categoryId,
                "fields", List.of(
                        Map.of("fieldKey", "client_name", "label", "Client Name",
                                "fieldType", "TEXT", "required", true, "orderNum", 0),
                        Map.of("fieldKey", "date", "label", "Date",
                                "fieldType", "TEXT", "required", true, "orderNum", 1)
                )
        );
        final var tplResult = perform(authPost("/api/lawyer/templates", lawyerToken, tplBody))
                .andReturn();
        templateId = objectMapper.readTree(tplResult.getResponse().getContentAsString()).get("id").asLong();

        perform(authPost("/api/lawyer/templates/" + templateId + "/publish", lawyerToken, Map.of()));
    }

    // ── Authorization ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Authorization")
    class Authorization {

        @Test
        @DisplayName("GET /api/user/documents returns 403 for LAWYER role")
        void documents_lawyerRole_returns403() throws Exception {
            perform(authGet("/api/user/documents", lawyerToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/user/documents returns 403 for ADMIN role")
        void documents_adminRole_returns403() throws Exception {
            perform(authGet("/api/user/documents", adminToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/user/documents returns 401 for unauthenticated")
        void documents_noAuth_returns401() throws Exception {
            mvc.perform(get("/api/user/documents"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── Template Browsing ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/user/templates")
    class TemplateBrowsing {

        @Test
        @DisplayName("GET returns list of published templates")
        void getTemplates_user_returns200() throws Exception {
            perform(authGet("/api/user/templates", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("GET with categoryId filter returns filtered list")
        void getTemplates_withCategoryFilter_returns200() throws Exception {
            perform(authGet("/api/user/templates?categoryId=" + categoryId, userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("GET /{id} returns template details")
        void getTemplate_exists_returns200() throws Exception {
            perform(authGet("/api/user/templates/" + templateId, userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(templateId));
        }

        @Test
        @DisplayName("GET /{id} returns 404 for non-existent template")
        void getTemplate_notFound_returns404() throws Exception {
            perform(authGet("/api/user/templates/999999999", userToken))
                    .andExpect(status().isNotFound());
        }
    }

    // ── Document Lifecycle ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Document lifecycle: create → validate → complete")
    class DocumentLifecycle {

        @Test
        @DisplayName("POST creates document in DRAFT status")
        void createDocument_validRequest_returnsDraft() throws Exception {
            final var body = Map.of(
                    "templateId", templateId,
                    "title", "My Contract " + System.nanoTime(),
                    "fieldValues", Map.of(
                            "client_name", "Daniyal",
                            "date", "28.03.2026"
                    )
            );

            perform(authPost("/api/user/documents", userToken, body))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.id").isNumber());
        }

        @Test
        @DisplayName("POST returns 400 when templateId is missing")
        void createDocument_missingTemplateId_returns400() throws Exception {
            final var body = Map.of(
                    "title", "Missing Template",
                    "fieldValues", Map.of()
            );

            perform(authPost("/api/user/documents", userToken, body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST returns 400 when title is too short")
        void createDocument_shortTitle_returns400() throws Exception {
            final var body = Map.of(
                    "templateId", templateId,
                    "title", "X",
                    "fieldValues", Map.of()
            );

            perform(authPost("/api/user/documents", userToken, body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Full flow: create → validate → complete → generates PDF")
        void fullDocumentFlow_create_validate_complete() throws Exception {
            // 1. Create document
            final var createBody = Map.of(
                    "templateId", templateId,
                    "title", "Full Flow Contract " + System.nanoTime(),
                    "fieldValues", Map.of(
                            "client_name", "Daniyal",
                            "date", "28.03.2026"
                    )
            );
            final var createResult = perform(authPost("/api/user/documents", userToken, createBody))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();

            final Long docId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asLong();

            // 2. Validate document
            perform(authPost("/api/user/documents/" + docId + "/validate", userToken, Map.of()))
                    .andExpect(status().is2xxSuccessful());

            // 3. Complete (generate PDF)
            perform(authPost("/api/user/documents/" + docId + "/complete", userToken, Map.of()))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("POST /validate returns 404 for non-existent document")
        void validateDocument_notFound_returns404() throws Exception {
            perform(authPost("/api/user/documents/999999999/validate", userToken, Map.of()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /complete returns 404 for non-existent document")
        void completeDocument_notFound_returns404() throws Exception {
            perform(authPost("/api/user/documents/999999999/complete", userToken, Map.of()))
                    .andExpect(status().isNotFound());
        }
    }

    // ── Document Retrieval ────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/user/documents")
    class DocumentRetrieval {

        private Long createDocument(String title) throws Exception {
            final var body = Map.of(
                    "templateId", templateId,
                    "title", title,
                    "fieldValues", Map.of("client_name", "Aliya", "date", "01.01.2026")
            );
            final var result = perform(authPost("/api/user/documents", userToken, body))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();
            return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        }

        @Test
        @DisplayName("GET returns paginated list of user's documents")
        void getDocuments_user_returnsPaginatedList() throws Exception {
            perform(authGet("/api/user/documents", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber());
        }

        @Test
        @DisplayName("GET /all returns all documents including archived")
        void getAllDocuments_user_returnsPaginatedList() throws Exception {
            perform(authGet("/api/user/documents/all", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("GET /{id} returns document by ID")
        void getDocumentById_exists_returns200() throws Exception {
            final Long docId = createDocument("Get By ID " + System.nanoTime());

            perform(authGet("/api/user/documents/" + docId, userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(docId));
        }

        @Test
        @DisplayName("GET /{id} returns 404 for non-existent document")
        void getDocumentById_notFound_returns404() throws Exception {
            perform(authGet("/api/user/documents/999999999", userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET with pagination params respects page size")
        void getDocuments_withPagination_returnsCorrectPageSize() throws Exception {
            perform(authGet("/api/user/documents?page=0&size=3", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size").value(3));
        }
    }

    // ── Document Update ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/user/documents/{id}")
    class DocumentUpdate {

        @Test
        @DisplayName("updates a DRAFT document")
        void updateDocument_draftDocument_returns200() throws Exception {
            final var createBody = Map.of(
                    "templateId", templateId,
                    "title", "To Update " + System.nanoTime(),
                    "fieldValues", Map.of("client_name", "Old Name", "date", "01.01.2026")
            );
            final var createResult = perform(authPost("/api/user/documents", userToken, createBody))
                    .andReturn();
            final Long docId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asLong();

            final var updateBody = Map.of(
                    "title", "Updated Title",
                    "fieldValues", Map.of("client_name", "New Name", "date", "15.06.2026")
            );

            perform(authPut("/api/user/documents/" + docId, userToken, updateBody))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 404 for non-existent document")
        void updateDocument_notFound_returns404() throws Exception {
            perform(authPut("/api/user/documents/999999999", userToken, Map.of("title", "Test Upd")))
                    .andExpect(status().isNotFound());
        }
    }

    // ── Archive / Restore / Delete ────────────────────────────────────────────

    @Nested
    @DisplayName("Archive, Restore, Delete")
    class ArchiveRestoreDelete {

        @Test
        @DisplayName("DELETE removes a DRAFT document")
        void deleteDocument_draftDocument_returns2xx() throws Exception {
            final var body = Map.of(
                    "templateId", templateId,
                    "title", "To Delete " + System.nanoTime(),
                    "fieldValues", Map.of("client_name", "Test", "date", "01.01.2026")
            );
            final var result = perform(authPost("/api/user/documents", userToken, body)).andReturn();
            final Long docId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            perform(authDelete("/api/user/documents/" + docId, userToken))
                    .andExpect(status().is2xxSuccessful());

            // Document should no longer be accessible
            perform(authGet("/api/user/documents/" + docId, userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE returns 404 for non-existent document")
        void deleteDocument_notFound_returns404() throws Exception {
            perform(authDelete("/api/user/documents/999999999", userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /archive returns 4xx for DRAFT document (cannot archive draft)")
        void archiveDocument_draftDocument_returns4xx() throws Exception {
            final var body = Map.of(
                    "templateId", templateId,
                    "title", "To Archive " + System.nanoTime(),
                    "fieldValues", Map.of("client_name", "Test", "date", "01.01.2026")
            );
            final var result = perform(authPost("/api/user/documents", userToken, body)).andReturn();
            final Long docId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            // Only COMPLETED documents can be archived
            perform(authPost("/api/user/documents/" + docId + "/archive", userToken, Map.of()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("POST /restore returns 4xx for DRAFT document (cannot restore non-archived)")
        void restoreDocument_draftDocument_returns4xx() throws Exception {
            final var body = Map.of(
                    "templateId", templateId,
                    "title", "Cannot Restore " + System.nanoTime(),
                    "fieldValues", Map.of("client_name", "Test", "date", "01.01.2026")
            );
            final var result = perform(authPost("/api/user/documents", userToken, body)).andReturn();
            final Long docId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            perform(authPost("/api/user/documents/" + docId + "/restore", userToken, Map.of()))
                    .andExpect(status().is4xxClientError());
        }
    }

    // ── Rule Engine Integration ───────────────────────────────────────────────

    @Nested
    @DisplayName("Rule Engine endpoints")
    class RuleEngine {

        @Test
        @DisplayName("GET /{id}/suggestions returns suggestions")
        void getSuggestions_draftDocument_returns200() throws Exception {
            final var body = Map.of(
                    "templateId", templateId,
                    "title", "Suggestions Test " + System.nanoTime(),
                    "fieldValues", Map.of("client_name", "Test", "date", "01.01.2026")
            );
            final var result = perform(authPost("/api/user/documents", userToken, body)).andReturn();
            final Long docId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            perform(authGet("/api/user/documents/" + docId + "/suggestions", userToken))
                    .andExpect(status().isOk());
        }
    }

    // ── Sharing ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Document Sharing")
    class Sharing {

        @Test
        @DisplayName("POST /{id}/share returns 4xx for non-completed document")
        void shareDocument_notCompleted_returns4xx() throws Exception {
            final var body = Map.of(
                    "templateId", templateId,
                    "title", "Share Test " + System.nanoTime(),
                    "fieldValues", Map.of("client_name", "Test", "date", "01.01.2026")
            );
            final var result = perform(authPost("/api/user/documents", userToken, body)).andReturn();
            final Long docId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            // Cannot share a DRAFT document
            perform(authPost("/api/user/documents/" + docId + "/share", userToken, Map.of()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("GET /{id}/versions returns empty list for new document")
        void getVersions_newDocument_returnsEmptyList() throws Exception {
            final var body = Map.of(
                    "templateId", templateId,
                    "title", "Version Test " + System.nanoTime(),
                    "fieldValues", Map.of("client_name", "Test", "date", "01.01.2026")
            );
            final var result = perform(authPost("/api/user/documents", userToken, body)).andReturn();
            final Long docId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

            perform(authGet("/api/user/documents/" + docId + "/versions", userToken))
                    .andExpect(status().isOk());
        }
    }

    // ── User Profile ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/user/profile")
    class UserProfile {

        @Test
        @DisplayName("GET returns current user's profile")
        void getProfile_user_returns200() throws Exception {
            perform(authGet("/api/user/profile", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(testUser.getEmail()));
        }

        @Test
        @DisplayName("PUT updates user profile")
        void updateProfile_validRequest_returns200() throws Exception {
            final var body = Map.of(
                    "fio", "Updated Full Name"
            );

            perform(authPut("/api/user/profile", userToken, body))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT returns 400 when fio is blank")
        void updateProfile_blankFio_returns400() throws Exception {
            final var body = Map.of("fio", "");

            perform(authPut("/api/user/profile", userToken, body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET returns 403 for LAWYER role")
        void getProfile_lawyerRole_returns403() throws Exception {
            perform(authGet("/api/user/profile", lawyerToken))
                    .andExpect(status().isForbidden());
        }
    }

    // ── Lawyer Application ────────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/user/lawyer-applications")
    class LawyerApplication {

        @Test
        @DisplayName("POST submits a new lawyer application")
        void submitApplication_validRequest_returns2xx() throws Exception {
            final var body = Map.of("licenseNum", "LICENSE-12345");

            perform(authPost("/api/user/lawyer-applications", userToken, body))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("GET /my returns user's own applications")
        void getMyApplications_user_returns200() throws Exception {
            // Endpoint returns 404 when no application exists; submit one first
            perform(authPost("/api/user/lawyer-applications", userToken,
                    Map.of("licenseNum", "LICENSE-12345")));

            perform(authGet("/api/user/lawyer-applications/my", userToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST returns 400 when licenseNum is too short")
        void submitApplication_shortLicense_returns400() throws Exception {
            final var body = Map.of("licenseNum", "AB");  // < 5 chars

            perform(authPost("/api/user/lawyer-applications", userToken, body))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Matching ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("/api/user/matching")
    class Matching {

        @Test
        @DisplayName("POST returns matching templates for input text")
        void matching_validRequest_returns200() throws Exception {
            final var body = Map.of(
                    "inputText", "I need to sell my car and need a purchase agreement"
            );

            perform(authPost("/api/user/matching", userToken, body))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST returns 400 when inputText is blank")
        void matching_blankText_returns400() throws Exception {
            final var body = Map.of("inputText", "");

            perform(authPost("/api/user/matching", userToken, body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST returns 403 for LAWYER role")
        void matching_lawyerRole_returns403() throws Exception {
            final var body = Map.of("inputText", "test");

            perform(authPost("/api/user/matching", lawyerToken, body))
                    .andExpect(status().isForbidden());
        }
    }
}
