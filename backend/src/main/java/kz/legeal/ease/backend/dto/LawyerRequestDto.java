package kz.legeal.ease.backend.dto;

import kz.legeal.ease.backend.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LawyerRequestDto {

    private Long id;

    private BaseUserDto lawyerInfo;

    private String licenseNumber;

    private Status status;

    private LocalDateTime submittedAt;

    private BaseUserDto reviewerInfo;

    private LocalDateTime reviewedAt;

    private String rejectionReason;
}
