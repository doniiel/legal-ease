package kz.legeal.ease.backend.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.UserProfileDto;
import kz.legeal.ease.backend.request.UpdateProfileRequest;
import kz.legeal.ease.backend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/profile")
@PreAuthorize("isAuthenticated()")
@Tag(name = "User - Profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Operation(summary = "Get current user profile")
    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile() {
        return ResponseEntity.ok(userProfileService.getProfile());
    }

    @Operation(summary = "Update current user profile (fio, phone)")
    @PutMapping
    public ResponseEntity<UserProfileDto> updateProfile(
            @RequestBody @Valid UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userProfileService.updateProfile(request));
    }
}
