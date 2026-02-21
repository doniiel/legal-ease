package kz.legeal.ease.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/lawyer-applications")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Lawyer Applications", description = "Manage lawyer registration requests")
@Validated
@RequiredArgsConstructor
public class AdminLawyerApplicationController {

    private final LawyerApplicationHandlerService handlerService;

    @Operation(summary = "Get all applications (paginated)")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<LawyerApplicationDto>> getHistory(
            @ModelAttribute LawyerRequestSearchCriteria criteria,
            @ParameterObject @PageableDefault(
                    size = 10,
                    sort = "createdDate",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(handlerService.getRequestsHistory(pageable, criteria));
    }

    @Operation(summary = "Get application by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LawyerApplicationDto> getById(
            @Parameter(description = "Application ID", required = true)
            @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(handlerService.getById(id));
    }

    @Operation(summary = "Approve application")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Approved"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Already processed")
    })
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
            @Parameter(description = "Application ID", required = true)
            @NotNull @PathVariable Long id
    ) {
        handlerService.approveRequest(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reject application")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rejected"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            @Parameter(description = "Application ID", required = true)
            @NotNull @PathVariable Long id,
            @RequestBody @Valid RejectLawyerRequest request
    ) {
        handlerService.rejectRequest(id, request.getReason());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete application")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete approved application")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Application ID", required = true)
            @NotNull @PathVariable Long id
    ) {
        handlerService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }

}
