package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Document Reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final DocumentReviewService reviewService;

    @Operation(summary = "Submit or update a review (LAWYER only)")
    @PostMapping("/api/documents/{documentId}/reviews")
    public ResponseEntity<DocumentReviewDto> submitReview(
            @Parameter(description = "Document ID", required = true) @NotNull @PathVariable Long documentId,
            @Valid @RequestBody DocumentReviewRequest request
    ) {
        return ResponseEntity.ok(reviewService.submitReview(documentId, request));
    }

    @Operation(summary = "Get all reviews for a document (LAWYER only)")
    @GetMapping("/api/documents/{documentId}/reviews")
    public ResponseEntity<List<DocumentReviewDto>> getReviewsForDocument(
            @Parameter(description = "Document ID", required = true) @NotNull @PathVariable Long documentId
    ) {
        return ResponseEntity.ok(reviewService.getReviewsForDocument(documentId));
    }

    @Operation(summary = "List my reviews (LAWYER only)")
    @GetMapping("/api/reviews")
    public ResponseEntity<Page<DocumentReviewDto>> getMyReviews(
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.getMyReviews(pageable));
    }

    @Operation(summary = "Delete a review (LAWYER only)")
    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review ID", required = true) @NotNull @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
