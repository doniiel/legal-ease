package kz.legeal.ease.backend.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.LawyerApplicationPreviewDto;
import kz.legeal.ease.backend.request.LawyerApplicationRequest;
import kz.legeal.ease.backend.service.LawyerApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/lawyer-applications")
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Lawyer Applications", description = "Submit and track lawyer registration application")
@RequiredArgsConstructor
public class LawyerApplicationController {

    private final LawyerApplicationService lawyerApplicationService;

    @Operation(summary = "Submit lawyer application")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Application submitted successfully"),
            @ApiResponse(responseCode = "409", description = "Application already exists")
    })
    @PostMapping
    public ResponseEntity<LawyerApplicationPreviewDto> submit(
            @RequestBody @Valid LawyerApplicationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lawyerApplicationService.submitApplication(request));
    }

    @Operation(summary = "Get my application status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application found"),
            @ApiResponse(responseCode = "404", description = "No application found")
    })
    @GetMapping("/my")
    public ResponseEntity<LawyerApplicationPreviewDto> getMyApplication() {
        return ResponseEntity.ok(lawyerApplicationService.getMyApplication());
    }

}
