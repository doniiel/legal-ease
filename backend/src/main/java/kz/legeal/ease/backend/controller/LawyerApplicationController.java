package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kz.legeal.ease.backend.dto.LawyerApplicationDto;
import kz.legeal.ease.backend.dto.LawyerApplicationPreviewDto;
import kz.legeal.ease.backend.request.LawyerApplicationRequest;
import kz.legeal.ease.backend.request.RejectLawyerRequest;
import kz.legeal.ease.backend.request.criteria.LawyerRequestSearchCriteria;
import kz.legeal.ease.backend.service.LawyerApplicationHandlerService;
import kz.legeal.ease.backend.service.LawyerApplicationService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lawyer-applications")
@Tag(name = "Lawyer Applications")
@Validated
@RequiredArgsConstructor
public class LawyerApplicationController {

    private final LawyerApplicationService lawyerApplicationService;
    private final LawyerApplicationHandlerService handlerService;

    // ── USER endpoints ────────────────────────────────────────────────────────

    @Operation(summary = "Submit lawyer application (USER only)")
    @PostMapping
    public ResponseEntity<LawyerApplicationPreviewDto> submit(
            @RequestBody @Valid LawyerApplicationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lawyerApplicationService.submitApplication(request));
    }

    @Operation(summary = "Get my application status (USER only)")
    @GetMapping("/my")
    public ResponseEntity<LawyerApplicationPreviewDto> getMyApplication() {
        return ResponseEntity.ok(lawyerApplicationService.getMyApplication());
    }

    // ── ADMIN endpoints ───────────────────────────────────────────────────────

    @Operation(summary = "Get all applications (ADMIN only)")
    @GetMapping
    public ResponseEntity<Page<LawyerApplicationDto>> getHistory(
            @ModelAttribute LawyerRequestSearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(handlerService.getRequestsHistory(pageable, criteria));
    }

    @Operation(summary = "Get application by ID (ADMIN only)")
    @GetMapping("/{id}")
    public ResponseEntity<LawyerApplicationDto> getById(
            @Parameter(description = "Application ID", required = true) @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(handlerService.getById(id));
    }

    @Operation(summary = "Approve application (ADMIN only)")
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
            @Parameter(description = "Application ID", required = true) @NotNull @PathVariable Long id
    ) {
        handlerService.approveRequest(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reject application (ADMIN only)")
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            @Parameter(description = "Application ID", required = true) @NotNull @PathVariable Long id,
            @RequestBody @Valid RejectLawyerRequest request
    ) {
        handlerService.rejectRequest(id, request.getReason());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete application (ADMIN only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Application ID", required = true) @NotNull @PathVariable Long id
    ) {
        handlerService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}
