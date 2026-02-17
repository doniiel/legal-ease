package kz.legeal.ease.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.legeal.ease.backend.request.LawyerRequestDto;
import kz.legeal.ease.backend.request.criteria.LawyerRequestSearchCriteria;
import kz.legeal.ease.backend.service.LawyerRequestService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/lawyer-requests")
@Tag(
        name = "",
        description = ""
)
@RequiredArgsConstructor
public class LawyerRequestController {

    private final LawyerRequestService lawyerRequestService;


    @Operation(summary = "Get pending lawyer requests",
            description = "Returns list of lawyer registration requests that need approval")
    @ApiResponse(responseCode = "200", description = "List of lawyer requests")
    @GetMapping("/pending")
    public ResponseEntity<Page<LawyerRequestDto>> getPendingLawyerRequests(
            @ModelAttribute LawyerRequestSearchCriteria criteria,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(lawyerRequestService.getPendingRequests(pageable, criteria));
    }

    @Operation(summary = "Get all lawyer requests",
            description = "Return list of lawyer registration requests")
    @ApiResponse(responseCode = "200", description = "List of lawyer requests")
    @GetMapping("/history")
    public ResponseEntity<Page<LawyerRequestDto>> getHistoryEducatorRequests(
            @ModelAttribute LawyerRequestSearchCriteria criteria,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(lawyerRequestService.getRequestsHistory(pageable, criteria));
    }

    @Operation(
            summary = "Approve lawyer registration",
            description = "Approve lawyer registration by supervisor"
    )
    @ApiResponse(responseCode = "200", description = "Lawyer successfully approved", content = @Content)
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveEducator(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Lawyer approve id",
                    required = true,
                    content = @Content(mediaType = "application/json")
            )
            @PathVariable Long id
    ) {
        lawyerRequestService.approveRequest(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Reject lawyer registration",
            description = "Reject lawyer registration by supervisor"
    )
    @ApiResponse(responseCode = "200", description = "Lawyer successfully rejected", content = @Content)
    @ApiResponse(responseCode = "400", description = "Invalid registration data", content = @Content)
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectEducator(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Lawyer reject id",
                    required = true,
                    content = @Content(mediaType = "application/json")
            )
            @PathVariable Long id) {
        lawyerRequestService.rejectRequest(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Delete educator",
            description = "Delete an educator and related data by educator ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Educator successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Educator not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteEducator(
            @Parameter(description = "Educator ID", required = true, example = "123")
            @PathVariable Long id) {
        lawyerRequestService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}
