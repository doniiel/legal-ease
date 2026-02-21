package kz.legeal.ease.backend.controller;

import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.LawyerApplicationPreviewDto;
import kz.legeal.ease.backend.request.LawyerApplicationRequest;
import kz.legeal.ease.backend.service.LawyerApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lawyer-applications")
@RequiredArgsConstructor
public class LawyerApplicationController {

    private final LawyerApplicationService lawyerApplicationService;

    @PostMapping
    public ResponseEntity<LawyerApplicationPreviewDto> submitApplication(
            @RequestBody @Valid LawyerApplicationRequest request
    ) {
        return ResponseEntity.ok(lawyerApplicationService.submitApplication(request));
    }
}
