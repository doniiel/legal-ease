package kz.legeal.ease.backend.request.criteria;

import kz.legeal.ease.backend.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LawyerRequestSearchCriteria {

    private String fio;

    private String email;

    private LocalDate requestDate;

    private Status status;
}
