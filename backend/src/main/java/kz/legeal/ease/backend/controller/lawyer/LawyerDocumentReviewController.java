package kz.legeal.ease.backend.controller.lawyer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kz.legeal.ease.backend.dto.document.DocumentReviewDto;
import kz.legeal.ease.backend.request.DocumentReviewRequest;
import kz.legeal.ease.backend.service.lawyer.DocumentReviewService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lawyer")
@PreAuthorize("hasRole('LAWYER')")
@Tag(name = "Lawyer - Document Reviews", description = "Submit and manage reviews of user documents")
@RequiredArgsConstructor
public class LawyerDocumentReviewController {

    private final DocumentReviewService reviewService;

    @Operation(
            summary = "Submit or update a review",
            description = "Submit a review for a document created from one of the lawyer's templates. " +
                    "If a review by this lawyer already exists, it is updated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review submitted"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Document not from your template"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PostMapping("/documents/{documentId}/review")
    public ResponseEntity<DocumentReviewDto> submitReview(
            @Parameter(description = "Document ID", required = true) @NotNull @PathVariable Long documentId,
            @Valid @RequestBody DocumentReviewRequest request
    ) {
        return ResponseEntity.ok(reviewService.submitReview(documentId, request));
    }

    @Operation(
            summary = "Get all reviews for a document",
            description = "Returns all lawyer reviews for a specific document. Document must belong to your template."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews returned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/documents/{documentId}/reviews")
    public ResponseEntity<List<DocumentReviewDto>> getReviewsForDocument(
            @Parameter(description = "Document ID", required = true) @NotNull @PathVariable Long documentId
    ) {
        return ResponseEntity.ok(reviewService.getReviewsForDocument(documentId));
    }

    @Operation(summary = "List my reviews", description = "Paginated list of all reviews submitted by the current lawyer")
    @ApiResponse(responseCode = "200", description = "Reviews returned")
    @GetMapping("/reviews")
    public ResponseEntity<Page<DocumentReviewDto>> getMyReviews(
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.getMyReviews(pageable));
    }

    @Operation(summary = "Delete a review")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Review deleted"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review ID", required = true) @NotNull @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
