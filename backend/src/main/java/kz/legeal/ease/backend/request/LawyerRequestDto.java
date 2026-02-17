package kz.legeal.ease.backend.request;

import kz.legeal.ease.backend.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LawyerRequestDto {

    private Long id;

    private String lawyerFio;

    private String lawyerEmail;

    private String lawyerIIN;

    private LocalDateTime requestDate;

    private Long userId;

    private Status status;

    private boolean active;
}
