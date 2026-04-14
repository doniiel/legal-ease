package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import kz.legeal.ease.backend.dto.UserDto;
import kz.legeal.ease.backend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management (Admin)")
@RequiredArgsConstructor
public class UserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Get all users (ADMIN only)")
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAll(
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminUserService.getAll(pageable));
    }

    @Operation(summary = "Get user by ID (ADMIN only)")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@NotNull @PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getById(id));
    }

    @Operation(summary = "Revoke LAWYER role (ADMIN only)")
    @PostMapping("/{id}/revoke-lawyer")
    public ResponseEntity<Void> revokeLawyer(@NotNull @PathVariable Long id) {
        adminUserService.revokeLawyerRole(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Block user account (ADMIN only)")
    @PostMapping("/{id}/block")
    public ResponseEntity<Void> block(@NotNull @PathVariable Long id) {
        adminUserService.blockUser(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Unblock user account (ADMIN only)")
    @PostMapping("/{id}/unblock")
    public ResponseEntity<Void> unblock(@NotNull @PathVariable Long id) {
        adminUserService.unblockUser(id);
        return ResponseEntity.ok().build();
    }
}
