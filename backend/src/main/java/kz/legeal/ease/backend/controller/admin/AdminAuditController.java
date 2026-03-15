package kz.legeal.ease.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import kz.legeal.ease.backend.dto.AuditLogDto;
import kz.legeal.ease.backend.enums.AuditAction;
import kz.legeal.ease.backend.service.AdminAuditService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Audit Logs", description = "Search and retrieve audit trail entries")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AdminAuditService auditService;

    @Operation(
            summary = "Search audit logs",
            description = "Returns a paginated, filterable list of all audit log entries. All filter parameters are optional."
    )
    @ApiResponse(responseCode = "200", description = "Audit logs returned")
    @GetMapping
    public ResponseEntity<Page<AuditLogDto>> search(
            @Parameter(description = "Filter by acting user ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by action type")   @RequestParam(required = false) AuditAction action,
            @Parameter(description = "Filter by entity type (e.g. 'Document')") @RequestParam(required = false) String entityType,
            @Parameter(description = "Filter by entity primary key") @RequestParam(required = false) Long entityId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.search(userId, action, entityType, entityId, pageable));
    }

    @Operation(summary = "Get audit log entry by ID")
    @ApiResponse(responseCode = "200", description = "Audit log entry found")
    @ApiResponse(responseCode = "404", description = "Entry not found")
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogDto> getById(
            @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(auditService.getById(id));
    }
}
