package kz.legeal.ease.backend.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kz.legeal.ease.backend.enums.Gender;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "ФИО обязательно")
    @Schema(description = "ФИО пользователя")
    private String fio;

    @Pattern(
            regexp = "^(\\+7|8)7\\d{9}$",
            message = "Телефон должен быть в формате: +77764268111 или 87764268111"
    )
    @Schema(description = "Телефон пользователя")
    private String phone;

    @Schema(description = "Пол пользователя: MALE или FEMALE")
    private Gender gender;
}
