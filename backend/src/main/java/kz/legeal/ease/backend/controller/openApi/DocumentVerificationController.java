package kz.legeal.ease.backend.controller.openApi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.legeal.ease.backend.dto.document.DocumentVerificationResponse;
import kz.legeal.ease.backend.service.document.DocumentVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoint for QR-code-based document verification.
 *
 * <p>Route: {@code GET /open-api/documents/verify/{id}}
 * <p>Authentication: none — this is the destination URL encoded in the QR code.
 */
@Tag(name = "Document Verification", description = "Public document authenticity check")
@RestController
@RequestMapping("/open-api/documents")
@RequiredArgsConstructor
public class DocumentVerificationController {

    private final DocumentVerificationService verificationService;

    @Operation(
        summary = "Verify document authenticity",
        description = "Checks whether the document was properly finalized through LegalEase. "
                    + "Returns VALID if a content hash is stored, INVALID otherwise."
    )
    @GetMapping("/verify/{id}")
    public ResponseEntity<DocumentVerificationResponse> verify(
            @Parameter(description = "Numeric document ID from the QR code URL")
            @PathVariable Long id) {
        return ResponseEntity.ok(verificationService.verify(id));
    }
}
