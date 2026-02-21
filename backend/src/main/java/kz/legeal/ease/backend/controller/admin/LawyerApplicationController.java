package kz.legeal.ease.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import kz.legeal.ease.backend.dto.LawyerApplicationDto;
import kz.legeal.ease.backend.request.RejectLawyerRequest;
import kz.legeal.ease.backend.request.criteria.LawyerRequestSearchCriteria;
import kz.legeal.ease.backend.service.LawyerApplicationHandlerService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/lawyer-applications")
@Tag(
        name = "Admin - Lawyer Requests",
        description = "APIs for managing lawyer registration requests"
)
@Validated
@RequiredArgsConstructor
public class LawyerApplicationController {

    private final LawyerApplicationHandlerService lawyerRequestService;

    @Operation(
            summary = "Get lawyer request by ID",
            description = "Returns lawyer registration request details"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request found"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LawyerApplicationDto> getPendingLawyerRequestById(
            @Parameter(description = "Request ID", example = "10", required = true)
            @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(lawyerRequestService.getById(id));
    }

    @Operation(
            summary = "Get lawyer requests history",
            description = "Returns paginated list of lawyer registration requests"
    )
    @ApiResponse(responseCode = "200", description = "Requests retrieved successfully")
    @GetMapping("/history")
    public ResponseEntity<Page<LawyerApplicationDto>> getHistory(
            @ModelAttribute LawyerRequestSearchCriteria criteria,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(lawyerRequestService.getRequestsHistory(pageable, criteria));
    }

    @Operation(
            summary = "Approve lawyer request",
            description = "Approves lawyer registration request"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lawyer approved successfully"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
            @Parameter(description = "Request ID", example = "10", required = true)
            @PathVariable Long id
    ) {
        lawyerRequestService.approveRequest(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Reject lawyer request",
            description = "Rejects lawyer registration request with reason"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lawyer rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid rejection data"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            @Parameter(description = "Request ID", example = "10", required = true)
            @PathVariable Long id,
            @RequestBody @Validated RejectLawyerRequest request
    ) {
        lawyerRequestService.rejectRequest(id, request.getReason());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Delete lawyer request",
            description = "Deletes lawyer registration request"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Request deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Request ID", example = "10", required = true)
            @PathVariable Long id
    ) {
        lawyerRequestService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}
