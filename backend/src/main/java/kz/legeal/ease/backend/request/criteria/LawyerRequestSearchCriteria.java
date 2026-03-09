package kz.legeal.ease.backend.request.criteria;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.legeal.ease.backend.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Filters for searching lawyer applications")
public class LawyerRequestSearchCriteria {

    @Schema(description = "Application status", example = "APPROVED")
    private Status status;

    @Schema(description = "Filter applications created after this date",
            example = "2025-01-01T00:00:00")
    private LocalDateTime createdFrom;

    @Schema(description = "Filter applications created before this date",
            example = "2025-12-31T23:59:59")
    private LocalDateTime createdTo;

    @Schema(description = "Filter applications reviewed after this date",
            example = "2025-01-01T00:00:00")
    private LocalDateTime reviewedFrom;

    @Schema(description = "Filter applications reviewed before this date",
            example = "2025-12-31T23:59:59")
    private LocalDateTime reviewedTo;

    @Schema(description = "Lawyer license number", example = "KZ-123456")
    private String licenseNumber;

    @Schema(description = "User ID", example = "15")
    private Long userId;

    @Schema(description = "Reviewer full name", example = "John Doe")
    private String reviewerFio;
}
