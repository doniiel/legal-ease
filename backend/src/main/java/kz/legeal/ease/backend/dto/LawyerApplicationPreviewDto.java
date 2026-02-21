package kz.legeal.ease.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LawyerApplicationPreviewDto {

    @Schema(description = "Unique identifier of the lawyer application request", example = "15")
    private Long id;

    @Schema(description = "Information about the lawyer (user who submitted the application)")
    private BaseUserDto lawyerInfo;

    @Schema(description = "Lawyer license number provided in the application", example = "KZ-ADV-2024-00123")
    private String licenseNumber;

    @Schema(description = "Date and time when the application was submitted", example = "2026-02-20T14:30:00")
    private LocalDateTime submittedAt;

    @Schema(description = "Reason for rejection (if the application was rejected)", example = "Invalid license number")
    private String rejectionReason;
}
