package kz.legeal.ease.backend.request.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClauseExplainRequest {

    @NotBlank(message = "Clause text must not be blank")
    @Size(max = 5000, message = "Clause text must not exceed 5000 characters")
    private String text;
}
