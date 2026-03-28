package kz.legeal.ease.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {

    @Schema(description = "ID пользователя")
    private Long id;

    @Schema(description = "ФИО пользователя")
    private String fio;

    @Schema(description = "Email пользователя")
    private String email;

    @Schema(description = "Телефон пользователя")
    private String phone;

    @Schema(description = "ИИН пользователя")
    private String iin;

    @Schema(description = "Пол пользователя")
    private String gender;

    @Schema(description = "Дата рождения")
    private LocalDate dateOfBirth;

    @Schema(description = "Роль: ADMIN, LAWYER, USER")
    private String role;

    @Schema(description = "Статус активности аккаунта")
    private boolean active;
}
