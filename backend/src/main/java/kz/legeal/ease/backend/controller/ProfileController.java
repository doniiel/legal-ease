package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.UserProfileDto;
import kz.legeal.ease.backend.request.UpdateProfileRequest;
import kz.legeal.ease.backend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    @Operation(summary = "Get current user profile (all roles)")
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
