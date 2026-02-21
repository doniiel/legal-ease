package kz.legeal.ease.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class UserRoleDto {

    private Long id;

    private Long userId;

    private Long roleId;

    private LocalDateTime beginDate;

    private LocalDateTime endDate;
}