package kz.legeal.ease.backend.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for public (no-auth) endpoints:
 * - GET /open-api/categories
 * - GET /open-api/documents/verify/{id}
 */
@DisplayName("Public API")
class PublicApiTest extends BaseIntegrationTest {

    // ── Categories ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /open-api/categories")
    class Categories {

        @Test
        @DisplayName("returns 200 with list (no auth required)")
        void getCategories_noAuth_returns200() throws Exception {
            mvc.perform(get("/open-api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("returns 200 even with Bearer token present")
        void getCategories_withAuth_returns200() throws Exception {
            mvc.perform(get("/open-api/categories")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());
        }
    }

    // ── Document Verification ─────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /open-api/documents/verify/{id}")
    class DocumentVerification {

        @Test
        @DisplayName("returns INVALID for non-existent document")
        void verify_nonExistentDocument_returnsInvalid() throws Exception {
            mvc.perform(get("/open-api/documents/verify/999999999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("INVALID"))
                    .andExpect(jsonPath("$.hashValid").value(false))
                    .andExpect(jsonPath("$.documentId").value("DOC-999999999"));
        }

        @Test
        @DisplayName("returns 200 without any authentication header")
        void verify_noAuth_returns200() throws Exception {
            mvc.perform(get("/open-api/documents/verify/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("documentId is formatted as DOC-NNNNNN")
        void verify_documentIdFormat() throws Exception {
            mvc.perform(get("/open-api/documents/verify/42"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.documentId").value("DOC-000042"));
        }
    }
}
