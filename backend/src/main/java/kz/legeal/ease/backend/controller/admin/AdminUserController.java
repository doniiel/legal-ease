package kz.legeal.ease.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import kz.legeal.ease.backend.dto.UserDto;
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
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - User Management")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Get all users (paginated)")
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAll(
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminUserService.getAll(pageable));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@NotNull @PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getById(id));
    }

    @Operation(summary = "Revoke LAWYER role — user keeps USER role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lawyer role revoked"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "User is not a lawyer or is an admin")
    })
    @PostMapping("/{id}/revoke-lawyer")
    public ResponseEntity<Void> revokeLawyer(@NotNull @PathVariable Long id) {
        adminUserService.revokeLawyerRole(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Block user account")
    @PostMapping("/{id}/block")
    public ResponseEntity<Void> block(@NotNull @PathVariable Long id) {
        adminUserService.blockUser(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Unblock user account")
    @PostMapping("/{id}/unblock")
    public ResponseEntity<Void> unblock(@NotNull @PathVariable Long id) {
        adminUserService.unblockUser(id);
        return ResponseEntity.ok().build();
    }
}
