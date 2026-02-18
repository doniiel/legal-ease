package kz.legeal.ease.backend.request.criteria;

import kz.legeal.ease.backend.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LawyerRequestSearchCriteria {

    private Status status;

    private LocalDateTime createdFrom;

    private LocalDateTime createdTo;

    private LocalDateTime reviewedFrom;

    private LocalDateTime reviewedTo;

    private String licenseNumber;

    private Long userId;

    private String reviewerFio;

}
