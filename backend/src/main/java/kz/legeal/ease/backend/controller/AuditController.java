package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Logs (Admin)")
@RequiredArgsConstructor
public class AuditController {

    private final AdminAuditService auditService;

    @Operation(summary = "Search audit logs (ADMIN only)")
    @GetMapping
    public ResponseEntity<Page<AuditLogDto>> search(
            @Parameter(description = "Filter by user ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by action type") @RequestParam(required = false) AuditAction action,
            @Parameter(description = "Filter by entity type") @RequestParam(required = false) String entityType,
            @Parameter(description = "Filter by entity ID") @RequestParam(required = false) Long entityId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.search(userId, action, entityType, entityId, pageable));
    }

    @Operation(summary = "Get audit log entry by ID (ADMIN only)")
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogDto> getById(@NotNull @PathVariable Long id) {
        return ResponseEntity.ok(auditService.getById(id));
    }
}
