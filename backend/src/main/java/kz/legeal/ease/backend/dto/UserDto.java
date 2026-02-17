package kz.legeal.ease.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import kz.legeal.ease.backend.domain.Role;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    @Schema(description = "Идентификатор пользователя")
    private Long id;

    @Schema(description = "Логин пользователя")
    private String username;

    @Email
    @Schema(description = "Email пользователя")
    private String email;

    @Schema(description = "Имя пользователя")
    private String firstName;

    @Schema(description = "Фамилия пользователя")
    private String lastName;

    @Schema(description = "Роль пользователя (например: ADMIN, SUPERVISOR, PARENT)")
    private Role role;

    @Pattern(
            regexp = "^(\\+7|8)7\\d{9}$",
            message = "Телефон должен быть в формате: +77764268111 или 87764268111"
    )
    @Schema(description = "Телефон пользователя (например: +77764268111)")
    private String telephone;

    @Schema(description = "URL на аватар/фото профиля")
    private String userProfileImageUrl;

}